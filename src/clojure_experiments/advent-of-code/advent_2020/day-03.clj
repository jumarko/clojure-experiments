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
  3 steps to the right and 1 step below."
  [a-map]
  (let [map-height (count a-map)
        map-width (count (first a-map))]
    (->> [0 0]
         (iterate (fn [[x y]]
                    [(inc x)
                     (rem (+ y 3) map-width)])) (take-while (fn [[x y]] ;; until we reach the bommot-most row on our map
                                                              (< x map-height))))))

(valid-positions sample-input)
;; => ([0 0] [1 3] [2 6] [3 9] [4 1] [5 4] [6 7] [7 10] [8 2] [9 5] [10 8])

(defn count-trees [a-map]
  (->> a-map
       valid-positions
       (map (fn [coords] (get-in a-map coords)))
       (filter #{\#})
       count))

(count-trees sample-input)
;; => 7

(count-trees test-input)
;; => 292
(comment
  (valid-positions test-input)
  (map #(get-in test-input %) (valid-positions test-input)))
