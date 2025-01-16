(ns clojure-experiments.advent-of-code.advent-2024.utils
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn read-input [day-num]
  (->> (slurp (io/reader (format "src/clojure_experiments/advent_of_code/advent_2024/day_%s.txt"
                                 day-num)))
       str/split-lines))

(defn transpose [avec]
  (apply mapv vector avec))
