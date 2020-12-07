(ns clojure-experiments.advent-of-code.2020.01
  "https://adventofcode.com/2020/day/1.
  Input: https://adventofcode.com/2020/day/1/input"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure-experiments.advent-of-code.2020.utils :refer [read-input]]))

(defn sum-2020
  "Finds two numbers in the expense report that sump up to 2020
  and returns a product of these numbers.
  Returns nil if no such numbers are present."
  [expense-report]
  (let [[x y]  (first (for [x expense-report
                            y expense-report
                     :when (= 2020 (+ x y))]
                       [x y]))]
    (when x (* x y))))

(def sample-report [1721 979 366 299 675 1456])
(def test-report (read-input 1 #(Integer/parseInt %)))

(sum-2020 test-report)
;; => 211899

(deftest sum-2020-test
  (testing "Simple sum"
    (is (= 514579
           (sum-2020 sample-report))))
  )


;; Note: this could be possibly optimized by sorting the input
;; and than only taking numbers larger than given into consideration?
(defn sum-2020-3
  "Finds three numbers in the expense report that sump up to 2020
  and returns a product of these numbers.
  Returns nil if no such numbers are present."
  [expense-report]
  (let [[x y z]  (first (for [x expense-report
                              y expense-report
                              z expense-report
                            :when (= 2020 (+ x y z))]
                        [x y z]))]
    (when x (* x y z))))

(time (sum-2020-3 test-report))
;; "Elapsed time: 139.808565 msecs"
;; => 275765682

(deftest sum-2020-test-3
  (testing "Simple sum"
    (is (= 241861950
           (sum-2020-3 sample-report))))
  )
