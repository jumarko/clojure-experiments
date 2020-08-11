(ns clojure-experiments.stats.descriptive
  "Little helpers for computing descriptive statistics (min, max, median, mean, percentiles, ...)"
  (:import (org.apache.commons.math3.stat.descriptive DescriptiveStatistics))
  (:require [clojure.spec.alpha :as s]))

(s/fdef describe
  :args (s/cat :data (s/every double?))
  :ret (s/keys :req-un [::min ::perc25 ::median ::perc75 ::perc95 ::perc99 ::max ::mean ::standard-deviation ::sum]))
(defn describe
  "Given a sequence/collection of numbers (doubles!)
  returns a map of descriptive statistics. "
  [data]
  (let [descriptive-stats (DescriptiveStatistics.)
        perc-fn #(.getPercentile descriptive-stats %)]
    (doseq [d data]
      (.addValue descriptive-stats d))
    (-> (bean descriptive-stats)
        (assoc :standard-deviation (.getStandardDeviation descriptive-stats))
        (assoc :perc25 (perc-fn 25)
               :median (perc-fn 50)
               :perc75 (perc-fn 75)
               :perc95 (perc-fn 95)
               :perc99 (perc-fn 99))
        (select-keys [:min :perc25 :median :perc75 :perc95 :perc99 :max :mean :standard-deviation :sum]))))

(defn describe-as-vector
  "Same as `describe` but returns a simple vector of statistics values preserving order
  for an easy comparison between multiple sets of statistics."
  [data]
  (->> data
       describe
       (juxt :min :perc25 :median :perc75 :perc95 :perc99 :max :mean :standard-deviation :sum)))

