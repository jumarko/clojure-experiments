(ns clojure-experiments.advent-of-code.advent-2024.day-01
  "Pair up two lists: https://adventofcode.com/2024/day/1"
  (:require
   [clojure-experiments.advent-of-code.advent-2024.utils :as u]
   [clojure.string :as str]))

;;; Part 1
;;; Pair up two lists by sorting them - that is pairing the smallest number from list 1 with smallest number from list 2.
(defn part1-distances
  "Computes distances (absolute differences) between items in the two lists,
  after the lists are sorted."
  [list1 list2]
  (let [sorted1 (sort list1)
        sorted2 (sort list2)]
    (mapv #(abs (- %1 %2)) sorted1 sorted2)))

(defn- transpose [avec]
  (apply mapv vector avec))
(transpose [[1 2] [3 4] [10 20]])
;; => [[1 3 10] [2 4 20]]

(defn read-input-as-lists-of-numbers []
  (let [input (u/read-input "01")]
    (->> input
         (mapv (fn [line] (mapv parse-long (str/split line #"\s+"))))
         (transpose))))

(defn part1
  "Sum up all the distances between the two lists from the input,
  after the lists are sorted."
  []
  (let [input (read-input-as-lists-of-numbers)
        distances (apply part1-distances input)]
    (apply + distances)))

(part1)
;; => 2344935


(defn part2
  "Determine how often each number from the left list appears in the right list.
  Calculate 'similarity score' by summing products of each number from the left list
  by its number of ocurrences in the right list."
  []
  (let [[l1 l2] (read-input-as-lists-of-numbers)
        l2-freqs (frequencies l2)]
    (reduce + (mapv (fn [l1-item]
                      (let [freq (get l2-freqs l1-item 0)]
                        (* l1-item freq)))
                    l1))))
(part2)
;; => 27647262

