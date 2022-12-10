(ns clojure-experiments.advent-of-code.advent-2022.day-10
  "https://adventofcode.com/2022/day/10
  Input: https://adventofcode.com/2022/day/10/input"
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure-experiments.macros.macros  :refer [assert=]]
   [clojure.string :as str]))


(def full-input (utils/read-input "10"))

(def sample-input
  (str/split-lines
   "noop
addx 3
addx -5"
   ))

(def sample-input2 (utils/read-input "10.sample"))

(defn interpret [cycles instruction]
  (let [{:keys [c v] :as _last-cycle} (peek cycles)
        [inst arg] (str/split instruction #" ")
        noop {:c (inc c) :v v}]
    (case inst
      "noop" (conj cycles noop)
      "addx" (conj cycles noop {:c (+ c 2) :v (+ v (parse-long arg))}))))
(interpret [{:c 1 :v 1}] "noop")
;; => [{:c 1, :v 1} {:c 2, :v 1}]
(interpret [{:c 1, :v 1} {:c 2, :v 1}] "addx -5")
;; => [{:c 1, :v 1} {:c 2, :v 1} {:c 3, :v 1} {:c 4, :v -4}]

(defn compute [input]
  (reduce interpret [{:c 1 :v 1}] input))

(compute sample-input)
;; => [{:c 1, :v 1} {:c 2, :v 1} {:c 3, :v 1} {:c 4, :v 4} {:c 5, :v 4} {:c 6, :v -1}]

(assert= {:c 241 :v 17}
         (peek (compute sample-input2)))

(defn cycle-strength [{:keys [c v] :as cycle}]
  (* c v))

(defn total-strength [input]
  (->> (compute input)
       (filter (fn [{:keys [c]}] (= (mod c 40) 20)))
       (map cycle-strength)
       (reduce +)))

(assert= 13140 (total-strength sample-input2))

;; part1 answer:
(assert= 12640 (total-strength full-input))



;;; part2
(compute sample-input2)

(defn draw-pixel [pixel x]
  (if (<= (dec x) pixel (inc x))  "#" "."))
(assert= "#" (draw-pixel 0 1))
(assert= "#" (draw-pixel 2 1))
(assert= "."(draw-pixel 3 1))
(assert= "#" (draw-pixel 5 4))

(assert= ["#" "#" "." "#" "#" "."]
         (mapv (fn [{:keys [c v]}] (draw-pixel c v))
               (compute sample-input)))

(defn draw-screen [input]
  (->> (compute input)
       (map (fn [{:keys [c v]}] (draw-pixel (dec (mod c 40))
                                            v)))
       (partition-all 40)
       (mapv #(apply str %))))

(draw-screen sample-input)
;; => ["##.##."]

(draw-screen sample-input2)
;;=>
["##..##..##..##..##..##..##..##..##..##.."
 "###...###...###...###...###...###...###."
 "####....####....####....####....####...."
 "#####.....#####.....#####.....#####....."
 "######......######......######......###."
 "#######.......#######.......#######....."
 "."]

(draw-screen full-input)
;;=> 
["####.#..#.###..####.#....###....##.###.#"
 "#....#..#.#..#....#.#....#..#....#.#..##"
 "###..####.###....#..#....#..#....#.#..##"
 "#....#..#.#..#..#...#....###.....#.###.."
 "#....#..#.#..#.#....#....#.#..#..#.#.#.#"
 "####.#..#.###..####.####.#..#..##..#..#."
 "."]
