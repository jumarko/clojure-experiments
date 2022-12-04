(ns clojure-experiments.advent-of-code.advent-2022.day-04
  "https://adventofcode.com/2022/day/4
  Input: https://adventofcode.com/2022/day/4/input"
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure.set :as set]
   [clojure.string :as str]))

(def input (utils/read-input "04"))
(def sample-input ["2-4,6-8"
                   "2-3,4-5"
                   "5-7,7-9"
                   "2-8,3-7"
                   "6-6,4-6"
                   "2-6,4-8"])

(defn- parse-range [str-range]
  (let [ri (fn range-inclusive [a b] (range a (inc b)))]
    (->> (str/split str-range #"-")
         (map parse-long)
         (apply ri))))
(parse-range "2-8")
;; => (2 3 4 5 6 7 8)

(defn line-ranges [line]
  (map parse-range (str/split line #",")))
(line-ranges "2-8,3-7")
;; => ((2 3 4 5 6 7 8) (3 4 5 6 7))

(->> sample-input
     (map line-ranges))
;; => (((2 3 4) (6 7 8))
;;     ((2 3) (4 5))
;;     ((5 6 7) (7 8 9))
;;     ((2 3 4 5 6 7 8) (3 4 5 6 7))
;;     ((6) (4 5 6))
;;     ((2 3 4 5 6) (4 5 6 7 8)))


(defn includes-range?
  "Returns true if range a fully contains range b
  OR range b fully contains range a."
  [a b]
  (let [sa (set a)
        sb (set b)
        sab (set/intersection sa sb)]
    (or (= sab sa) (= sab sb))))

(defn contained-ranges [input]
  (->> input
      (map line-ranges)
      (filter #(apply includes-range? %))))
(contained-ranges sample-input)
;; => (((2 3 4 5 6 7 8) (3 4 5 6 7))
;;     ((6) (4 5 6)))

(defn puzzle-1 []
  (count (contained-ranges input)))
(puzzle-1)
;; => 540


(defn overlap [a b]
  (not-empty (set/intersection (set a) (set b))))

(defn overlapping-ranges [input]
  (->> (map line-ranges input)
       (filter #(apply overlap %))))
(overlapping-ranges sample-input)
;; => (((5 6 7) (7 8 9))
;;     ((2 3 4 5 6 7 8) (3 4 5 6 7))
;;     ((6) (4 5 6))
;;     ((2 3 4 5 6) (4 5 6 7 8)))

(defn puzzle-2 []
  (count (overlapping-ranges input)))
(puzzle-2)
;; => 872
