(ns clojure-experiments.advent-of-code.advent-2022.day-08
  "https://adventofcode.com/2022/day/8"
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure-experiments.macros.macros  :refer [assert=]]
   [clojure.string :as str])
  )

(def full-input (utils/read-input "08"))

(def sample-input
  (str/split-lines
   "30373
25512
65332
33549
35390"))

(def test
  [[1 0 1]
   [3 4 9]
   [2 5 7]])


(defn make-grid [lines]
  (mapv (fn [l] (mapv #(Character/digit % 10) l))
        lines))
(def sample-grid (make-grid sample-input))
;;=>
[[3 0 3 7 3]
 [2 5 5 1 2]
 [6 5 3 3 2]
 [3 3 5 4 9]
 [3 5 3 9 0]]

(defn tree-height [grid i j]
  (get-in grid [i j]))
(tree-height sample-grid 4 3)
;; => 9

(subvec (sample-grid 2) 0 2)
;; => [6 5]
(subvec (sample-grid 2) 3 5)
;; => [3 2]
(mapv #(nth % 2) sample-grid)
;; => [3 5 3 5 3]
(subvec (mapv #(nth % 2) sample-grid) 0 2)
;; => [3 5]
(subvec (mapv #(nth % 2) sample-grid) 3 5)
;; => [5 3]


(defn grid-size [grid]
  (count (first grid)))

(defn trees-between [grid i j]
  (let [gs (grid-size grid)
        row (grid i)
        col (mapv #(nth % j) grid)]
    [(subvec row 0 j)
     (subvec row (inc j) gs)
     (subvec col 0 i)
     (subvec col (inc i) gs)]))
(trees-between sample-grid 2 2)
;; => [[6 5] [3 2] [3 5] [5 3]]
(trees-between sample-grid 3 1)
;; => [[3] [5 4 9] [0 5 5] [5]]

(defn visible [grid i j]
  (let [tree (tree-height grid i j)
        tb (trees-between grid i j)]
    (some (fn [trees-toward-edge]
            (when (< (apply max (or (not-empty trees-toward-edge)
                                    [-1]))
                     tree)
              tree))
          tb)))

(visible sample-grid 0 0)
;; => 3
(visible sample-grid 1 1)
;; => 5
(visible sample-grid 2 2)
;; => nil

(defn visible-trees [grid]
  (let [gs (grid-size grid)]
    (mapv (fn [row]
            (mapv (fn [col] (visible grid row col))
                  (range gs)))
          (range gs))))
(visible-trees sample-grid)
;; => [[3 0 3 7 3]
;;     [2 5 5 nil 2]
;;     [6 5 nil 3 2]
;;     [3 nil 5 nil 9]
;;     [3 5 3 9 0]]

(defn count-visible-trees [grid]
  (->> grid
      (visible-trees)
      (apply concat)
      (remove nil?)
      count))
(assert= 21 (count-visible-trees sample-grid))


(defn part-1 []
  (count-visible-trees (make-grid full-input )))
(part-1)
;; => 1835
