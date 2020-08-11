(ns clojure-experiments.aws.logs
  "Utility functions to load data from cloudwatch insights logs.
  Be mindful about concurrency limits - if you reach the limit you'll get an error:
      \"LimitExceededException\", :message \"Account maximum query concurrency limit of [10] reached...

  Resources:
  - Cloudwatch Insights query syntax: https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/CWL_QuerySyntax.html
  - aws-api: https://github.com/cognitect-labsuaws-api"
  (:require [clojure-experiments.concurrency :refer [map-throttled]]
            [clojure.set :as set]
            [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [clojure-experiments.visualizations.oz :as my-oz]
            [oz.core :as oz]
            [clojure.spec.alpha :as s]
            [clojure-experiments.stats.descriptive :as stat])
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
       (println "Getting results for query: " query-id)
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
        :clones (poll-results clone-id)})
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
  | parse /^.*(Cloning|Successfully cloned) (?<repo_url>\\S+) to \\/var\\/codescene\\/repos\\/(?<job_id>\\d+)\\/[^\\/]+\\/repos\\/(?<repo_name>\\S+).*$/
  | stats earliest(@timestamp) as clone_start, 
        latest(@timestamp) as clone_finished, 
        (clone_finished - clone_start)/1000 as duration_seconds
        by job_id, repo_name
  | sort duration_seconds desc
  | limit 10000"})

;; could be useful to use Histogram + color: https://youtu.be/9uaHRWj04D4?t=439
(defn hist
  ([data field-name]
   (hist nil data field-name))
  ([chart-title data field-name]
   (hist chart-title data field-name nil))
  ([chart-title data field-name opts]
   (my-oz/histogram data
                    field-name
                    (merge {:step 10
                            #_#_:scale "symlog"
                            :width 1200
                            :height 600}
                           (when chart-title {:title chart-title})
                           opts))))

;; E.g. distinguishing multiple job types (delta, full analysis, x-ray, project delete)
;; via color: https://vega.github.io/vega-lite/docs/bar.html#stack
(defn- color [field-name]
  {:color {:field field-name :type "nominal"}})

(defn- to-date [epoch-millis-str]
  (java.util.Date. (Long/parseLong epoch-millis-str)))

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
  (oz/view! (hist clones "duration_seconds" (color "repo_name")))


  ;;; show multiple days data at once via Hiccup: https://github.com/metasoarous/oz#hiccup


  (do
    (time (def multiple-days-data (get-all-data jobs-delay-query
                                                delta-jobs-durations-query
                                                other-jobs-durations-query
                                                git-clones-query
                                                (from-to (truncate-to-midnight (.minusDays (now)
                                                                                           3))))))

    ;; TODO: use Vega Lite's combinators: https://youtu.be/9uaHRWj04D4?t=572
    ;; (facet row, vconcat, layer, repeat row)
    (do 
      (def multiple-days-data-histograms
        [:div
         ;; this must be a lazy seq, not a vector otherwise an 'Invalid arity' error is thrown in oz.js
         (for [{:keys [start-time end-time delays delta-durations other-durations clones]} multiple-days-data]
           [:div
            [:p [:b (format "%s -- %s" start-time end-time)]]
            [:div {:style {:display "flex" :flex-direction "col"}}
             [:vega-lite (hist "Batch jobs delays in seconds" delays "delay_seconds" (color "job_type"))]
             [:vega-lite (hist "Other jobs total durations in seconds" other-durations "duration_seconds")]
             [:vega-lite (hist "Delta jobs total durations in seconds" delta-durations "duration_seconds")]
             ;; boxplot is confusing => don't show it
             #_[:vega-lite (my-oz/boxplot delta-durations "duration_seconds"
                                          {:extent 10.0})]
             ;; TODO: having many different repos make the chart less readable and bigger -> perhaps use separate visualization?
             [:vega-lite (hist "Git clones durations" clones "duration_seconds" #_(color "repo_name"))]]
            [:hr]])])

      (oz/view! multiple-days-data-histograms)))

  ;; descriptive statistics for all delta durations
  (let [durations-seconds (->> multiple-days-data
                               (mapcat :delta-durations)
                               (mapv (fn [{:strs [duration_seconds]}]
                                       (Double/parseDouble duration_seconds))))]
    (stat/describe durations-seconds))
;; => {:min 0.0,
;;     :perc99 1372.1500000000024,
;;     :perc95 860.75,
;;     :mean 252.76114649681574,
;;     :standard-deviation 274.44589011424137,
;;     :median 119.0,
;;     :max 1564.0,
;;     :perc25 103.0,
;;     :perc75 287.5,
;;     :sum 79367.0}


  ;; descriptive statistics for delta durations per day
  (let [with-stats (mapv (fn [day-data]
                           (let [with-durations-as-doubles
                                 (-> day-data
                                     (update :delta-durations
                                             (fn [durations]
                                               (mapv (fn [{:strs [duration_seconds]}] (Double/parseDouble duration_seconds)) durations)))
                                     (update :start-time str))]
                             (assoc with-durations-as-doubles
                                    :delta-durations-stats
                                    (stat/describe-as-vector stat/describe-as-ints
                                                             (:delta-durations with-durations-as-doubles)))))
                         multiple-days-data)]
    (mapv #(select-keys % [:start-time :delta-durations-stats])
          with-stats))
;; => [{:start-time "2020-08-01T00:00Z", :delta-durations-stats [90 103 115 185 751 811 198 185 5347]}
;;     {:start-time "2020-08-02T00:00Z", :delta-durations-stats [98 103 119 489 1967 1967 321 446 5783]}
;;     {:start-time "2020-08-03T00:00Z", :delta-durations-stats [77 103 121 203 605 956 193 164 44890]}
;;     {:start-time "2020-08-04T00:00Z", :delta-durations-stats [84 104 141 419 1016 1619 294 305 69453]}
;;     {:start-time "2020-08-05T00:00Z", :delta-durations-stats [85 105 143 525 1184 2087 354 385 87260]}
;;     {:start-time "2020-08-06T00:00Z", :delta-durations-stats [76 102 115 233 716 2075 239 276 83183]}
;;     {:start-time "2020-08-07T00:00Z", :delta-durations-stats [82 106 131 398 1114 2572 301 346 76947]}
;;     {:start-time "2020-08-08T00:00Z", :delta-durations-stats [103 111 122 131 576 576 164 138 3134]}
;;     {:start-time "2020-08-09T00:00Z", :delta-durations-stats [100 103 118 469 549 549 244 186 4151]}
;;     {:start-time "2020-08-10T00:00Z", :delta-durations-stats [0 102 119 334 975 1564 266 291 69631]}
;;     {:start-time "2020-08-11T00:00Z", :delta-durations-stats [83 97 123 136 476 476 144 93 2451]}]

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





