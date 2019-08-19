(ns four-clojure.080-perfect-numbers
  "Number is perfect if sum of its divisors equal number itself.
  Write a function that detects perfect numbers."
  (:require [clojure.test :refer [deftest is testing]]))

(defn perfect-number?
  [n]
  ;; 
  (let [divisor? (fn [n d] (zero? (mod n d)))
        divisors? (range 1 n)
        divisors-sum (->> divisors?
                          (filter (partial divisor? n))
                          (apply +))]
    (= n divisors-sum)))


(perfect-number? 1)
;; => false
(perfect-number? 5)
;; => false
(perfect-number? 6)
;; => true
(perfect-number? 496)
;; => true

