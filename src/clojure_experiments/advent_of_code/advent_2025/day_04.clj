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

(defn- neighbours-counts-matrix
  "Replaces each element of the `rolls-matrix` by the number of adjacent rollpapers.
  Empty spaces are replaced with `nil`."
  [rolls-matrix]
  (map-indexed (fn [i row]
                 (map-indexed (fn [j _coll]
                                (when (rollpaper-at? rolls-matrix i j)
                                  (count (neighbour-rolls rolls-matrix [i j]))))
                              row))
               rolls-matrix))

(defn part1 [rolls-matrix]
  (let [neighbours-matrix (neighbours-counts-matrix rolls-matrix)]
    ;; count how many places have less then 4 neighbours
    (reduce (fn [acc row] (+ (count (filter (fn [neighbours-count]
                                              (and neighbours-count (< neighbours-count 4)))
                                            row))
                             acc))
            0
            neighbours-matrix)))

(assert (= 13 (part1 sample-rollpapers)))
;; PART 1 answer:
(assert (= 1435 (part1 rollpapers)))



;;; Part 2.
;;; Similar to part 1, but forklifts now can remove accessible rollpapers.
;;; Once rollpapers are removed, there can be more space so the forklifts can now remove more rollpapers.
;;; Keep iterating until you remove as many rollpapers as

;; Hmm...
;; As I think about it, I'll need to somewhat modify `neighbours-counts-matrix`
;; to remove rollpapers with empty spaces instead of with counts....
(defn- remove-rollpapers
  "Replace movable rollpapers (meaning they have fewer than 4 neighbours)
  in `rolls-matrix` with empty spaces (0)."
  [rolls-matrix]
  ;; NOTE: wrapping lazy seqs with `vec` to make sure we can use `get-in` even when iterating on the results (see `part2`)
  (vec (map-indexed (fn [i row]
                      (vec (map-indexed (fn [j _coll]
                                          (if (and (rollpaper-at? rolls-matrix i j)
                                                   (< (count (neighbour-rolls rolls-matrix [i j]))
                                                      4))
                                            0
                                            (get-in rolls-matrix [i j])))
                                        row)))
                    rolls-matrix)))

;; ... but then I can simplify `part1` implementation too!
;; I just need a procedure how counting rollpapers in two matrices (before and after removal)
(defn count-rollpapers [rolls-matrix]
  (->> rolls-matrix flatten (filter #(= 1 %)) (apply +)))

(defn part1 [rolls-matrix]
  (let [after-removal (remove-rollpapers rolls-matrix)]
    (- (count-rollpapers rolls-matrix)
       (count-rollpapers after-removal))))

(assert (= 13 (part1 sample-rollpapers)))
(assert (= 1435 (part1 rollpapers)))


;; And now it shouldn't be hard to reuse the above for part2
;; NOTE: we are really trying to iterate until we reach the "fixed point" of the function
;; (that is once the number of rollpapers before and after removal is the same).
(defn part2
  ([rolls-matrix]
   (part2 0 rolls-matrix))
  ([removed-total rolls-matrix]
   (let [after-removal (remove-rollpapers rolls-matrix)
         removed-rollpapers (- (count-rollpapers rolls-matrix)
                               (count-rollpapers after-removal))]
     (if (pos? removed-rollpapers)
       (part2 (+ removed-total removed-rollpapers) after-removal)
       removed-total))))
(assert (= 43 (part2 sample-rollpapers)))
(assert (= 8623 (time (part2 rollpapers))))
;; "Elapsed time: 1225.678458 msecs"

