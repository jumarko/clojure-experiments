(ns clojure-experiments.four-clojure.115-balance-of-n
  "http://www.4clojure.com/problem/115.
  Balanced number is one whose digits have the same sum on the left and right half of the number.
  Solutions: http://www.4clojure.com/problem/solutions/115"
  (:require [clojure.test :refer [are deftest is testing]]))

(defn- halves [n]
  (let [digits (str n)
        mid-point (double (/ (count digits) 2))
        first-half (take (Math/floor mid-point) digits)
        ;; leave out the middle digit
        second-half (drop (Math/ceil mid-point) digits)]
    [first-half second-half]))
(halves 0)
(halves 11)
(halves 121)
(defn- digit->num [c] (- (int c) 48))
(defn- sum-digits [chars] (apply + (map digit->num chars)))
(sum-digits "12345")
;; => 15
(defn balanced? [n]
  (let [[f s] (halves n)]
    (= (sum-digits f) (sum-digits s))))

;; JUST for submission:
(fn balanced? [n]
  (let [digit->num (fn [c] (- (int c) 48))
        sum-digits(fn [chars] (apply + (map digit->num chars)))
        halves (fn [n]
                 (let [digits (str n)
                       mid-point (double (/ (count digits) 2))
                       first-half (take (Math/floor mid-point) digits)
                       ;; leave out the middle digit
                       second-half (drop (Math/ceil mid-point) digits)]
                   [first-half second-half]))
        [f s] (halves n)]
    (= (sum-digits f) (sum-digits s))))

(defn i [x] (inc x))

(deftest balanced-test
  (testing "balanced numbers"
    (is (balanced? 0))
    (is (balanced? 11))
    (is (balanced? 121))
    (is (balanced? 89098))
    (is (balanced? 89089)))
  (testing "unbalanced numbers"
    (is (not (balanced? 123)))
    (is (not (balanced? 88099))))
  (testing "first 20 balanced numbers"
    (is (= [0 1 2 3 4 5 6 7 8 9 11 22 33 44 55 66 77 88 99 101]
           (take 20 (filter balanced? (range)))))))

;;; Solutions: http://www.4clojure.com/problem/solutions/115
;; leetwinski:
(fn [n]
  (let [
        d (map #(Integer. (str %)) (str n))
        c (/ (count d) 2)
        l (take c d)
        r (take c (reverse d))]
    (= (apply + l) (apply + r))))
