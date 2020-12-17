(ns advent-of-clojure.2018.01
  "https://adventofcode.com/2018/day/1"
  (:require [advent-of-clojure.2018.input :as io]
            [clojure.test :refer [deftest is testing]]))

;;;; Time travelling device and frequency changes
;;;; input: https://adventofcode.com/2018/day/1/input
;;;; saved to 01_input.txt because `slurp` doesn't work with HTTPS

;;; Puzzle 1
;;; Sum up frequencies from the input file
(defn- to-number [line]
  (Integer/parseInt line))

(defn- sum-frequencies [numbers]
  (reduce + numbers))

(defn puzzle1 []
  (io/with-input "01_input.txt" sum-frequencies to-number))

(deftest puzzle1-test
  (testing "dummy test"
    (is (= 11 (sum-frequencies [3 -15 23]))))
  (testing "real input"
    (is (= 484 (puzzle1)))))


;;; Puzzle 2
;;; Find the repeating frequency
;;; Ideas:
;;;   1. sort isn't usable in this case since I'd loose the input order which is important
;;;   2. I could loop and store frequencies computed so far in the set and quickly check
;;;      if new frequency is already there or not.
;;;      The input list can be easily repeated with `cycle`
;;;   3. Use `reductions` (it does return lazy seq!)

(defn- first-repeating-frequency [numbers]
  (reduce
   (fn [[freqs prev-freq] num]
     (let [new-freq (+ prev-freq num)]
       (if (contains? freqs new-freq)
         (reduced new-freq)
         [(conj freqs new-freq) new-freq])))
   [#{0} 0]
   (cycle numbers)))

(defn puzzle2 []
  (io/with-input "01_input.txt" first-repeating-frequency to-number))

(deftest puzzle2-test
  (testing "dummy test"
    ;; testing only the `first-repeating-frequency` fn since the other is covered by real input test
    ;; and the previous puzzle
    (is (= 2 (first-repeating-frequency [1 -2 3 1])))
    (is (= 0 (first-repeating-frequency [1 -1])))
    (is (= 10 (first-repeating-frequency [3 3 4 -2 -4])))
    (is (= 5 (first-repeating-frequency [-6 3 8 5 -6])))
    (is (= 14 (first-repeating-frequency [7 7 -2 -7 -4]))))
  (testing "real input"
    (is (= 367 (puzzle2)))))
