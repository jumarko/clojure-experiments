(ns clojure-experiments.advent-of-code.advent-2022.day-06
  "https://adventofcode.com/2022/day/6
  Input: https://adventofcode.com/2022/day/6/input

  Parsing signal - looking for start-of-packet marker (4 unique characters)
 "
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure-experiments.macros.macros  :refer [assert=]]))

(def input (first (utils/read-input "06")))

(def sample-input "mjqjpqmgbljsphdztnvjfqwrcgsmlb")


;;; Part 1

(defn packet-start [signal-code]
  (reduce (fn [[index prefix :as acc] ch]
            (if (= 4 (count (set prefix)))
              ;; they are all unique
              (reduced acc)
              [(inc index)
               (str (subs prefix (if (= 4 (count prefix)) 1 0))
                    ch)]))
          [0 ""]
          signal-code))
(packet-start sample-input)
;; => [7 "jpqm"]
(defn start-of-packet [signal-code]
  (first (packet-start signal-code)))

(assert= 7 (start-of-packet sample-input))
;; some more examples:
(assert= 5 (start-of-packet "bvwbjplbgvbhsrlpgdmjqwftvncz"))
(assert= 6 (start-of-packet "nppdvjthqldpwncqszvftbrmjlhg"))
(assert= 10 (start-of-packet "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg"))
(assert= 11 (start-of-packet "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw"))

;; Part 1 solution:
(assert= 1134 (start-of-packet input))



;;; Part 2:
;;; You also need to look at start-of-message marker, that is 14 distinct characters.

;; let's modify `packet-start` slightly to make the length of the prefix dynamic
(defn message-start [signal-code prefix-length]
  (reduce (fn [[index prefix :as acc] ch]
            (if (= prefix-length (count (set prefix)))
              ;; they are all unique
              (reduced acc)
              [(inc index)
               (str (subs prefix (if (= prefix-length (count prefix)) 1 0))
                    ch)]))
          [0 ""]
          signal-code))
(defn start-of-packet [signal-code]
  (first (message-start signal-code 4)))
(assert= 11 (start-of-packet "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw"))

(defn start-of-message [signal-code]
  (first (message-start signal-code 14)))

(assert= 19 (start-of-message sample-input))
;; some more examples:
(assert= 23 (start-of-message "bvwbjplbgvbhsrlpgdmjqwftvncz"))
(assert= 23 (start-of-message "nppdvjthqldpwncqszvftbrmjlhg"))
(assert= 29 (start-of-message "nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg"))
(assert= 26 (start-of-message "zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw"))

;; Part 2 solution:
(assert= 2263 (start-of-message input))



;;; Try an alternative implementation

;; map-indexed with multiple arguments could work, right?
;; - except that `map-indexed` doesn't take multiple collections
;; => so just `map`
(defn message-start [signal-code prefix-length]
  (->> (apply map (fn [i & chars] (when (= (count chars) (count (set chars)))
                                    [i (apply str chars)]))
              (range prefix-length (count signal-code))
              (take prefix-length (iterate next signal-code)))
       (remove nil?)
       first))
(message-start sample-input 4)
;; => [7 "jpqm"]
(assert= 7 (start-of-packet sample-input))
(assert= 1134 (start-of-packet input))
(assert= 2263 (start-of-message input))


;; Even simpler version  using `partition`
(take 4 (partition 4 1 sample-input))
;; => ((\m \j \q \j) (\j \q \j \p) (\q \j \p \q) (\j \p \q \m))

(defn message-start [signal-code prefix-length]
  (->> (map-indexed (fn [i chars]
                      (when (= (count chars) (count (set chars)))
                        [(+ i prefix-length) (apply str chars)]))
                    (partition prefix-length 1 signal-code))
       (remove nil?)
       first))
(message-start sample-input 4)
;; => [7 "jpqm"]
(assert= 7 (start-of-packet sample-input))
(assert= 1134 (start-of-packet input))
(assert= 2263 (start-of-message input))

;; even simpler is to use `keep-indexed`
(defn message-start [signal-code prefix-length]
  (first (keep-indexed (fn [i chars]
                         (when (= (count chars) (count (set chars)))
                           [(+ i prefix-length) (apply str chars)]))
                       (partition prefix-length 1 signal-code))))
(assert= 7 (start-of-packet sample-input))
(assert= 1134 (start-of-packet input))
(assert= 2263 (start-of-message input))
