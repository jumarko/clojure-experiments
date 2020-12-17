(ns clojure-experiments.advent-of-code.advent-2020.day-09
  "https://adventofcode.com/2020/day/9
  Input: https://adventofcode.com/2020/day/9/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]))


(def sample-input
  "35
20
15
25
47
40
62
55
65
95
102
117
150
182
127
219
299
277
309
576")

(def sample-numbers (->> sample-input str/split-lines (mapv #(Long/parseLong %))))

(def test-numbers (read-input 9 #(Long/parseLong %)))


(defn partition-numbers [step numbers]
  (->> numbers
       (partition (inc step) 1)
       (mapv (fn [part] [(last part) (take step part)]))))

(partition-numbers 5 sample-numbers)
;; => [[40 (35 20 15 25 47)]
;;     [62 (20 15 25 47 40)]
;; ...
;;     [576 (127 219 299 277 309)]]

(defn number-parts [number numbers-in-part]
  (for [x numbers-in-part
        y numbers-in-part
        :when (and (not= x y)
                   (= number (+ x y)))]
    [x y]))
(number-parts 40 '(35 20 15 25 47))
;; => ([15 25] [25 15])

(defn invalid-numbers [step numbers]
  (->> (partition-numbers step numbers)
       (filter (fn [[number part]]
                 (empty? (number-parts number part))))))
(invalid-numbers 5 sample-numbers)
;; => ([127 (95 102 117 150 182)])

(ffirst (invalid-numbers 5 sample-numbers))
;; => 127

(ffirst (invalid-numbers 25 test-numbers))
;; => 1492208709


