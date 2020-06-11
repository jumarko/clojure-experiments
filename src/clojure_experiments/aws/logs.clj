(ns clojure-experiments.aws.logs
  "Utility functions to load data from cloudwatch insights logs.
  Be mindful about concurrency limits - if you reach the limit you'll get an error:
      \"LimitExceededException\", :message \"Account maximum query concurrency limit of [10] reached..."
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
                 (mapv :value %)))
       ;; records without job_id are incomplete and cannot be used
       ;; - should work for both queries
       (filterv (fn [{:strs [job_id status] :as _row}]
                  (and job_id
                      (not (#{":running" "submitted"}
                            status)))))))

(defn get-query-results [query-id]
  (println "DEBUG: get-query-results: " query-id)
  (let [{:keys [status results]} (invoke-op
                                  #(aws/invoke logs {:op :GetQueryResults
                                                     :request {:queryId query-id}}))]
    (if (#{"Scheduled" "Running"} status)
      :incomplete
      (results->map results))))

(defn poll-results [query-id]
  (let [max-polling-time 10000
        polling-start (System/currentTimeMillis)]
    (loop []
      (println "Getting results for query: " query-id)
      (let [results (get-query-results query-id)
            elapsed-time (- (System/currentTimeMillis) polling-start)]
        (cond
          (> elapsed-time  max-polling-time)
          ;; can be either :incomplete or real results
          results

          (= results :incomplete)
          (do (Thread/sleep 1000)
              (recur))

          :else results)))))

(defn from-to
  "Returns pairs (2-element vectors) of all 1-day periods from given 'from' day to given 'to' day.
  The input dates will be truncated to a day precision (via LocalDate).)"
  [from-day to-day]
  (let [period (java.time.Period/between (.toLocalDate from-day) (.toLocalDate to-day))
        days-between (.getDays period)]
    (map
     (fn [plus-days]
       [(.plusDays from-day plus-days)
        (.plusDays from-day (inc plus-days))])
     (range days-between))))
(from-to (date-time 2020 6 1) (date-time 2020 6 5))
;; => [[#object[java.time.ZonedDateTime 0x6cf1446b "2020-06-01T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x2e4773ec "2020-06-02T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0x51bf09e4 "2020-06-02T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x62b6da26 "2020-06-03T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0xd40c52f "2020-06-03T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x2ff3106a "2020-06-04T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0x242ab838 "2020-06-04T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x2b5f8c0d "2020-06-05T00:00Z"]]]

;; even if you specify time beyond mindnight you still only get the whole days:
(from-to (date-time 2020 6 1) (date-time 2020 6 3 8 0 0))
;; => [[#object[java.time.ZonedDateTime 0x5eff54d6 "2020-06-01T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x6b4f6547 "2020-06-02T00:00Z"]]
;;     [#object[java.time.ZonedDateTime 0x3f68325 "2020-06-02T00:00Z"]
;;      #object[java.time.ZonedDateTime 0x7ec7e55b "2020-06-03T00:00Z"]]]

(defn get-all-data [delay-query duration-query from-day to-day]
  (let [started-queries (map-throttled
                         (dec (quot max-concurrent-queries 2)) ;; two different queries per day and decrement to have some space
                         (fn [[start-time end-time]]
                           (let [delay-query-id (start-query delay-query {:start-time start-time
                                                                          :end-time end-time})
                                 duration-query-id (start-query duration-query {:start-time start-time
                                                                                :end-time end-time})]
                             {:start-time start-time
                              :end-time end-time
                              :delay-id delay-query-id
                              :duration-id duration-query-id}))
                         (from-to from-day to-day))]
    (mapv
     (fn [{:keys [delay-id duration-id start-time end-time]}]
       {:start-time start-time
        :end-time end-time
        :delays (poll-results delay-id)
        :durations (poll-results duration-id)})
     started-queries)))

(def jobs-delay-query "fields @timestamp, @message
| filter @message like /batch-job-id=/
| parse /^.*job-id=(?<job_id>\\d+) job-type=(?<job_type>\\S+) job-status=:?(?<job_status>(submitted|running)).*batch-job-id=(?<batch_job_id>\\S+).*$/
| stats earliest(@timestamp) as submitted_at, 
        latest(@timestamp) as started_at, 
        (started_at - submitted_at)/1000 as delay_seconds
        by job_id, job_type, batch_job_id
| sort delay_seconds desc
| limit 1000")

(def delta-jobs-durations-query "fields @timestamp, @message
| filter @message like /batch-job-id=/
| parse /^.*job-id=(?<job_id>\\d+) job-type=(?<job_type>\\S+) job-status=(?<job_status>\\S+).*batch-job-id=(?<batch_job_id>\\S+).*$/
| filter job_type = ':run-delta-analysis'
| stats latest(job_status) as status,
        earliest(@timestamp) as submitted_at, 
        latest(@timestamp) as finished_at, 
        (finished_at - submitted_at)/1000 as duration_seconds
        by job_id, job_type, batch_job_id
| sort duration_seconds desc")

(def my-log-group "codescene-web-prod-application")

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

(comment

  (def delays-query-id (start-query {:group-name my-log-group :query jobs-delay-query}
                                    {:start-time (date-time 2020 6 4) :end-time (date-time 2020 6 5)}))
  (def delays (get-query-results delays-query-id))
  (oz/view! (hist delays "delay_seconds"))

  ;; show multiple days data at once via Hiccup: https://github.com/metasoarous/oz#hiccup

  (def my-from  (date-time 2020 6 1))
  (def my-to (date-time 2020 6 11))
  (def multiple-days-data (get-all-data {:group-name my-log-group :query jobs-delay-query}
                                        {:group-name my-log-group :query delta-jobs-durations-query}
                                        my-from my-to))

  ;; TODO: use Vega Lite's combinators: https://youtu.be/9uaHRWj04D4?t=572
  ;; (facet row, vconcat, layer, repeat row)
  (def multiple-days-data-histograms
    [:div
     ;; this must be a lazy seq, not a vector otherwise an 'Invalid arity' error is thrown in oz.js
     (for [{:keys [start-time end-time delays durations]} multiple-days-data]
       [:div
        [:p [:b (format "%s -- %s" start-time end-time)]]
        [:div {:style {:display "flex" :flex-direction "col"}}
         [:vega-lite (hist "Batch jobs delays in seconds" delays "delay_seconds"
                           ;; distinguishing multiple job types (delta, full analysis, x-ray, project delete)
                           ;; via color: https://vega.github.io/vega-lite/docs/bar.html#stack
                           {:color {:field "job_type" :type "nominal"}})]
         [:vega-lite (hist "Delta jobs total durations in seconds" durations "duration_seconds")]]
        [:hr]])])

  (oz/view! multiple-days-data-histograms)

  ;;
  )


  
