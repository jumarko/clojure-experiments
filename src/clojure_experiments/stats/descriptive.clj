(ns clojure-experiments.stats.descriptive
  "Little helpers for computing descriptive statistics (min, max, median, mean, percentiles, ...)"
  (:require [clojure.spec.alpha :as s]
            [medley.core :as m])
  (:import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics))

(def selected-keys [:min :perc25 :median :perc75 :perc95 :max :mean :standard-deviation :sum :count])

(s/fdef describe
  :args (s/cat :data (s/every double?))
  :ret (s/keys :req-un [::min ::perc25 ::median ::perc75 ::perc95 ::max ::mean ::standard-deviation ::sum ::count]))
(defn describe
  "Given a sequence/collection of numbers (doubles!)
  returns a map of descriptive statistics. "
  [data]
  (let [descriptive-stats (DescriptiveStatistics.)
        perc-fn #(.getPercentile descriptive-stats %)]
    (doseq [d data]
      (.addValue descriptive-stats d))
    (-> (bean descriptive-stats)
        (assoc :standard-deviation (.getStandardDeviation descriptive-stats)
               :count (.getN descriptive-stats))
        (assoc :perc25 (perc-fn 25)
               :median (perc-fn 50)
               :perc75 (perc-fn 75)
               :perc95 (perc-fn 95))
        (select-keys selected-keys))))

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

