(ns clojure-experiments.stats.descriptive
  "Little helpers for computing descriptive statistics (min, max, median, mean, percentiles, ...)."
  (:require [clojure-experiments.stats.confidence-intervals :refer [confidence-interval]]
            [clojure.spec.alpha :as s]
            [medley.core :as m])
  (:import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics))

(def selected-keys [:min :max :mean :standard-deviation :sum :count])

(s/fdef describe
  :args (s/cat :data (s/every double?))
  :ret (s/keys :req-un [::min ::perc25 ::median ::perc75 ::perc95 ::max ::mean ::standard-deviation ::sum ::count]))
(defn describe
  "Given a sequence/collection of numbers (doubles!)
  returns a map of descriptive statistics. "
  [data]
  (let [descriptive-stats (DescriptiveStatistics.)
        perc-fn #(.getPercentile descriptive-stats %)
        _ (doseq [d data]
            (.addValue descriptive-stats d))
        desc-stats (-> (bean descriptive-stats)
                       (assoc :standard-deviation (.getStandardDeviation descriptive-stats)
                              :count (.getN descriptive-stats))
                       (select-keys selected-keys)
                       (assoc :perc25 (perc-fn 25)
                              :median (perc-fn 50)
                              :perc75 (perc-fn 75)
                              :perc95 (perc-fn 95)))
        ;; hardcoded confidence level 95% is enough right now
        confidence-level 0.95
        stats-with-confidence-intervals (assoc desc-stats
                                               :median-confidence (confidence-interval :median confidence-level desc-stats)
                                               :mean-confidence (confidence-interval :mean confidence-level desc-stats))]
    stats-with-confidence-intervals))
;; sample describe output (operation durations in seconds)
;; => {:min 18.0, :perc95 77.0, :mean 41.1422287390029, :standard-deviation 22.398382172681043, :median-confidence [32.85124062839878 35.14875937160122], :median 34.0, :max 261.0, :count 682, :perc25 28.0, :mean-confidence [39.458217043083515 42.82624043492229], :perc75 48.0, :sum 28059.0}

(defn describe-as-ints [data]
  (m/map-vals int (describe data)))

(defn describe-as-vector
  "Same as `describe` but returns a simple vector of statistics values preserving order
  for an easy comparison between multiple sets of statistics.
  Returns these stats in order: min, perc25, median, perc75, perc95, max, mean, stdev, sum"
  ([data]
   (describe-as-vector describe data))
  ([describe-fn data]
   (->> (describe-fn data)
        ((apply juxt selected-keys)))))

