(ns clojure-experiments.advent-of-code.advent-2025.day-04
  "Input: https://adventofcode.com/2025/day/4/input.
  "
  (:require
   [clojure-experiments.advent-of-code.utils :as utils]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clj-http.client :as http]))

;;; Specs


;;; Input
(def sample
  ["..@@.@@@@."
   "@@@.@.@.@@"
   "@@@@@.@.@@"
   "@.@@@@..@."
   "@@.@@@@.@@"
   ".@@@@@@@.@"
   ".@.@.@.@@@"
   "@.@@@.@@@@"
   ".@@@@@@@@."
   "@.@.@@@.@."])

(defn parse-rollpapers
  "Parses lines of input into a matrix of 2-D vectors where 1 represents a rollpaper and 0 an empty space."
  [input-lines]
  (mapv (fn [line] (mapv (fn [c] (case c \@ 1 \. 0))
                         line))
        input-lines))

(def sample-rollpapers (parse-rollpapers sample))
;;=>
[[0 0 1 1 0 1 1 1 1 0]
 [1 1 1 0 1 0 1 0 1 1]
 [1 1 1 1 1 0 1 0 1 1]
 [1 0 1 1 1 1 0 0 1 0]
 [1 1 0 1 1 1 1 0 1 1]
 [0 1 1 1 1 1 1 1 0 1]
 [0 1 0 1 0 1 0 1 1 1]
 [1 0 1 1 1 0 1 1 1 1]
 [0 1 1 1 1 1 1 1 1 0]
 [1 0 1 0 1 1 1 0 1 0]]

(def rollpapers (parse-rollpapers (utils/read-input 2025 4)))
;; preview input:
(take 2 rollpapers)
;; =>
[[1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 1 1 0 1 1 1 0 1 1 0 1 1 1 1 0 0 1 0 1 1 0 0 0 0 0 0 1 1 1 1 0 0 1 0 0 0 1 1 1 0 1 1 1 0 0 0 0 0 1 0 1 1 1 1 1 0 1 1 0 0 1 0 1 1 0 1 0 0 1 1 1 0 1 0 0 0 1 1 1 1 0 1 0 1 0 1 1 1 1 1 1 1 0 1 0 1 1 1 0 0 1 0 0 0 1 1 1 0 1 1 1 0 0 1 1 0 0 0 1 0 1]
 [0 1 1 0 0 0 0 1 1 0 0 0 1 1 1 0 1 1 1 0 1 0 0 1 1 0 0 1 1 1 1 0 1 0 1 1 0 1 0 1 1 0 1 0 1 0 1 1 0 0 1 1 1 1 0 1 0 1 0 0 1 0 1 0 1 0 1 0 1 1 1 1 1 0 0 1 1 1 1 1 1 0 0 1 1 1 1 0 1 1 0 1 1 1 1 1 1 1 0 0 1 1 1 0 1 0 1 1 1 1 1 1 0 1 1 1 1 1 0 1 0 1 1 0 1 1 1 0 1 0 0 0 1 0 1 0]]

(defn rollpaper-at?
  "Returns true if there's a rollpaper at given position in the matrix,
  false otherwise."
  [matrix x y]
  (= 1 (get-in matrix [x y])))


(defn neighbour-rolls
  "Find any rollpapers in the neighbourhood of given point in the matrix.
  A rollpaper is marked by value '1' at the position."
  [matrix [x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :let [xi (+ x dx) yi (+ y dy)]
        :when (and (not= 0 dx dy) ; not the same element
                   (rollpaper-at? matrix xi yi))] ; it's a rollpaper and not an empty space (or outside the matrix)
    [xi yi]))
(assert (= [[1 0] [1 1]]
           (neighbour-rolls sample-rollpapers [0 0])))
(assert (= [[2 6] [3 5] [4 5] [4 6]]
           (neighbour-rolls sample-rollpapers [3 6])))
(assert (= [[8 8] [9 8]]
           (neighbour-rolls sample-rollpapers [9 9])))
(assert (empty? (neighbour-rolls sample-rollpapers [11 11])))


(defn part1 [rolls-matrix]
  ;; go through the matrix and replace each element matrix by the number of adjacent rollpapers;
  ;; remove empty spaces so we don't count them
  (let [neighbours-counts-matrix (map-indexed (fn [i row]
                                                (map-indexed (fn [j _coll]
                                                               (when (rollpaper-at? rolls-matrix i j)
                                                                 (count (neighbour-rolls rolls-matrix [i j]))))
                                                             row))
                                              rolls-matrix)]
    (reduce (fn [acc row] (+ (count (filter (fn [neighbours-count]
                                              (and neighbours-count (< neighbours-count 4)))
                                            row))
                             acc))
            0
            neighbours-counts-matrix)))

(assert (= 13 (part1 sample-rollpapers)))

(assert (= 1435 (part1 rollpapers)))

