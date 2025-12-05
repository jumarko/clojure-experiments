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
