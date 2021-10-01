(ns clojure-experiments.aws.logs
  "Utility functions to load data from cloudwatch insights logs.
  Be mindful about concurrency limits - if you reach the limit you'll get an error:
      \"LimitExceededException\", :message \"Account maximum query concurrency limit of [10] reached...

  Resources:
  - Cloudwatch Insights query syntax: https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/CWL_QuerySyntax.html
  - aws-api: https://github.com/cognitect-labsuaws-api"
  (:require [clojure-experiments.concurrency :refer [map-throttled]]
            [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [clojure-experiments.visualizations.oz :as my-oz]
            [oz.core :as oz]
            [clojure.spec.alpha :as s]
            [clojure-experiments.stats.descriptive :as stat]
            [clojure.core.protocols :as p])
  (:import (org.apache.commons.lang3 StringUtils)))

(def logs (aws/client {:api :logs
                       :credentials-provider (credentials/profile-credentials-provider "empear-dev")}))

(def max-concurrent-queries 10)

;; https://docs.aws.amazon.com/AmazonCloudWatchLogs/latest/APIReference/API_StartQuery.html
(:StartQuery (aws/ops logs))

;; https://docs.aws.amazon.com/AmazonCloudWatchLogs/latest/APIReference/API_GetQueryResults.html
(:GetQueryResults (aws/ops logs))

(defn date-time
  ([y m d]
   (date-time y m d 0 0 0))
  ([y m d h mi s]
   (java.time.ZonedDateTime/of y m d h mi s 0 java.time.ZoneOffset/UTC)))

(defn now []
  (java.time.ZonedDateTime/now java.time.ZoneOffset/UTC))


;; handle API failures: https://github.com/cognitect-labsuaws-api#responses-success-failure
(defn invoke-op [op-fn]
  (let [response (op-fn)]
    (when-let [error-category (:cognitect.anomalies/category response)]
      (println "ERROR: " error-category)
      (prn "Full response: " response))
    response))

(s/fdef start-query
  :args (s/cat :query-definition (s/keys :req-un [::group-name ::query])
               :interval (s/keys :req-un [::start-time ::end-time])))
(defn start-query [{:keys [group-name query]}
                   {:keys [start-time end-time]}]
  (println "DEBUG: start-query: " start-time end-time )
  (let [{:keys [queryId] :as response} (invoke-op
                                         #(aws/invoke logs {:op :StartQuery
                                                            :request {:logGroupName group-name
                                                                      :startTime (.toEpochSecond start-time)
                                                                      :endTime (.toEpochSecond end-time)
                                                                      :queryString query}}))]
    (if queryId
      queryId 
      (throw (ex-info "Couldn't submit the query. Concurrency limit reached?"
                      {:query query
                       :start-time start-time
                       :end-time end-time
                       :response response})))))

(defn- results->map [query-results]
  (->> query-results
       (mapv
        #(zipmap (mapv :field %)
                 (mapv :value %)))))

(defn get-query-results [query-id]
  (println "DEBUG: get-query-results: " query-id)
  (let [{:keys [status results]} (invoke-op
                                  #(aws/invoke logs {:op :GetQueryResults
                                                     :request {:queryId query-id}}))]
    (if (#{"Scheduled" "Running"} status)
      :incomplete
      (results->map results))))

(defn get-finished-query-results
  "This is a specialized version of `get-query-results` which assumes that the job 'status' field
  is present in the query results.
  Only results with status 'Scheduled' or 'Running' are included."
  [query-id]
  (let [results (get-query-results query-id)]
    (if (= :incomplete results)
      :incomplete
      (filterv (fn [{:strs [job_id status] :as _row}]
                 (and job_id
                      (not (#{":running" "submitted"}
                            status))))
               results))))

(defn poll-results
  ([query-id]
   (poll-results query-id false))
  ([query-id remove-incomplete-jobs?]
   (let [max-polling-time 10000
         polling-start (System/currentTimeMillis)]
     (loop []
       (let [results (if remove-incomplete-jobs?
                       (get-finished-query-results query-id)
                       (get-query-results query-id))
             elapsed-time (- (System/currentTimeMillis) polling-start)]
         (cond
           (> elapsed-time  max-polling-time)
           ;; can be either :incomplete or real results
           results

           (= results :incomplete)
           (do (Thread/sleep 1000)
               (recur))

           :else results))))))

(defn- truncate-to-midnight [date-time]
  (.truncatedTo date-time java.time.temporal.ChronoUnit/DAYS))

(defn from-to
  "Returns pairs (2-element vectors) of all 1-day periods from given 'from' day to given 'to' day.
  If `to` 
  The input dates will be truncated to a day precision (via LocalDate).)"
  ([from]
   (from-to from (now)))
  ([from to]
   (let [between
         ;; start with given date and do 1-day increments
         (as-> (iterate (fn [zdt] (truncate-to-midnight (.plusDays zdt 1)))
                        from) $
           (take-while (fn [zdt] (<= (.compareTo zdt to)
                                     0))
                       $)
              ;; add the `to` datetime if needed (if it's not aligned to the midnight)
           (vec $)
           (cond-> $ (not= to (truncate-to-midnight to))
                   (conj to))
           (dedupe $))]
     ;; convert the simple sequence of datetimes into proper pairs/intervals
     (map vec (partition 2 1 between)))))

