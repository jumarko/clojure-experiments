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
            [clojure.spec.alpha :as s]))

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
  [delay-query duration-query git-clone-query from-to-intervals]
  (let [started-queries (map-throttled
                         (dec (quot max-concurrent-queries 3)) ;; three different queries per day and decrement to have some space
                         (fn [[start-time end-time]]
                           (let [delay-query-id (start-query delay-query {:start-time start-time
                                                                          :end-time end-time})
                                 duration-query-id (start-query duration-query {:start-time start-time
                                                                                :end-time end-time})
                                 clone-query-id (start-query git-clone-query {:start-time start-time
                                                                              :end-time end-time})]
                             {:start-time start-time
                              :end-time end-time
                              :delay-id delay-query-id
                              :duration-id duration-query-id
                              :clone-id clone-query-id}))
                         from-to-intervals)]
    (mapv
     (fn [{:keys [delay-id duration-id clone-id start-time end-time]}]
       {:start-time start-time
        :end-time end-time
        :delays (poll-results delay-id true)
        :durations (poll-results duration-id true)
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

(def git-clones-query
  {:group-name "/aws/batch/job"
   :query "fields @timestamp, @message
  | filter @message like /(Cloning|Successfully cloned)/
  | parse /^.*(Cloning|Successfully cloned) (?<repo_url>\\S+) to \\/var\\/codescene\\/repos\\/(?<job_id>\\d+)\\/[^\\/]+\\/repos\\/(?<repo_name>\\S+).*$/
  | stats earliest(@timestamp) as clone_start, 
        latest(@timestamp) as clone_finished, 
        (clone_finished - clone_start)/1000 as duration_seconds
        by job_id, repo_name
  | sort duration_seconds desc
  | limit 10000"})

;; TODO: could be useful to use Histogram + color: https://youtu.be/9uaHRWj04D4?t=439
;; 
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


  (time
   (do
     (def multiple-days-data (get-all-data jobs-delay-query
                                           delta-jobs-durations-query
                                           git-clones-query
                                           (from-to (truncate-to-midnight (.minusDays (now)
                                                                                      3)))))

     ;; TODO: use Vega Lite's combinators: https://youtu.be/9uaHRWj04D4?t=572
     ;; (facet row, vconcat, layer, repeat row)
     (def multiple-days-data-histograms
       [:div
        ;; this must be a lazy seq, not a vector otherwise an 'Invalid arity' error is thrown in oz.js
        (for [{:keys [start-time end-time delays durations clones]} multiple-days-data]
          [:div
           [:p [:b (format "%s -- %s" start-time end-time)]]
           [:div {:style {:display "flex" :flex-direction "col"}}
            [:vega-lite (hist "Delta jobs total durations in seconds" durations "duration_seconds")]
            [:vega-lite (hist "Batch jobs delays in seconds" delays "delay_seconds" (color "job_type"))]
            ;; TODO: having many different repos make the chart less readable and bigger -> perhaps use separate visualization?
            [:vega-lite (hist "Git clones durations" clones "duration_seconds" (color "repo_name"))]]
           [:hr]])])

     (oz/view! multiple-days-data-histograms)))

  ;;
  )


  
