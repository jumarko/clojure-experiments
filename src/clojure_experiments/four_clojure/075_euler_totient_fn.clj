(ns clojure-experiments.four-clojure.075-euler-totient-fn
  "Euler Totient Function: http://www.4clojure.com/problem/75.
  See also solutions: http://www.4clojure.com/problem/solutions/75"
  (:require [clojure.test :refer [deftest is testing]]))

;;;; comprime numbers => GCD(x, y) = 1
;;;; totient fn(x) = "number of positive integers y < x such that x and y are comprime numbers"

(defn totient [x]
  (if (= 1 x)
    1
    (let [gcd (fn gcd [a b] (if (zero? b) a (gcd b (mod a b))))
          totient-numbers (filter #(= 1 (gcd % x))
                                  (range 1 x))]
      (count totient-numbers))))

(deftest totient-test
  (testing "special case"
    (is (= 1
           (totient 1))))
  (testing "10 has 4 coprimes"
    (is (= 4
           (count [1 3 7 9])
           (totient 10))))
  (testing "40 has 16 coprimes"
    (is (= 16
           (totient 40))))
  (testing "99 has 60 coprimes"
    (is (= 60
           (totient 99))))
  )