;; 
#_(from-to (date-time 2020 6 1))
(from-to (date-time 2020 6 1) (date-time 2020 6 5))
;; => ([#object[java.time.ZonedDateTime 0x7291b6e0 "2020-06-01T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x7cc8e5b2 "2020-06-02T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0x7cc8e5b2 "2020-06-02T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x457176a9 "2020-06-03T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0x457176a9 "2020-06-03T00:00Z"]
;;      #object[java.time.ZonedDateTime 0xdfd5d2b "2020-06-04T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0xdfd5d2b "2020-06-04T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x55342fb3 "2020-06-05T00:00Z"]])

;; when you specify time beyond mindnight you get the whole days + 1 partial 'day' interval:
(from-to (date-time 2020 6 1) (date-time 2020 6 3 8 0 0))
;; => ([#object[java.time.ZonedDateTime 0x77e28d46 "2020-06-01T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x3a3d8d4b "2020-06-02T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0x3a3d8d4b "2020-06-02T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x526239d7 "2020-06-03T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0x526239d7 "2020-06-03T00:00Z"]
;;      #object[java.time.ZonedDateTime 0xc825da3 "2020-06-03T08:00Z"]])

(defn get-all-data
  [delay-query delta-duration-query other-durations-query git-clone-query from-to-intervals]
  (let [started-queries (map-throttled
                         (quot max-concurrent-queries 4)
                         (fn [[start-time end-time]]
                           (let [delay-query-id (start-query delay-query {:start-time start-time
                                                                          :end-time end-time})
                                 delta-duration-query-id (start-query delta-duration-query {:start-time start-time
                                                                                      :end-time end-time})
                                 other-durations-query-id (start-query other-durations-query
                                                                       {:start-time start-time
                                                                        :end-time end-time})
                                 clone-query-id (start-query git-clone-query {:start-time start-time
                                                                              :end-time end-time})]
                             {:start-time start-time
                              :end-time end-time
                              :delay-id delay-query-id
                              :delta-duration-id delta-duration-query-id
                              :other-durations-id other-durations-query-id
                              :clone-id clone-query-id}))
                         from-to-intervals)]
    (mapv
     (fn [{:keys [delay-id delta-duration-id other-durations-id clone-id start-time end-time]}]
       {:start-time start-time
        :end-time end-time
        :delays (poll-results delay-id true)
        :delta-durations (poll-results delta-duration-id true)
        :other-durations (poll-results other-durations-id true)
        :clone-durations (poll-results clone-id)})
     started-queries)))

(def jobs-delay-query
  {:group-name "codescene-web-prod-application"
   :query "fields @timestamp, @message
  | filter @message like /batch-job-id=/
  | parse /^.*job-id=(?<job_id>\\d+) job-type=(?<job_type>\\S+) job-status=:?(?<job_status>(submitted|running)).*batch-job-id=(?<batch_job_id>\\S+).*$/
  | stats earliest(@timestamp) as submitted_at,
        latest(@timestamp) as started_at,
        (started_at - submitted_at)/1000 as delay_seconds
        by job_id, job_type, batch_job_id
  | sort delay_seconds desc
  | limit 10000"})

(def delta-jobs-durations-query
  {:group-name "codescene-web-prod-application"
   :query "fields @timestamp, @message
  | filter @message like /batch-job-id=/
  | parse /^.*job-id=(?<job_id>\\d+) job-type=(?<job_type>\\S+) job-status=(?<job_status>\\S+).*batch-job-id=(?<batch_job_id>\\S+).*$/
  | filter job_type = ':run-delta-analysis'
  | stats latest(job_status) as status,
        earliest(@timestamp) as submitted_at,
        latest(@timestamp) as finished_at,
        (finished_at - submitted_at)/1000 as duration_seconds
        by job_id, job_type, batch_job_id
  | sort duration_seconds desc
  | limit 10000 "})

(def other-jobs-durations-query
  {:group-name "codescene-web-prod-application"
   :query "fields @timestamp, @message
| filter @message like /batch-job-id=/
| parse /^.*job-id=(?<job_id>\\d+) job-type=(?<job_type>\\S+) job-status=(?<job_status>\\S+).*batch-job-id=(?<batch_job_id>\\S+).*$/
| filter job_type != ':run-delta-analysis'
| stats latest(job_status) as status,
        earliest(@timestamp) as submitted_at,
        latest(@timestamp) as finished_at,
        (finished_at - submitted_at)/1000 as duration_seconds
        by job_id, job_type, batch_job_id
| sort duration_seconds desc"})


(def git-clones-query
  {:group-name "/aws/batch/job"
   :query "fields @timestamp, @message
  | filter (@message like /(Cloning|Successfully cloned)/ and @logStream like /codescene-prod/)
  | parse /^(?<date>\\S+) (?<time>\\S+) ip-(?<host_ip>[^\\.]+)\\.(\\S+) .*\\(:(?<job_type>[^ ]+) #(?<job_id>\\d+)\\) - (Cloning|Successfully cloned) https:\\/\\/[^\\/]+\\/(?<repository>\\S+) to (?<dir>\\S+)$/
  | stats earliest(@timestamp) as clone_start,
        latest(@timestamp) as clone_finished,
        (clone_finished - clone_start)/1000 as duration_seconds
        by job_type, job_id, host_ip, repository, @logStream
  | sort duration_seconds desc
  | limit 10000"})

;; could be useful to use Histogram + color: https://youtu.be/9uaHRWj04D4?t=439
(defn hist
  "Shows two histograms stacked vertically (one under another).
  The bottom one has 'symlog' scale for easier interpretation of outliers (such as a long git clone)"
  ([data field-name]
   (hist nil data field-name))
  ([chart-title data field-name]
   (hist chart-title data field-name nil))
  ([chart-title data field-name opts]
   {:vconcat [(my-oz/histogram data
                               field-name
                               (merge {:step 10
                                       :width 1200
                                       :height 600}
                                      (when chart-title {:title chart-title})
                                      opts))
              (my-oz/histogram data
                               field-name
                               (merge {:step 10
                                       :scale "symlog"
                                       :width 1200
                                       :height 600}
                                      (when chart-title {:title (str "[LOG scale - Y axis]: " chart-title)})
                                      opts))]}))

;; E.g. distinguishing multiple job types (delta, full analysis, x-ray, project delete)
;; via color: https://vega.github.io/vega-lite/docs/bar.html#stack
;; To get custom colors (scale) see https://stackoverflow.com/questions/58933759/vega-lite-set-color-from-data-whilst-retaining-a-legend
(s/def ::field-value string?)
(s/def ::rgb-color-string string?)
(s/def ::domain (s/coll-of ::field-value :kind vector? ))
(s/def ::range (s/coll-of ::rgb-color-string :kind vector?))
(s/fdef color
  :args (s/cat :field-name string?
               :color-scale (s/? (s/nilable (s/keys :req-un [::domain ::range])))))
(defn- color
  "See https://stackoverflow.com/questions/58933759/vega-lite-set-color-from-data-whilst-retaining-a-legend
  for more details about color scale."
  ([field-name]
   (color field-name nil))
  ([field-name color-scale]
   (cond-> {:color {:field field-name :type "nominal"}}
         color-scale (assoc-in [:color :scale] color-scale))))

;; Example of using custom histogram colors to better distinguish between different values
;; Here the default colors would be badly readable (orange vs red)
(def job-type-color
  (color "job_type" {:domain [":run-analysis" ":run-delta-analysis" ":run-xray" ":delete-project"]
                     :range [ "rgb(255, 0, 0)" "rgb(0, 255, 0)" "rgb(0, 0, 255)" "rgb(0, 0, 0)"]}))

(defn- to-date [epoch-millis-str]
  (java.util.Date. (Long/parseLong epoch-millis-str)))

;; descriptive statistics for all delta durations
(defn- describe-durations
  "Computes descriptive statistics for all the durations in given data (assumed to be partitioned by day).
  The `data-key` is used to select appropriate record within a single day data;
  it should be :delta-durations, :clone-durations, or :other-durations.
  The value associated with the data-key is supposed to contain the 'duration_seconds' key."
  ([many-days-data data-key]
   (describe-durations many-days-data data-key "duration_seconds"))
  ([many-days-data data-key duration-key]
   (let [durations-seconds (->> many-days-data
                                (mapcat data-key)
                                (mapv (fn [job-data]
                                        (Double/parseDouble (get job-data duration-key)))))]
     (stat/describe durations-seconds))))

(defn describe-durations-per-day
  "Similar to `describe-durations` but produces statistics for each day separately
  as a vector as defined by `stat/describe-as-vector`."
  ([many-days-data data-key]
   (describe-durations-per-day many-days-data data-key "duration_seconds"))
  ([many-days-data data-key duration-key]
   (let [stats-key (keyword (str (name data-key) "-stats")) ; e.g. :delta-durations-stats
         with-stats (mapv (fn [day-data]
                            (let [with-durations-as-doubles
                                  (-> day-data
                                      (update data-key
                                              (fn [durations]
                                                (mapv (fn [job-data]
                                                        (Double/parseDouble (get job-data duration-key)))
                                                      durations)))
                                      (update :start-time str))]
                              (assoc with-durations-as-doubles
                                     stats-key
                                     (stat/describe-as-vector stat/describe-as-ints
                                                              (get with-durations-as-doubles data-key)))))
                          many-days-data)]
     (mapv #(select-keys % [:start-time stats-key])
           with-stats))))

(defn filter-job-type [job-type many-days-data]
  (mapv (fn [day-data]
          (update day-data :clone-durations
                  (partial filterv (fn [{:strs [job_type] :as _daily-clones}]
                                     (= job-type job_type)))))
        many-days-data))


(comment

  ;; delays
  (def delays-query-id (start-query jobs-delay-query
                                    {:start-time (date-time 2020 6 4) :end-time (date-time 2020 6 5)}))
  (Thread/sleep 5000)
  (def delays (get-finished-query-results delays-query-id))
  (oz/view! (hist delays "delay_seconds"))

  ;; delta durations
  (def delta-query-id (start-query delta-jobs-durations-query
                                   {:start-time (date-time 2020 6 4) :end-time (date-time 2020 6 5)}))
  (Thread/sleep 5000)
  (def deltas (get-finished-query-results delta-query-id))
  (oz/view! (hist deltas "duration_seconds"))

  ;; git clones durations
  (def clones-query-id (start-query git-clones-query
                                    {:start-time (date-time 2020 6 4) :end-time (date-time 2020 6 5)}))
  (Thread/sleep 5000)
  (def clones (get-query-results clones-query-id))
  (oz/view! (hist clones "duration_seconds" (color "repository")))


  ;;; show multiple days data at once via Hiccup: https://github.com/metasoarous/oz#hiccup
  (do
    (time (def multiple-days-data (get-all-data jobs-delay-query
                                                delta-jobs-durations-query
                                                other-jobs-durations-query
                                                git-clones-query
                                                (from-to (truncate-to-midnight (.minusDays (now)
                                                                                           4))))))
  
      ;; TODO: use Vega Lite's combinators: https://youtu.be/9uaHRWj04D4?t=572
      ;; (facet row, vconcat, layer, repeat row)
    ;; TODO: support interval selection and zooming: https://vega.github.io/vega-lite-v2/docs/selection.html#scale-domains
    (do
      (def multiple-days-data-histograms
        [:div
           ;; this must be a lazy seq, not a vector otherwise an 'Invalid arity' error is thrown in oz.js
         (for [{:keys [start-time end-time delays delta-durations other-durations clone-durations]} multiple-days-data
               :let [date-str (format "%s -- %s" start-time end-time)]]
           [:div
            [:p [:b date-str]]
            [:div {:style {:display "flex" :flex-direction "col"}}

             [:vega-lite (hist (format "Batch jobs delays in seconds (%s)" date-str) delays "delay_seconds" job-type-color)]

             [:vega-lite (hist (format "Delta jobs total durations in seconds (%s)" date-str) delta-durations "duration_seconds")]
             ;; boxplot is confusing => don't show it
             #_[:vega-lite (my-oz/boxplot delta-durations "duration_seconds"
                                          {:extent 10.0})]

             ;; default colors are hard to read - check https://stackoverflow.com/questions/58933759/vega-lite-set-color-from-data-whilst-retaining-a-legend
             [:vega-lite (hist (format "Other jobs total durations in seconds (%s)" date-str) other-durations "duration_seconds" job-type-color)]
               ;; TODO: having many different repos make the chart less readable and bigger -> perhaps use separate visualization?

             [:vega-lite (hist (format "Git clones durations (%s)" date-str) clone-durations "duration_seconds" (color "repository"))]]

            [:hr]])])

      (oz/view! multiple-days-data-histograms)))

  ;;; descriptive statistics for all delta durations
  ;;; TODO: it would be useful to remove weekends

  ;; See `clojure-experiments.stats.descriptive/selected-keys`

  ;; git clones
  (describe-durations-per-day multiple-days-data :clone-durations)
  ;;=> 
  [{:start-time "2021-09-23T00:00Z", :clone-durations-stats [0 1 3 19 39 1014 13 39 11976 902]}
   {:start-time "2021-09-24T00:00Z", :clone-durations-stats [0 1 4 15 38 304 11 19 9793 825]}
   {:start-time "2021-09-25T00:00Z", :clone-durations-stats [0 1 2 9 43 302 11 28 2636 221]}
   {:start-time "2021-09-26T00:00Z", :clone-durations-stats [0 1 3 10 44 277 12 26 2736 226]}
   {:start-time "2021-09-27T00:00Z", :clone-durations-stats [0 1 3 11 34 333 11 21 8286 745]}
   {:start-time "2021-09-28T00:00Z", :clone-durations-stats [0 1 3 16 44 773 13 33 13389 962]}
   {:start-time "2021-09-29T00:00Z", :clone-durations-stats [0 1 4 21 43 6217 19 193 20166 1038]}
   {:start-time "2021-09-30T00:00Z", :clone-durations-stats [0 1 3 18 74 12066 41 570 34720 835]}
   {:start-time "2021-10-01T00:00Z", :clone-durations-stats [0 1 3 10 33 154 9 15 2577 270]}]



   [{:start-time "2021-03-31T00:00Z", :clone-durations-stats [0 2 5 9 37 295 10 19 6331 589]}
    {:start-time "2021-04-01T00:00Z", :clone-durations-stats [0 2 4 8 34 299 9 17 6004 633]}
    {:start-time "2021-04-02T00:00Z", :clone-durations-stats [0 1 5 8 29 276 8 19 3817 437]}
    {:start-time "2021-04-03T00:00Z", :clone-durations-stats [0 0 3 11 44 305 13 33 1690 127]}
    {:start-time "2021-04-04T00:00Z", :clone-durations-stats [0 1 4 8 54 266 13 32 1833 140]}
    {:start-time "2021-04-05T00:00Z", :clone-durations-stats [0 1 5 9 35 297 10 20 5179 477]}
    {:start-time "2021-04-06T00:00Z", :clone-durations-stats [0 2 5 9 31 350 9 18 5794 615]}
    {:start-time "2021-04-07T00:00Z", :clone-durations-stats [0 2 6 9 35 304 11 19 7113 639]}
    {:start-time "2021-04-08T00:00Z", :clone-durations-stats [0 1 5 8 125 191 15 36 1310 83]}]


   [{:start-time "2021-01-05T00:00Z", :clone-durations-stats [1 3 6 17 332 40818 118 1809 60445 510]}
    {:start-time "2021-01-06T00:00Z", :clone-durations-stats [1 3 6 25 169 85407 242 4182 101325 417]}
    {:start-time "2021-01-07T00:00Z", :clone-durations-stats [1 3 6 21 60 82945 229 4105 93835 408]}
    {:start-time "2021-01-08T00:00Z", :clone-durations-stats [1 3 5 19 101 79614 218 3941 89062 408]}
    {:start-time "2021-01-09T00:00Z", :clone-durations-stats [1 2 4 19 83 81928 726 7605 84279 116]}
    {:start-time "2021-01-10T00:00Z", :clone-durations-stats [1 2 3 14 100 81528 626 7015 84571 135]}

   [{:start-time "2021-01-06T00:00Z", :clone-durations-stats [1 3 6 25 169 85407 242 4182 101325 417]}
    {:start-time "2021-01-07T00:00Z", :clone-durations-stats [1 3 6 21 60 82945 229 4105 93835 408]}
    {:start-time "2021-01-08T00:00Z", :clone-durations-stats [1 3 5 19 101 79614 218 3941 89062 408]}
    {:start-time "2021-01-09T00:00Z", :clone-durations-stats [1 2 4 19 83 81928 726 7605 84279 116]}
    {:start-time "2021-01-10T00:00Z", :clone-durations-stats [1 2 3 14 100 81528 626 7015 84571 135]}
    {:start-time "2021-01-11T00:00Z", :clone-durations-stats [0 3 7 26 139 51540 255 3442 57336 224]}]
  ;; updated 11.1.2021 after the whole day is complete?
      {:start-time "2021-01-11T00:00Z", :clone-durations-stats [1 4 6 17 136 55988 145 2629 65712 453]}
      {:start-time "2021-01-12T00:00Z", :clone-durations-stats [1 3 6 9 39 676 12 35 6859 538]}
      {:start-time "2021-01-13T00:00Z", :clone-durations-stats [0 5 8 31 43 57 16 15 2835 167]}]
      {:start-time "2021-01-13T00:00Z", :clone-durations-stats [1 5 7 18 41 142 14 16 6090 423]}
      {:start-time "2021-01-14T00:00Z", :clone-durations-stats [1 3 6 11 40 372 12 24 5155 399]}
      {:start-time "2021-01-15T00:00Z", :clone-durations-stats [1 3 6 11 41 334 12 21 7151 562]}
      {:start-time "2021-01-16T00:00Z", :clone-durations-stats [1 2 3 8 75 351 14 39 1638 110]}
      {:start-time "2021-01-17T00:00Z", :clone-durations-stats [1 2 5 11 63 340 17 40 1737 99]}
      {:start-time "2021-01-18T00:00Z", :clone-durations-stats [1 3 6 12 41 468 13 26 6211 465]}
      {:start-time "2021-01-19T00:00Z", :clone-durations-stats [1 4 6 12 42 463 13 24 7799 571]}

  (map vector [:min :perc25 :median :perc75 :perc95 :max :mean :standard-deviation :sum :count]
       [1 5 7 15 46 16397 52 776 23582 446])
;; => ([:min 1] [:perc25 5] [:median 7] [:perc75 15] [:perc95 46] [:max 16397] [:mean 52] [:standard-deviation 776] [:sum 23582] [:count 446])
;; => ([:min 1] [:perc25 5] [:median 7] [:perc75 12] [:perc95 37] [:max 348] [:mean 14] [:standard-deviation 22] [:sum 5662] [:count 403])

;; => [{:start-time "2020-12-15T00:00Z", :clone-durations-stats [1 5 7 12 37 348 14 22 5662 403]}
;;     {:start-time "2020-12-16T00:00Z", :clone-durations-stats [1 5 7 15 46 16397 52 776 23582 446]}
;;     {:start-time "2020-12-17T00:00Z", :clone-durations-stats [0 5 7 25 91 47781 130 2244 59263 453]}
;;     {:start-time "2020-12-18T00:00Z", :clone-durations-stats [1 4 6 8 33 355 10 20 4743 461]}
;;     {:start-time "2020-12-19T00:00Z", :clone-durations-stats [1 2 3 8 55 385 14 41 1525 108]}
;;     {:start-time "2020-12-20T00:00Z", :clone-durations-stats [1 2 6 17 65 330 17 36 1885 110]}
;;     {:start-time "2020-12-21T00:00Z", :clone-durations-stats [1 5 6 11 36 504 13 28 6487 469]}
;;     {:start-time "2020-12-22T00:00Z", :clone-durations-stats [1 5 6 10 33 348 10 20 5839 535]}
;;     {:start-time "2020-12-23T00:00Z", :clone-durations-stats [0 4 6 10 34 359 11 22 4977 418]}
;;     {:start-time "2020-12-24T00:00Z", :clone-durations-stats [1 4 6 11 44 287 14 25 2656 187]}
;;     {:start-time "2020-12-25T00:00Z", :clone-durations-stats [1 2 7 23 57 296 17 31 2317 132]}
;;     {:start-time "2020-12-26T00:00Z", :clone-durations-stats [1 2 5 9 65 324 15 38 1515 99]}
;;     {:start-time "2020-12-27T00:00Z", :clone-durations-stats [1 2 3 11 82 304 16 39 1306 80]}
;;     {:start-time "2020-12-28T00:00Z", :clone-durations-stats [1 3 5 9 40 345 12 27 3109 245]}
;;     {:start-time "2020-12-29T00:00Z", :clone-durations-stats [1 3 6 11 38 317 13 23 3686 282]}
;;     {:start-time "2020-12-30T00:00Z", :clone-durations-stats [0 3 7 18 55 519 18 44 4974 263]}
;;     {:start-time "2020-12-31T00:00Z", :clone-durations-stats [0 2 5 13 81 1154 33 139 4776 141]}
;;     {:start-time "2021-01-01T00:00Z", :clone-durations-stats [1 2 6 12 46 296 14 30 1927 137]}
;;     {:start-time "2021-01-02T00:00Z", :clone-durations-stats [1 2 5 15 84 353 19 46 1438 74]}
;;     {:start-time "2021-01-03T00:00Z", :clone-durations-stats [1 2 4 8 54 323 14 36 1511 107]}
;;     {:start-time "2021-01-04T00:00Z", :clone-durations-stats [1 3 6 10 40 627 19 66 8159 424]}
;;     {:start-time "2021-01-05T00:00Z", :clone-durations-stats [1 3 6 17 332 40818 118 1809 60445 510]}
;;     {:start-time "2021-01-06T00:00Z", :clone-durations-stats [1 3 6 25 169 85407 242 4182 101325 417]}
;;     {:start-time "2021-01-07T00:00Z", :clone-durations-stats [1 3 6 21 60 82945 229 4105 93835 408]}
;;     {:start-time "2021-01-08T00:00Z", :clone-durations-stats [1 3 5 19 101 79614 218 3941 89062 408]}
;;     {:start-time "2021-01-09T00:00Z", :clone-durations-stats [1 2 4 19 83 81928 726 7605 84279 116]}
;;     {:start-time "2021-01-10T00:00Z", :clone-durations-stats [1 2 3 14 100 81528 626 7015 84571 135]}
  ;; incomplete (today)
;;     {:start-time "2021-01-11T00:00Z", :clone-durations-stats [0 3 7 26 139 51540 255 3442 57336 224]}]

  ;; Calculate mean/median and 95% confidence interval


  (describe-durations-per-day multiple-days-data :delays "delay_seconds")
;; => [{:start-time "2020-12-01T00:00Z", :delays-stats [0 10 14 88 533 753 98 166 48054 488]}
;;     {:start-time "2020-12-02T00:00Z", :delays-stats [0 10 14 53 450 8540 88 384 50126 565]}
;;     {:start-time "2020-12-03T00:00Z", :delays-stats [0 10 14 53 351 745 66 123 34954 523]}
;;     {:start-time "2020-12-04T00:00Z", :delays-stats [4 11 17 100 488 701 93 148 44246 473]}
;;     {:start-time "2020-12-05T00:00Z", :delays-stats [0 11 49 119 537 668 97 145 11562 119]}
;;     {:start-time "2020-12-06T00:00Z", :delays-stats [5 12 50 124 405 584 92 125 9960 108]}
;;     {:start-time "2020-12-07T00:00Z", :delays-stats [4 10 14 70 342 1096 75 148 38566 513]}
;;     {:start-time "2020-12-08T00:00Z", :delays-stats [0 12 31 120 546 985 108 171 45685 420]}
;;     {:start-time "2020-12-09T00:00Z", :delays-stats [0 9 14 106 503 782 95 159 54254 571]}
;;     {:start-time "2020-12-10T00:00Z", :delays-stats [0 11 16 103 456 767 90 147 44030 487]}
;;     {:start-time "2020-12-11T00:00Z", :delays-stats [0 11 19 215 595 780 141 195 66802 471]}
;;     {:start-time "2020-12-12T00:00Z", :delays-stats [0 11 40 194 638 699 147 195 20205 137]}
;;     {:start-time "2020-12-13T00:00Z", :delays-stats [4 11 82 219 626 750 155 201 18841 121]}
;;     {:start-time "2020-12-14T00:00Z", :delays-stats [4 11 40 254 719 1874 181 284 88711 488]}
;;     {:start-time "2020-12-15T00:00Z", :delays-stats [3 12 48 239 671 1955 178 281 74698 418]}
;;     {:start-time "2020-12-16T00:00Z", :delays-stats [4 12 57 276 613 783 169 202 77955 459]}
;;     {:start-time "2020-12-17T00:00Z", :delays-stats [3 12 100 316 660 830 190 215 89168 469]}
;;     {:start-time "2020-12-18T00:00Z", :delays-stats [2 10 38 174 599 1078 138 199 65631 474]}
;;     {:start-time "2020-12-19T00:00Z", :delays-stats [5 13 47 143 633 780 133 186 14380 108]}
;;     {:start-time "2020-12-20T00:00Z", :delays-stats [0 12 50 134 516 577 110 144 13058 118]}
;;     {:start-time "2020-12-21T00:00Z", :delays-stats [0 11 45 242 609 854 155 204 75533 487]}
;;     {:start-time "2020-12-22T00:00Z", :delays-stats [0 11 48 369 619 782 190 223 38975 205]}]

  ;; first check batch job delays - the last field is count
  (describe-durations-per-day multiple-days-data :delays "delay_seconds")


;; => [{:start-time "2020-08-08T00:00Z", :delays-stats [5 12 27 63 573 1587 90 206 8606 95]}
;;     {:start-time "2020-08-09T00:00Z", :delays-stats [6 12 27 36 318 1586 72 188 6963 96]}
;;     {:start-time "2020-08-10T00:00Z", :delays-stats [0 10 14 70 465 1109 88 162 29611 335]}
;;     {:start-time "2020-08-11T00:00Z", :delays-stats [4 10 15 39 174 549 45 77 17052 375]}

  ;; then :delta-durations
  (describe-durations multiple-days-data :delta-durations)
  ;; 26.10.2020 - using my own stats functions
;; => {:min 19.0, :perc95 70.0, :mean 44.33704735376044, :standard-deviation 52.079017565494986, :median-confidence [33.574994502283 36.425005497717], :median 35.0, :max 656.0, :count 359, :perc25 29.0, :mean-confidence [38.93156619379294 49.74252851372794], :perc75 47.0, :sum 15917.0}
  ;; 26.10.2020 - using fastmath.stats/bootstrap-ci
  (->> multiple-days-data (mapcat :delta-durations) (mapv (fn [job-data] (Double/parseDouble (get job-data "duration_seconds"))))
       #_fastmath.stats/bootstrap-ci ;; => [38.036434540389955 49.57320334261837 44.33704735376044] (this is 98% confidence)
       (#(fastmath.stats/bootstrap-ci % 0.95 100 fastmath.stats/median)))   ;; => [34.0 37.0 35.0]


  ;; 7.10.2020:
  ;; => {:min 18.0, :perc95 77.0, :mean 41.1422287390029, :standard-deviation 22.398382172681043, :median-confidence [32.85124062839878 35.14875937160122], :median 34.0, :max 261.0, :count 682, :perc25 28.0, :mean-confidence [39.458217043083515 42.82624043492229], :perc75 48.0, :sum 28059.0}
  ;; Much older:
;; => {:min 0.0, :perc95 837.6999999999998, :mean 252.4925839188132, :standard-deviation 276.7040042324224, :median 121.5, :max 2572.0, :perc25 103.0, :perc75 266.25, :sum 646886.0}


  ;; [min p25 median p75 p95 max avg stdev sum count]
  (describe-durations-per-day multiple-days-data :delta-durations)
;; => [{:start-time "2021-03-31T00:00Z",
;;      :delta-durations-stats [21 44 75 142 272 3585 115 202 47934 414]}
;;     {:start-time "2021-04-01T00:00Z",
;;      :delta-durations-stats [19 44 74 134 278 7956 134 402 64201 478]}
;;     {:start-time "2021-04-02T00:00Z",
;;      :delta-durations-stats [23 42 76 141 291 1069 111 123 30152 271]}
;;     {:start-time "2021-04-03T00:00Z",
;;      :delta-durations-stats [28 65 117 236 1823 1853 275 478 6619 24]}
;;     {:start-time "2021-04-04T00:00Z", :delta-durations-stats [35 50 105 151 216 222 106 58 2561 24]}
;;     {:start-time "2021-04-05T00:00Z",
;;      :delta-durations-stats [23 45 78 129 254 1436 118 163 41069 348]}
;;     {:start-time "2021-04-06T00:00Z",
;;      :delta-durations-stats [22 45 70 130 275 1224 117 163 53455 455]}
;;     {:start-time "2021-04-07T00:00Z",
;;      :delta-durations-stats [22 44 71 139 918 1162 148 233 72035 485]}
;;     {:start-time "2021-04-08T00:00Z", :delta-durations-stats [42 55 82 193 690 877 152 167 3802 25]}]

;; => [{:start-time "2020-08-10T00:00Z",
;;      :delta-durations-stats [0 102 119 334 975 1564 266 291 69631 261]}
;;     {:start-time "2020-08-11T00:00Z",
;;      :delta-durations-stats [83 97 117 154 284 539 141 73 41655 294]}
;;     {:start-time "2020-08-12T00:00Z",
;;      :delta-durations-stats [83 93 113 142 478 779 151 112 36592 241]}
;;     {:start-time "2020-08-13T00:00Z",
;;      :delta-durations-stats [21 45 88 107 192 569 91 62 21135 232]}
;;     {:start-time "2020-08-14T00:00Z",
;;      :delta-durations-stats [19 32 39 67 180 1387 68 109 17475 255]}
;;     {:start-time "2020-08-15T00:00Z",
;;      :delta-durations-stats [26 31 56 67 213 213 65 52 717 11]}
;;     {:start-time "2020-08-16T00:00Z",
;;      :delta-durations-stats [19 37 62 93 114 115 67 30 1349 20]}
;;     {:start-time "2020-08-17T00:00Z",
;;      :delta-durations-stats [19 28 37 53 82 116 42 18 4208 100]}]
  ;; => [{:start-time "2020-08-21T00:00Z", :delta-durations-stats [21 31 40 61 104 605 59 75 12984 217]}
  ;;     {:start-time "2020-08-22T00:00Z", :delta-durations-stats [25 33 43 57 87 107 45 17 1372 30]}
  ;;     {:start-time "2020-08-23T00:00Z", :delta-durations-stats [27 30 53 59 81 81 48 16 577 12]}
  ;;     {:start-time "2020-08-24T00:00Z", :delta-durations-stats [25 34 62 75 253 272 65 51 1377 21]}]

;; => [{:start-time "2020-07-28T00:00Z", :delta-durations-stats [78 102 121 219 639 958 209 181 44061]}
;;     {:start-time "2020-07-29T00:00Z", :delta-durations-stats [77 103 120 194 697 1059 213 202 43187]}
;;     {:start-time "2020-07-30T00:00Z", :delta-durations-stats [80 102 114 223 837 1539 237 257 61242]}
;;     {:start-time "2020-07-31T00:00Z", :delta-durations-stats [78 101 120 217 679 1023 213 203 45564]}
;;     {:start-time "2020-08-01T00:00Z", :delta-durations-stats [90 103 115 185 751 811 198 185 5347]}
;;     {:start-time "2020-08-02T00:00Z", :delta-durations-stats [98 103 119 489 1967 1967 321 446 5783]}
;;     {:start-time "2020-08-03T00:00Z", :delta-durations-stats [77 103 121 203 605 956 193 164 44890]}
;;     {:start-time "2020-08-04T00:00Z", :delta-durations-stats [84 104 141 419 1016 1619 294 305 69453]}
;;     {:start-time "2020-08-05T00:00Z", :delta-durations-stats [85 105 143 525 1184 2087 354 385 87260]}
;;     {:start-time "2020-08-06T00:00Z", :delta-durations-stats [76 102 115 233 716 2075 239 276 83183]}
;;     {:start-time "2020-08-07T00:00Z", :delta-durations-stats [82 106 131 398 1114 2572 301 346 76947]}
;;     {:start-time "2020-08-08T00:00Z", :delta-durations-stats [103 111 122 131 576 576 164 138 3134]}
;;     {:start-time "2020-08-09T00:00Z", :delta-durations-stats [100 103 118 469 549 549 244 186 4151]}
;;     {:start-time "2020-08-10T00:00Z", :delta-durations-stats [0 102 119 334 975 1564 266 291 69631]}
;;     {:start-time "2020-08-11T00:00Z", :delta-durations-stats [83 97 117 154 284 539 141 73 41655 294]}
;;     {:start-time "2020-08-12T00:00Z", :delta-durations-stats [83 93 113 142 478 779 151 112 36592 241]}
;;     {:start-time "2020-08-13T00:00Z", :delta-durations-stats [21 45 88 107 192 569 91 62 21135 232]}


  ;; git clone durations are more interesting - this if for ALL analyses

  (describe-durations multiple-days-data :clone-durations)
;; => {:min 0.0, :perc95 492.03024999999997, :mean 79.47033029801325, :standard-deviation 189.37814098852846, :median 19.066000000000003, :max 1784.198, :perc25 6.24275, :perc75 27.712, :sum 288000.47699999984}

  (describe-durations-per-day multiple-days-data :clone-durations)

;; => [{:start-time "2020-07-28T00:00Z", :clone-durations-stats [1 3 13 24 381 790 52 133 18307]}
;;     {:start-time "2020-07-29T00:00Z", :clone-durations-stats [1 4 15 24 420 974 55 140 18902]}
;;     {:start-time "2020-07-30T00:00Z", :clone-durations-stats [1 3 16 24 517 1153 72 179 29090]}
;;     {:start-time "2020-07-31T00:00Z", :clone-durations-stats [1 3 14 24 415 776 61 139 20225]}
;;     {:start-time "2020-08-01T00:00Z", :clone-durations-stats [1 1 2 9 40 422 18 63 3212]}
;;     {:start-time "2020-08-02T00:00Z", :clone-durations-stats [0 2 3 7 173 449 24 79 4285]}
;;     {:start-time "2020-08-03T00:00Z", :clone-durations-stats [0 4 12 23 69 1557 35 119 13409]}
;;     {:start-time "2020-08-04T00:00Z", :clone-durations-stats [1 4 18 25 508 1377 92 205 33646]}

;;     {:start-time "2020-08-06T00:00Z", :clone-durations-stats [1 5 18 24 493 1625 73 193 34197]}
;;     {:start-time "2020-08-07T00:00Z", :clone-durations-stats [1 3 18 28 733 1784 100 253 38210]}
;;     {:start-time "2020-08-08T00:00Z", :clone-durations-stats [1 2 3 10 29 442 15 52 2262]}
;;     {:start-time "2020-08-09T00:00Z", :clone-durations-stats [0 2 2 6 75 355 19 65 3573]}
;;     {:start-time "2020-08-10T00:00Z", :clone-durations-stats [0 6 19 395 557 1158 157 237 74718]}
;;     {:start-time "2020-08-11T00:00Z", :clone-durations-stats [1 3 5 10 35 357 12 28 4614 372]}

  ;; git clone durations - ONLY delta analyses
  ;; - WARNING: git clone bottleneck is moved to "git fetch" when using the "optimized git clone"
  (describe-durations (filter-job-type "run-delta-analysis" multiple-days-data)
                      :clone-durations)
  ;; => {:min 0.0, :perc95 554.597, :mean 101.97272610222394, :standard-deviation 212.08479813014006, :median 21.111, :max 1784.198, :perc25 14.757, :perc75 30.792, :sum 261356.0969999999}

  (-> (filter-job-type "run-delta-analysis" multiple-days-data)
      (describe-durations-per-day :clone-durations))
;; => [{:start-time "2020-07-28T00:00Z", :clone-durations-stats [2 13 20 29 540 741 77 158 16303]}
;;     {:start-time "2020-07-29T00:00Z", :clone-durations-stats [2 14 20 27 517 974 84 172 17010]}
;;     {:start-time "2020-07-30T00:00Z", :clone-durations-stats [1 14 21 28 569 1153 107 215 27613]}
;;     {:start-time "2020-07-31T00:00Z", :clone-durations-stats [2 13 20 32 470 776 89 163 18963]}
;;     {:start-time "2020-08-01T00:00Z", :clone-durations-stats [2 11 15 26 402 422 71 132 1917]}
;;     {:start-time "2020-08-02T00:00Z", :clone-durations-stats [19 20 23 395 449 449 147 184 2662]}
;;     {:start-time "2020-08-03T00:00Z", :clone-durations-stats [2 12 20 26 368 733 45 112 10670]}
;;     {:start-time "2020-08-04T00:00Z", :clone-durations-stats [2 18 22 39 583 1377 129 232 30521]}
;;     {:start-time "2020-08-05T00:00Z", :clone-durations-stats [3 18 22 35 739 1165 130 248 31998]}
;;     {:start-time "2020-08-06T00:00Z", :clone-durations-stats [2 13 20 27 573 1625 95 218 32977]}
;;     {:start-time "2020-08-07T00:00Z", :clone-durations-stats [1 18 22 48 970 1784 144 297 36857]}
;;     {:start-time "2020-08-08T00:00Z", :clone-durations-stats [18 20 22 29 442 442 60 113 1158]}
;;     {:start-time "2020-08-09T00:00Z", :clone-durations-stats [0 19 22 333 355 355 126 156 2272]}
;;     {:start-time "2020-08-10T00:00Z", :clone-durations-stats [0 14 20 33 612 1158 114 222 29819]}
;;     {:start-time "2020-08-11T00:00Z", :clone-durations-stats [1 4 5 10 33 357 11 22 3248 294]}

  ;; compare to full analyses
  (-> (filter-job-type "run-analysis" multiple-days-data)
      (describe-durations-per-day :clone-durations))
;; => [{:start-time "2020-07-28T00:00Z", :clone-durations-stats [1 2 4 15 73 804 25 92 1957]}
;;     {:start-time "2020-07-29T00:00Z", :clone-durations-stats [1 2 6 25 65 457 22 56 1723]}
;;     {:start-time "2020-07-30T00:00Z", :clone-durations-stats [1 2 3 10 78 346 17 45 1333]}
;;     {:start-time "2020-07-31T00:00Z", :clone-durations-stats [1 2 3 11 71 530 19 69 1266]}
;;     {:start-time "2020-08-01T00:00Z", :clone-durations-stats [1 2 3 12 68 386 18 50 1280]}
;;     {:start-time "2020-08-02T00:00Z", :clone-durations-stats [0 2 4 11 163 380 22 58 1622]}
;;     {:start-time "2020-08-03T00:00Z", :clone-durations-stats [0 2 5 17 75 1568 35 179 2721]}
;;     {:start-time "2020-08-04T00:00Z", :clone-durations-stats [1 2 5 14 157 1036 41 154 2735]}
;;     {:start-time "2020-08-05T00:00Z", :clone-durations-stats [1 2 4 13 184 1105 45 177 2926]}
;;     {:start-time "2020-08-06T00:00Z", :clone-durations-stats [1 2 3 8 70 472 18 62 1224]}
;;     {:start-time "2020-08-07T00:00Z", :clone-durations-stats [1 1 3 8 72 603 20 77 1345]}
;;     {:start-time "2020-08-08T00:00Z", :clone-durations-stats [1 2 3 11 64 321 15 43 1080]}
;;     {:start-time "2020-08-09T00:00Z", :clone-durations-stats [1 2 3 11 98 329 18 46 1294]}
;;     {:start-time "2020-08-10T00:00Z", :clone-durations-stats [0 2 4 14 127 920 37 139 2583]}

  ;; examine long durations
  (->> multiple-days-data
       (mapcat :delta-durations)
       (filter (fn [{:strs [duration_seconds] :as dd}]
                 (< 1500 (Double/parseDouble duration_seconds))))
       (mapv (fn [job]
               (-> job
                   (update "submitted_at" to-date)
                   (update "finished_at" to-date))
               )))
;; => [{"job_id" "38689",
;;      "job_type" ":run-delta-analysis",
;;      "batch_job_id" "e471ba88-94e0-4b82-a7ac-0e896f360307",
;;      "status" ":success",
;;      "submitted_at" #inst "2020-06-25T15:09:10.000-00:00",
;;      "finished_at" #inst "2020-06-25T15:36:56.000-00:00",
;;      "duration_seconds" "1666"}]


  (->> multiple-days-data
       (mapcat :clones)
       (filter (fn [{:strs [duration_seconds] :as dd}]
                 (< 300 (Double/parseDouble duration_seconds))))
       (mapv (fn [job]
               (-> job
                   (update "clone_start" to-date)
                   (update "clone_finished" to-date))
               )))



  ;;
  )

(comment

  (def commits-query
    {:group-name "/aws/batch/job"
     :query "fields @timestamp, @message
  | filter (@message like /:commits \\[.+:repo \"/ and @logStream like /codescene-prod/)
  | parse /:commits (?<commits>\\[[^\\]]+\\]).* :repo \"(?<repository>[^\"]+)/
  | display @timestamp, repository, commits
  | limit 10000"})
  (def commits-query-id (start-query
                         commits-query
                         {:start-time (date-time 2020 7 1) :end-time (date-time 2020 7 14)}))
  (def commits-results (get-query-results commits-query-id))

  (def sorted-commits-results
    (->> commits-results
         (map (fn [row] (-> (let [with-commits (-> row
                                                   (update  "commits"
                                                            ;; one weird occurence in log data didn't have "commits" and "repository" key (not sure why)
                                                            (fnil read-string "[]"))
                                                   (dissoc "@ptr"))]
                              (into (sorted-map) (assoc with-commits "commits-count" (count (get with-commits "commits"))))))))
         (sort-by (fn [row] (- (count (get row "commits")))))))

  (defn- repo-name [commits-data]
    (StringUtils/substringAfterLast (get commits-data "repository")
                                    "/"))
  (mapv (juxt repo-name #(% "commits-count"))
        sorted-commits-results)


  (def commits-by-repository (group-by repo-name
                                       sorted-commits-results))

  (oz/view! (hist "Commits per delta analysis" sorted-commits-results "commits-count")))





