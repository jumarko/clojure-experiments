(ns clojure-experiments.advent-of-code.advent-2022.day-01
  "https://adventofcode.com/2022/day/1.
  Input: https://adventofcode.com/2022/day/1/input"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def input (->> (slurp (io/reader "src/clojure_experiments/advent_of_code/advent_2022/01.txt"))
                str/split-lines))

(defn sum-nums [str-nums]
  (->> str-nums
       (map parse-long)
       (apply +)))
#_(sum-nums ["10" "212" "0"])

(defn elves-calories [input]
  (->> input
       (partition-by empty?)
       (remove #{[""]})
       (map sum-nums)))

(defn puzzle-1 [input]
  (apply max (elves-calories input)))
(assert (= (puzzle-1 input)
           67450))

(defn puzzle-2 [input]
  (->> (elves-calories input)
       sort
       (take-last 3)
       (apply +)))
(assert (= (puzzle-2 input)
           199357))
