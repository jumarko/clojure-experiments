(ns clojure-experiments.advent-of-code.advent-2020.day-03
  "https://adventofcode.com/2020/day/3
  Input: https://adventofcode.com/2020/day/3/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]))

(def sample-input
  (-> 
   "..##.......
#...#...#..
.#....#..#.
..#.#...#.#
.#...##..#.
..#.##.....
.#.#.#....#
.#........#
#.##...#...
#...##....#
.#..#...#.#
"
   (str/split #"\n")))

(def test-input (read-input 3 identity))

;; ... but computing indices using module is much simpler
(defn valid-positions
  "Given current position computes next position after moving
  `right` steps to the right and `down` steps down."
  [a-map [right down :as _slope]]
  (let [map-height (count a-map)
        map-width (count (first a-map))]
    (->> [0 0]
         (iterate (fn [[x y]]
                    [(+ x down)
                     (rem (+ y right) map-width)])) (take-while (fn [[x y]] ;; until we reach the bommot-most row on our map
                                                                  (< x map-height))))))

(valid-positions sample-input [3 1])
;; => ([0 0] [1 3] [2 6] [3 9] [4 1] [5 4] [6 7] [7 10] [8 2] [9 5] [10 8])

(defn count-trees [a-map slope]
  (->> (valid-positions a-map slope)
       (map (fn [coords] (get-in a-map coords)))
       (filter #{\#})
       count))

(count-trees sample-input [3 1])
;; => 7

(count-trees test-input [3 1])
;; => 292
(comment
  (valid-positions test-input [3 1])
  (map #(get-in test-input %) (valid-positions test-input [3 1])))

;;; Part 2 - multiply number of steps using each of given slopes
(def slopes [[1 1] [3 1] [5 1] [7 1] [1 2]])
(map #(count-trees test-input %)
     slopes)
;; => (81 292 89 101 44)

(apply * (map #(count-trees test-input %)
              slopes))
;; => 9354744432
