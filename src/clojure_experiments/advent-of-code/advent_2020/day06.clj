(ns clojure-experiments.advent-of-code.advent-2020.day06
  "https://adventofcode.com/2020/day/6
  Input: https://adventofcode.com/2020/day/6/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.string :as str]
            [clojure.set :as set]))

(def sample-input-one-group
  "abcx
abcy
abcz")

(def sample-input-multiple-groups
  "abc

a
b
c

ab
ac

a
a
a a

b")

(def test-input (read-input 6 nil))

(defn parse-groups [input]
  (mapv str/split-lines
        (str/split input #"\R\R")))
(parse-groups sample-input-one-group)
;; => [["abcx" "abcy" "abcz"]]
(parse-groups sample-input-multiple-groups)
;; => [["abc"] ["a" "b" "c"] ["ab" "ac"] ["a" "a" "a" "a"] ["b"]]

(defn- count-group-yes-questions [group]
  (count (reduce into #{} group)))

(defn count-yes-questions [input]
  (let [groups (parse-groups input)
        groups-counts (mapv count-group-yes-questions groups)]
    (apply + groups-counts)))

(count-yes-questions sample-input-one-group)
;; => 6
(count-yes-questions sample-input-multiple-groups)
;; => 11

(count-yes-questions test-input)
;; => 6703


;;; Part 2: count questions to which _everyone_ answered yes

(defn- count-group-yes-questions [group]
  (count (apply set/intersection (mapv set group))))

(count-yes-questions sample-input-one-group)
;; => 3
(count-yes-questions sample-input-multiple-groups)
;; => 6

(count-yes-questions test-input)
;; => 3430
