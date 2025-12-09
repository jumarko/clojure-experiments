(ns clojure-experiments.advent-of-code.advent-2025.day-07
  "Cephalopod math homework.
  Input: https://adventofcode.com/2025/day/7/input.
  "
  (:require
   [clojure-experiments.advent-of-code.utils :as utils]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))



(def sample-input (drop 1 (str/split-lines "
.......S.......
...............
.......^.......
...............
......^.^......
...............
.....^.^.^.....
...............
....^.^...^....
...............
...^.^...^.^...
...............
..^...^.....^..
...............
.^.^.^.^.^...^.
...............")))

(def full-input (utils/read-input 2025 7))


;;; Part 1: The goal is to find how many times the tachyom beam is split
;;; This boils down to the number of '^' in the manifold diagram,
;;; but counting only those that are actually hit by some beam.
;;; In the sample input, there's one that doesn't get hit,
;;; therefore the answer is 21, not 22.


;;; Solution method.
;;; Generate the final diagram with beams ('|') shown in appropriate places.
;;; We do that by starting with a single beam in the second row directly under the 'S' symbol,
;;; then com
;;; by iterating over the input and combining 
;;; To get the final result (number of beams)

(def splitter \^)
(def empty-spot \.)
(def beam \|)
(defn- combine [previous-row-with-beams next-row-without-beams]
  (apply str
         (map-indexed (fn [i previous-char]
                        (let [next-char (get next-row-without-beams i)
                              splitter-hit-by-beam? (fn [index]
                                                      (and (<= 0 index)
                                                           (= splitter (get next-row-without-beams index))
                                                           (= beam (get previous-row-with-beams index))))]
                          (condp = previous-char
                            ;; there's always empty space after a splitter
                            splitter empty-spot
                            beam (if (= next-char empty-spot)
                                   ;; beam continues if there's empty space after it ...
                                   beam
                                   ;; ... and get's blocked if there's a splitter.
                                   splitter)
                            ;; the most tricky case - empty spot might be transformed into a beam
                            ;; if there's a splitter at adjacent positions AND there's a beam above the splitter
                            empty-spot (cond
                                         ;; if it's a splitter just repeat it - we cannot replace it with a beam or empty spot
                                         (= splitter next-char) splitter

                                         ;; check if there's a splitter to the left or right and there was a beam above it
                                         (or (splitter-hit-by-beam? (inc i))
                                             (splitter-hit-by-beam? (dec i)))
                                         beam

                                         :else next-char))))

                      previous-row-with-beams)))

(defn trace-beams
  [[first-line & rest-lines :as input-lines]]
  ;; this re-draws the original input diagram with beams at appropriate location
  ;; starting with the _second_ row (with a beam directly under 'S')
  (reduce (fn [beams-diagram current-level]
            (conj beams-diagram (combine (peek beams-diagram) current-level)))
          ;; Start with one beam at the 'S' position
          [(str/replace first-line \S \|)]
          rest-lines))

(trace-beams sample-input)
;;=>
[".......|......."
 ".......|......."
 "......|^|......"
 "......|.|......"
 ".....|^|^|....."
 ".....|.|.|....."
 "....|^|^|^|...."
 "....|.|.|.|...."
 "...|^|^|||^|..."
 "...|.|.|||.|..."
 "..|^|^|||^|^|.."
 "..|.|.|||.|.|.."
 ".|^|||^||.||^|."
 ".|.|||.||.||.|."
 "|^|^|^|^|^|||^|"
 "|.|.|.|.|.|||.|"]

;; the answer is the number of '^' with a beam above it
(defn part1 [parsed-input]
  (let [traced (trace-beams parsed-input)
        ;; I call 'inactive' splitters that aren't hit by a beam;
        ;; as such, they don't split anything and shouldn't be counted.
        removed-inactive-splitters
        (map (fn [previous-line line]
               (map-indexed (fn [i spot]
                              (if (= splitter spot)
                                ;; remove the splitter if there isn't a beam above it
                                ;; to not count those
                                (when (= beam (get previous-line i)) splitter)
                                spot))
                            line))
             ;; the first line is irrelevant because there cannot be any relevant splitters on the first two lines
             (drop 1 traced) (drop 2 traced))]
    ;; now count the number of active splitters
    (count (filter #(= \^ %)
                   ;; flatten the collection to make filtering easier
                   (apply concat removed-inactive-splitters)))))

(assert (= 21 (part1 sample-input)))

(part1 full-input)
;; => 1662



;;; Part 2: Quantum tachyon manifold
;;; Single tachyon enters the manifold and "picks" left or right at each splitter.
;;; Count the total number of different paths the tachyon can take.

;; It seems to me that I can reuse the `trace-beams` function defined above
;; and simply count the total number of different paths marked by "beams" (pipes).
;; Q: how do I do that? 

;; Let's iterate through the rows, starting with initial state with tachyon in the first
;; row at the column defined by S (in the 0th row)
;; Then we can track its path by accumulating indexes
;; - Each time there's a splitter we split a path into two by appending index of -+1 to the previous path
;; - if it's not a splitter then it we just append the same index as the previous row had
(defn part2 [parsed-input]
  (let [start-column (.indexOf (first parsed-input)  "S")]
    (reduce
     (fn [paths-so-far next-row]
       ;; mapcat needed in case the path is split into two
       (set (mapcat (fn [path]
                      (let [prev-row-index (peek path)]
                           ;; if, in the next row, there's a splitter at the same index as the last element of the path (previous row)
                           ;; then we split it into two possible paths
                        (if (= splitter (get next-row prev-row-index))
                          [(conj path (dec prev-row-index))
                           (conj path (inc prev-row-index))]
                             ;; otherwise just 'extend' the path by appending the same index (there's no change in tachyom's trajectory)
                          [(conj path prev-row-index)])))
                    paths-so-far)))

     #{[start-column]}
     (drop 2 parsed-input))))


;; this works on sample input
(assert (= 40 (count (part2 sample-input))))

;; ... but it's way too slow on the full input :(
(comment 
  ;; takes a couple of minutes and then it fails with OutOfMemoryError
  (time (part2 full-input))
  )


;; Q: what can I optimize?
;; Maybe I don't need to track the whole path?
;; That is, I only need to keep the previous row index?
;; That would help with memory at least...
;; BUT: how would I dinstiguish between all the different possibilities?
