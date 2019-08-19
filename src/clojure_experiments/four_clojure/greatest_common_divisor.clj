(ns four-clojure.greatest-common-divisor)

;;; http://www.4clojure.com/problem/66
;;; Given two numbers write a function that returns greatest common divisor

(defn gcd [x y]
  (let [all-divisors (for [d (range 1 (inc (min x y)))
                             :when (and
                                    (zero? (mod x d))
                                    (zero? (mod y d)))]
                         d)]
      (apply max all-divisors)))

;; more elegant algorithmic solution
(defn gcd [a b] (if (zero? b) a (gcd b (mod a b))))

(= (gcd 2 4) 2)

(= (gcd 10 5) 5)

(= (gcd 5 7) 1)

(= (gcd 1023 858) 33)
