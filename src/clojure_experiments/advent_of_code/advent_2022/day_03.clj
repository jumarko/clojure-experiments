(ns clojure-experiments.advent-of-code.advent-2022.day-03
  "https://adventofcode.com/2022/day/3
  Input: https://adventofcode.com/2022/day/3/input"
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure.set :as set]))

(def input (utils/read-input "03"))

;;; Puzzle 1
(let [rucksack (first input)
      [comp1 comp2] (map set (split-at (/ (count rucksack) 2) rucksack))]
  (set/intersection comp1 comp2))
;; => #{\L}

;; priorities
(int \A)
;; => 65
(int \a)
;; => 97
(defn item-priority [item]
  (let [n (if (Character/isUpperCase item)
            38
            96)]
    (- (int item) n)))
(item-priority \p)
;; => 16
(item-priority \L)
;; => 38

(defn shared-item [items-coll]
  (let [shared-items (->> items-coll
                          (map set)
                          (apply set/intersection))]
    (assert (= 1 (count shared-items))
            (str "only one shared item expected but got: " shared-items))
    (first shared-items)))

(defn rucksack-priority [rucksack]
  (let [compartments (split-at (/ (count rucksack) 2) rucksack)
        shared-item (shared-item compartments)]
    (item-priority shared-item)))

(assert (= 38 (rucksack-priority (first input))))


(apply + (map rucksack-priority input))
;; => 8109

(defn puzzle-1 []
  (apply + (map rucksack-priority input)))
(assert (= 8109 (puzzle-1)))


;;; Puzzle 2
(defn groups [input]
  (partition 3 input))

(defn group-priority [group]
  (item-priority (shared-item group)))
(group-priority (take 3 input))
;; => 2

(defn puzzle-2 []
  (->> input
       groups
       (map group-priority)
       (apply +)))
(assert (= 2738 (puzzle-2)))



;;; Other solutions

;; https://github.com/genmeblog/advent-of-code/blob/master/src/advent_of_code_2022/day03.clj
;; - notice they encoded the data as the first step
