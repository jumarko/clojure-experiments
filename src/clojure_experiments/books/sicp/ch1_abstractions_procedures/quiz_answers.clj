(ns clojure-experiments.books.sicp.ch1-abstractions-procedures.quiz
  "For quick experiments and active testing yourself (reimplementation of core fns)."
  (:require [clojure.test :refer [deftest is are testing]]))


;; iterative improvement
;; how about helper fn?
;; the idea is to start with initial guess and improve until satisfied
(defn- abs [x]
  (if (> x 0) x (- x)))
(defn- square [x]
  (* x x))
(def threshold 0.001)
(defn- close-enough? [n guess]
  (< (abs (- n (square guess)))
     threshold))
(defn- avg [x y]
  (/ (+ x y) 2))
(defn- sqrt* [n guess]
  (if (close-enough? n guess)
    (double guess)
    ;; the important idea is that we can improve initial guess by transformation avg (guess + guess/x)
    (let [improved-guess (avg guess (/ n guess))]
      (recur n improved-guess))))
(defn sqrt [n]
  (when (neg? n)
    (throw (IllegalArgumentException.  (str "Cannot be negative: " n))))
  (sqrt* n 1))

(sqrt 4)
(defn- round-to-single-decimal-place [d]
  (Double/parseDouble (format "%.1f" d)))
(deftest sqrt-test
  (testing "works for non-negative numbers"
    (are [x y] (= x (round-to-single-decimal-place (sqrt y)))
      0.0 0
      1.0 1
      2.0 4
      3.0 9
      9.0 81
      100.0 10000))
  (testing "blows for negative numbers"
    (is (thrown? IllegalArgumentException (sqrt -1)))))


;; what is Iterative process?
(defn fibonacci-iter [n])

;; "invariant quantity" (ex. 1.16)


;; "Invariant quantity"
;; Ex. 1.16 - iterative variant of `fast-exp` procedure
;; how can I use this helper fn?
(defn fast-exp [x n m])


;; why the algorithm makes sense?
;; how can you 'invent' it?
(defn gcd [a b]
  )

;; expmod (p. 51)
;; x^n mod m
;; show also the native version and what's wrong with it..
(defn expmod [x n m])

;; use expmod to implement `fermat-test`
(defn fermat-prime? [n]
  )

;;; HO fns

;; generalized accumulate (ex. 1.33)
;; consider also implementating Simpson's rule with it.
(defn accmulate [])


;;; General methods - section 1.3.3

;; half-interval method (p. 67)
;; what does it do?
(defn half-interval-method [])

;; fixed-point (p. 69)
;; how is fixed-point useful?
(defn fixed-point [f]
  )


;; what arguments it takes?
;; what exactly it does?
;; how can I use it?
;; how can I derive it?
(defn newton-method [f])

;; average dumping?

;; iterative improvement?
