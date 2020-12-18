(ns clojure-experiments.advent-of-code.advent-2020.day-10
  "https://adventofcode.com/2020/day/10
  Input: https://adventofcode.com/2020/day/10/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]))


(def sample-input
  "16
10
15
5
1
11
7
19
6
12
4")

(def sample-input2
  "28
33
18
42
31
14
46
20
48
47
24
23
49
45
19
38
39
11
1
32
25
35
8
17
7
9
4
2
34
10
3")

(def sample-jolts (->> sample-input str/split-lines (mapv #(Long/parseLong %))))
(def sample-jolts2 (->> sample-input2 str/split-lines (mapv #(Long/parseLong %))))

(def test-jolts (read-input 10 #(Long/parseLong %)))

(defn differences [jolts]
  (let [charging-outlet-jolt 0
        device-jolt (+ 3 (apply max jolts))
        all-jolts (conj jolts charging-outlet-jolt device-jolt)]
    (->> all-jolts
         sort
         (partition 2 1)
         (map (fn [[v1 v2]] (- v2 v1))))))

(differences sample-jolts)
;; => (1 3 1 1 1 3 1 1 3 1 3 3)
(frequencies (differences sample-jolts2))
;; => {1 23, 3 8, 2 1}

(defn multiply-1-and-3-diffs
  [jolts]
  (let [diffs (differences jolts)
        {ones 1 threes 3} (frequencies diffs)]
    (* ones threes)))
(multiply-1-and-3-diffs sample-jolts2)
;; should be 22 * 10
;; => 220

(multiply-1-and-3-diffs test-jolts)
;; => 1998
