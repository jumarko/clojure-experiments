(ns clojure-experiments.stats.kixi.abalone
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure-experiments.stats.kixi.graphs :as g]))

(def abalone-csv-file (io/resource "abalone.data"))

(def parse-int #(Integer/parseInt %))
(def parse-double #(Double/parseDouble %))

(defn round-tenth [x]
  (double (/ (g/round (* x 10)) 10)))

(defn parse-row [[sex & rst]]
  (let [rings (-> rst last parse-int)
        rst   (butlast rst)]
    (->> rst
         ;; Values in the CSV are divided by 200, convert back to milimeters and
         ;; grams. The original measurements are up to 1/10th of a gram or 1mm,
         ;; so round to one digit after the comma to get rid of floating point
         ;; rounding errors.
         (map #(-> % parse-double (* 200) round-tenth))
         (zipmap [:length :diameter :height :whole-weight
                  :shucked-weight :viscera-weight :shell-weight])
         (into {:sex sex :rings rings}))))

(def data
  (->> abalone-csv-file
       slurp
       csv/read-csv
       (map parse-row)))

(def numeric-variables
  [:rings :length :diameter :height :whole-weight
   :shucked-weight :viscera-weight :shell-weight])

(comment
  (take 2 (shuffle data))
  ;; => ({:rings 6,
  ;;      :sex "I",
  ;;      :shell-weight 5.8,
  ;;      :diameter 41.0,
  ;;      :whole-weight 16.8,
  ;;      :viscera-weight 3.7,
  ;;      :length 54.0,
  ;;      :shucked-weight 6.0,
  ;;      :height 10.0}
  ;;     {:rings 9,
  ;;      :sex "F",
  ;;      :shell-weight 49.0,
  ;;      :diameter 86.0,
  ;;      :whole-weight 179.6,
  ;;      :viscera-weight 46.5,
  ;;      :length 112.0,
  ;;      :shucked-weight 77.9,
  ;;      :height 29.0})
  )
