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

(defn map-grid-tree [grid tree-fn]
  (let [gs (grid-size grid)]
    (mapv (fn [row]
            (mapv (fn [col] (tree-fn grid row col))
                  (range gs)))
          (range gs))))

(defn visible-trees [grid]
  (map-grid-tree grid visible))
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
(assert= 1835 (time (part-1)))
;; "Elapsed time: 228.600886 msecs"


;;; part 2

;; take-until is just like `take-while`
;; but also includes the first element that violates the predicate.
(defn take-until
  [pred coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (if (pred (first s))
       (cons (first s) (take-until pred (rest s)))
       (cons (first s) nil)))))
(take-until #(< % 3 ) (range 9))
;; => (0 1 2 3)

(defn scenic-trees [grid i j]
  (let [tree (tree-height grid i j)
        tb (trees-between grid i j)]
    (vec (map-indexed (fn [idx trees-toward-edge]
                        ;; take all the trees that are visible, starting from the closest ones
                        (let [fixed-order (cond-> trees-toward-edge (zero? (mod idx 2)) rseq)]
                          (vec (take-until #(< % tree) fixed-order))))
                      tb))))
(scenic-trees sample-grid 1 2)
;; => [[5] [1 2] [3] [3 5]]

(defn scenic-score [grid i j]
  (->> (scenic-trees grid i j)
       (map count)
       (apply *)))
(scenic-score sample-grid 1 2)
;; => 4

(scenic-trees sample-grid 3 2)
;; => [[3 3] [4 9] [3 5] [3]]
(scenic-score sample-grid 3 2)
;; => 8


(defn scenic-scores [grid]
  (map-grid-tree grid scenic-score))

(apply max (apply concat (scenic-scores sample-grid)))
;; => 8

(defn part-2 []
  (->> full-input
       make-grid
       scenic-scores
       (apply concat)
       (apply max)))
(assert= 263670 (time (part-2)))
;; "Elapsed time: 116.53988 msecs"
