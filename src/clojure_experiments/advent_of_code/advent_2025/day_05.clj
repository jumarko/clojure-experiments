(ns clojure-experiments.advent-of-code.advent-2025.day-05
  "Input: https://adventofcode.com/2025/day/5/input.
  "
  (:require
   [clojure-experiments.advent-of-code.utils :as utils]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

;;; Specs
(s/def ::fresh-ranges (s/coll-of (s/tuple int? int?)))
(s/def ::available-ingredients (s/coll-of int?))

;;; Input
(s/fdef parse-input
  :args (s/cat :input-lines (s/coll-of string?))
  :ret (s/keys :req-un [::fresh-ranges ::available-ingredients]))
(defn parse-input
  [input-lines]
  {:fresh-ranges (mapv (fn [range]
                         (mapv parse-long (str/split range #"-")))
                       (take-while not-empty input-lines))
   :available-ingredients (mapv parse-long (drop 1 (drop-while not-empty input-lines)))})

(def sample-input (str/split-lines "3-5
10-14
16-20
12-18

1
5
8
11
17
32"))

(def sample-parsed (parse-input sample-input))

(def full-input (utils/read-input 2025 5))
(def full-parsed (parse-input full-input))

(defn fresh-ingredients
  [{:keys [available-ingredients fresh-ranges] :as parsed-input}]
  ;; Naive solution - go through the ingredients one-by-one and check whether they are in any of the ranges
  (keep (fn [ingredient]
          (when (some (fn [[start end]]
                        (<= start ingredient end))
                      fresh-ranges)
            ingredient))
        available-ingredients))


(assert (= [5 11 17] (fresh-ingredients sample-parsed)))

(defn part1 [parsed-input]
  (count (fresh-ingredients parsed-input)))
(part1 sample-parsed)
;; => 3

(part1 full-parsed)
;; => 848


;;; Part 2: ignore the list of available ingredients;
;;; Instead, count how many fresh ingredients are in the ranges

;; First naive attept - of course it doesn't work because the ranges can _overlap_.
(defn part2 [{:keys [fresh-ranges] :as parsed-input}]
  (reduce
   (fn [acc [start end]]
     (+ acc (- end start)))
   0
   fresh-ranges))
(part2 sample-parsed)
;; => 16 (should be 14)


;; try again...
;; So we must reoncile overlapping intervals somehow
;; Here are some ideas: https://www.geeksforgeeks.org/dsa/minimum-removals-required-to-make-ranges-non-overlapping/
;; I think sorting them by starting element works nicely - let's try that!
(defn non-overlapping-intervals [intervals]
  (let [sorted-intervals (sort-by first intervals)]
    (reduce
     (fn [non-overlapping-intervals [a b :as interval]]
       (let [[x y :as previous-interval] (peek non-overlapping-intervals)]
         (if (or (nil? previous-interval) (< y a))
           ;; non-overlapping intervals, simply add the new interval to the list
           (conj non-overlapping-intervals interval)
           ;; overlapping intervals, let's replace previous-interval 
           (conj (pop non-overlapping-intervals) [x (max y b)]))))
     []
     sorted-intervals)))

(assert (= [[3 5] [10 20]]
           (non-overlapping-intervals (:fresh-ranges sample-parsed))))

(defn part2 [{:keys [fresh-ranges] :as _parsed-input}]
  (let [non-overlapping-ranges (non-overlapping-intervals fresh-ranges)]
    (reduce
     (fn [acc [start end]]
       (+ acc (inc (- end start))))
     0
     non-overlapping-ranges)))
(assert (= 14 (part2 sample-parsed)))

(part2 full-parsed)
;; => 334714395325710
