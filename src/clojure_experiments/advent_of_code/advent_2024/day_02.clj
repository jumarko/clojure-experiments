(ns clojure-experiments.advent-of-code.advent-2024.day-02
  "https://adventofcode.com/2024/day/2"
  (:require
   [clojure.string :as str]
   [clojure-experiments.advent-of-code.advent-2024.utils :as u]))


(defn read-input-as-lists-of-numbers []
  (let [input (u/read-input "02")]
    (mapv (fn [line] (mapv parse-long (str/split line #"\s+")))
          input)))

(defn- safe? [report-seq]
  (let [distances (->> report-seq
                       (partition 2 1)
                       (map #(apply - %)))
        monothonic? (or (every? pos? distances)
                        (every? neg? distances))
        acceptable-diffs? (every? #(<= 1 (abs %) 3) distances)]
    (and monothonic? acceptable-diffs?)))

(safe? [1 2 3])

(defn part1
  "Read the reports (input) where each line is a list of numbers/levels.
  and figure out which reports are _safe_.
  Safe reports are monotonically increasing/decreasing"
  []
  (->> (read-input-as-lists-of-numbers)
       (filter safe?)
       count))

(part1)
;; => 371

;; https://stackoverflow.com/questions/1394991/clojure-remove-item-from-vector-at-a-specified-location
(defn remove-by-index
  "Removes element at index `idx` from vector `avec`."
  [avec idx]

  (into (subvec avec 0 idx) (subvec avec (inc idx))))

(defn- safe2?
  "As `safe?` but tolerates a single bad level."
  [report-seq]
  (let [v (vec report-seq)
        one-element-removed (map-indexed (fn [idx _] (remove-by-index v idx))
                                         v)]
    (some safe? one-element-removed)))

(safe2? [7 6 4 2 1])
;; => true
(safe2? [1 2 7 8 9])
;; => nil
(safe2? [9 7 6 2 1])
;; => nil
(safe2? [1 3 2 4 5])
;; => true
(safe2? [8 6 4 4 1])
;; => true
(safe? [1 3 6 7 9])
;; => true


(defn part2
  "the same rules apply as before, except if removing a single level
  from an unsafe report would make it safe, the report instead counts as safe."
  []
  (->> (read-input-as-lists-of-numbers)
       (filter safe2?)
       count))

(part2)
;; => 426


