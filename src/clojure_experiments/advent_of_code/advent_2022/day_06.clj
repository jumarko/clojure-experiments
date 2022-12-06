(ns clojure-experiments.advent-of-code.advent-2022.day-06
  "https://adventofcode.com/2022/day/6
  Input: https://adventofcode.com/2022/day/6/input

  Parsing signal - looking for start-of-packet marker (4 unique characters)
 "
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure.string :as str]))


(def input (first (utils/read-input "06")))

(def sample-input "mjqjpqmgbljsphdztnvjfqwrcgsmlb")


;;; Part 1

(defn packet-start [signal-code]
  (reduce (fn [[prefix index :as acc] ch]
            (if (= 4 (count (set prefix)))
              ;; they are all unique
              (reduced acc)
              [(str (subs prefix (if (= 4 (count prefix)) 1 0))
                    ch)
               (inc index)]))
          ["" 0]
          signal-code))
(packet-start sample-input)
;; => ["jpqm" 7]
(defn start-of-packet [signal-code]
  (second (packet-start signal-code)))

(assert (= 7 (start-of-packet sample-input)))
;; some more examples:
(assert (= 5 (start-of-packet "bvwbjplbgvbhsrlpgdmjqwftvncz")))
(assert (= 6 (start-of-packet "nppdvjthqldpwncqszvftbrmjlhg")))
(assert (= 10 (start-of-packet "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg")))
(assert (= 11 (start-of-packet "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw")))

;; Part 1 result:
(start-of-packet input)
;; => 1134
