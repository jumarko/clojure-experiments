(ns clojure-experiments.stats.descriptive
  "Little helpers for computing descriptive statistics (min, max, median, mean, percentiles, ...).

  Commons math distributions guide: https://commons.apache.org/proper/commons-math/userguide/distribution.html
  "
  (:require [clojure.spec.alpha :as s]
            [medley.core :as m])
  (:import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
           org.apache.commons.math3.distribution.TDistribution))

(def selected-keys [:min :perc25 :median :perc75 :perc95 :max :mean :standard-deviation :sum :count])

(def conf-int nil) ; to allow easy redefinitions
(defmulti conf-int (fn [type level stats] type))

(defmethod conf-int :median [_type level {:keys [perc25 perc75 median count] :as stats}]
  {:pre [(number? median) (number? count) (number? perc25) (number? perc75)]}
  (let [iqr (- perc75 perc25) ; is it always correct to subtract perc25 from perc75?
        margin-of-error (* 1.5 (/ iqr (Math/sqrt count)))]
    [(- median margin-of-error)
     (+ median margin-of-error)]))


;; See Commons math distributions guide: https://commons.apache.org/proper/commons-math/userguide/distribution.html
;; also https://www.statisticshowto.com/tables/t-distribution-table/
(defn- confidence-level->cumulative-probability [confidence-level]
  (assert (<= 0.0 confidence-level 1.0))
  (+ confidence-level
     (/ (- 1.0 confidence-level) 2)))
#_(confidence-level->cumulative-probability 0.95)
;; => 0.975

(defn t-value [confidence-level count]
  ;; confidence level implies cumulative probability that is twice as high
  (let [cummulative-probability (confidence-level->cumulative-probability confidence-level)]
    (-> (TDistribution. (dec count))
        (.inverseCumulativeProbability cummulative-probability))))
#_(t-value 0.95 10)
;; => 2.262157162798239

;; experiment with TDistribution
(comment
  (-> (TDistribution. (dec 10))
      ;; this is for confidence interval 98% (that is alpha value 0.001)
      ;; see also https://www.statisticshowto.com/tables/t-distribution-table/
      (.inverseCumulativeProbability 0.99)
      )
  ;; => 2.8214379250321984
  )

(defmethod conf-int :mean [_type level {:keys [mean count standard-deviation] :as stats}]
  {:pre [(number? mean) (number? count) (number? standard-deviation)]}
  (let [standard-error (/ standard-deviation (Math/sqrt count))
        t-value-for-level (t-value level count)
        margin-of-error (* t-value-for-level standard-error)]
    [(- mean margin-of-error)
     (+ mean margin-of-error)]))


(s/fdef confidence-interval
  :args (s/cat :type #{:mean :median}
               :confidence-level (s/double-in :min 0.0 :max 1.0)
               :stats map?))
(defn confidence-interval
  "Calculates confidence interval for mean or median using given confidence level
  and existing precomputed summary statistics.

  Commons Math and  confidence intervals: https://stackoverflow.com/questions/5564621/using-apache-commons-math-to-determine-confidence-intervals
  - not directly supported by commons-math but summary statistics provide everything you need to calculate them.

  For mean, we use the formula based on Central-limit theorem that defines standard error and margin of error (via Student's T distribution)
  - T-Distribution: https://commons.apache.org/proper/commons-math/javadocs/api-3.5/org/apache/commons/math3/distribution/TDistribution.html
  - https://www.statisticshowto.com/probability-and-statistics/confidence-interval/
  For median, we use informal formula defined in Dr Nic's video: https://www.youtube.com/watch?v=6XT8MkIzF7w
    - another alternative would be Bootstrapping
  More info about calculating confidence interval for a median:
  - Confidence interval for a median and other quantiles: https://www-users.york.ac.uk/~mb55/intro/cicent.htm
  - https://www.researchgate.net/post/How_to_calculate_changes_95_CI_in_median
  - A Simple Confidence Interval for the Median: https://www.semanticscholar.org/paper/A-Simple-Confidence-Interval-for-the-Median-Olive/8fa8fc57bd385695d12f18b1d1e9f3278339e98f?p2df
     - pdf: http://lagrange.math.siu.edu/Olive/ppmedci.pdf
  - David Olive’s median confidence interval: http://exploringdatablog.blogspot.com/2012/04/david-olives-median-confidence-interval.html
  - Confidence intervals for median: https://stats.stackexchange.com/questions/122001/confidence-intervals-for-median
  - 
"
  [type confidence-level stats]
  (conf-int type confidence-level stats))

#_(confidence-interval :median
                     0.95
                     {:min 18.0, :perc95 77.0, :mean 41.1422287390029, :standard-deviation 22.398382172681043,
                      :median 34.0, :max 261.0, :count 682, :perc25 28.0, :perc75 48.0, :sum 28059.0})
;; => [32.85124062839878 35.14875937160122]

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
                       (assoc :perc25 (perc-fn 25)
                              :median (perc-fn 50)
                              :perc75 (perc-fn 75)
                              :perc95 (perc-fn 95))
                       (select-keys selected-keys))
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

