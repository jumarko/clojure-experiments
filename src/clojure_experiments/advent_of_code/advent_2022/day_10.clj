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

(def sample-input2
  (str/split-lines
   "addx 15
addx -11
addx 6
addx -3
addx 5
addx -1
addx -8
addx 13
addx 4
noop
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx 5
addx -1
addx -35
addx 1
addx 24
addx -19
addx 1
addx 16
addx -11
noop
noop
addx 21
addx -15
noop
noop
addx -3
addx 9
addx 1
addx -3
addx 8
addx 1
addx 5
noop
noop
noop
noop
noop
addx -36
noop
addx 1
addx 7
noop
noop
noop
addx 2
addx 6
noop
noop
noop
noop
noop
addx 1
noop
noop
addx 7
addx 1
noop
addx -13
addx 13
addx 7
noop
addx 1
addx -33
noop
noop
noop
addx 2
noop
noop
noop
addx 8
noop
addx -1
addx 2
addx 1
noop
addx 17
addx -9
addx 1
addx 1
addx -3
addx 11
noop
noop
addx 1
noop
addx 1
noop
noop
addx -13
addx -19
addx 1
addx 3
addx 26
addx -30
addx 12
addx -1
addx 3
addx 1
noop
noop
noop
addx -9
addx 18
addx 1
addx 2
noop
noop
addx 9
noop
noop
noop
addx -1
addx 2
addx -37
addx 1
addx 3
noop
addx 15
addx -21
addx 22
addx -6
addx 1
noop
addx 2
addx 1
noop
addx -10
noop
noop
addx 20
addx 1
addx 2
addx 2
addx -6
addx -11
noop
noop
noop"))

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

(reduce interpret [{:c 1 :v 1}] sample-input)
;; => [{:c 1, :v 1} {:c 2, :v 1} {:c 3, :v 1} {:c 4, :v 4} {:c 5, :v 4} {:c 6, :v -1}]

(peek (reduce interpret [{:c 1 :v 1}] sample-input2))
;; => {:c 241, :v 17}

(defn cycle-strength [{:keys [c v] :as cycle}]
  (* c v))

(defn total-strength [input]
  (->> (reduce interpret [{:c 1 :v 1}] input)
       (filter (fn [{:keys [c]}] (= (mod c 40) 20)))
       (map cycle-strength)
       (reduce +)))

(assert= 13140 (total-strength sample-input2))

;; part1 answer:
(assert= 12640 (total-strength full-input))



;;; part2


