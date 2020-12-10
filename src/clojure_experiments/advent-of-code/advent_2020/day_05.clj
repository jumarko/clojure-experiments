(ns clojure-experiments.advent-of-code.advent-2020.day-05
  "https://adventofcode.com/2020/day/5
  Input: https://adventofcode.com/2020/day/5/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.test :refer [deftest is testing are]]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]))

;;;; Explore nearby boarding passes.
;;;; A seat might be specified like FBFBBFFRLR, where F means "front", B means "back", L means "left", and R means "right".
;;;; consider just the first seven characters of FBFBBFFRLR:
;;;;
;;;; Start by considering the whole range, rows 0 through 127.
;;;;  F means to take the lower half, keeping rows 0 through 63.
;;;;  B means to take the upper half, keeping rows 32 through 63.
;;;;  F means to take the lower half, keeping rows 32 through 47.
;;;;  B means to take the upper half, keeping rows 40 through 47.
;;;;  B keeps rows 44 through 47.
;;;;  F keeps rows 44 through 45.
;;;;  The final F keeps the lower of the two, row 44.
;;;;
;;;; The last three characters will be either L or R;
;;;;   these specify exactly one of the 8 columns of seats on the plane (numbered 0 through 7). T
;;;;
;;;; => FBFBBFFRLR is seat at row 44, column 5
;;;;

(def sample-seat "FBFBBFFRLR")

(def test-input (read-input 5 identity))

(defn seat-id [[row col :as _seat]]
  (+ (* row 8) col))
(seat-id [44 5])
;; => 357

(defn binary-to-decimal [binary-string]
  (Integer/parseInt binary-string 2))

(s/fdef string-to-seat
  :args (s/cat :seat (s/and string? #(= 10 (count %))))
  :ret (s/tuple pos-int? pos-int?))
(defn string-to-seat [seat-string]
  (let [to-binary (fn [row-or-column-substring]
                    (str/join (mapv {\F 0 \B 1
                                     \L 0 \R 1}
                                    row-or-column-substring)))
        row-binary (to-binary (subs seat-string 0 7))
        col-binary (to-binary (subs seat-string 7))]
    [(binary-to-decimal row-binary) (binary-to-decimal col-binary)]))

(string-to-seat sample-seat)
;; => [44 5]

(deftest string-to-seat-test
  (testing "sample seat"
    (is (= [44 5]
           (string-to-seat sample-seat))))
  (testing "more sample seats"
    (are [parsed-seat seat-string] (= parsed-seat (string-to-seat seat-string))
      [70 7] "BFFFBBFRRR"
      [14 7] "FFFBBBFRRR"
      [102 4] "BBFFBBFRLL")))

(defn highest-id [seats-input]
  (->> seats-input
       (mapv string-to-seat)
       (mapv seat-id)
       (apply max)))
(highest-id test-input);; => 980

 
