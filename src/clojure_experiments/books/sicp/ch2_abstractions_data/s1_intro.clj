(ns clojure-experiments.books.sicp.ch2-abstractions-data.s1-intro
  "This is the section 2.1 Introduction to Data Abstraction.
  It demonstrates the technique 'constructors & selectors' using rational numbers as an example
  (sections 2.1.1 and 2.1.2 - Abstraction Barriers).
  2.1.4 contains an extended exercise - Interval Arithmetic."
  (:require [clojure.test :refer [are deftest is testing]]))


;;; 2.1.1 - Rational Numbers (p. 83)
;;; We try to implement operations on rational numbers
;;; starting just by 'wishful thinking' - that is not caring about particular implementation,
;;; rather assuming that we have basic functions already available to us:

;; this is just a first representation that came to my mind...
;; I added it because I wanted to write tests early on
(defn make-rat [n d]
  {:num n :den d})
(defn numer [x]
  (:num x))
(defn denom [x]
  (:den x))

;;---------------------
;; now implement basic operations on rational numbers
(defn add-rat [d1 d2]
  (/ (+ (* (numer d1) (denom d2))
      (* (numer d2) (denom d1))
      )
     (* (denom d1) (denom d2))))

;; Now, how can I implement a test?
;; => first implementation try with maps
(defmacro test-rat [rat-operation & nums]
  `(are [expected n1 d1 n2 d2] (= expected (~rat-operation (make-rat n1 d1) (make-rat n2 d2)))
     ~@nums))

(deftest test-add
  (testing "simple add"
    (test-rat add-rat
      3/6 1 3 1 6
      1/4 1 8 1 8)))

(defn sub-rat [d1 d2]
  (/ (- (* (numer d1) (denom d2))
        (* (numer d2) (denom d1))
        )
     (* (denom d1) (denom d2))))
(deftest test-sub
  (testing "simple subtraction"
    (test-rat sub-rat
      1/6 1 3 1 6
      -1/8 1 8 2 8)))

(defn mult-rat [d1 d2]
  (/ (* (numer d1) (numer d2))
     (* (denom d1) (denom d2))) )
(deftest test-mult
  (testing "simple multiplication"
    (test-rat mult-rat
              1/18 1 3 1 6
              2/64 1 8 2 8
              -15/4 -3 2 5 2)))

(defn div-rat [d1 d2]
  (/ (* (numer d1) (denom d2))
     (* (numer d2) (denom d1))))
(deftest test-div
  (testing "simple division"
    (test-rat div-rat
              2 1 3 1 6
              1/2 1 8 2 8
              -3/5 -3 2 5 2)))

;; equal-rat? procedure (although I didnt' use it yet)
(defn equal-rat? [x y]
  (= (* (numer x) (denom y))
     (* (numer y) (denom x))))
(equal-rat? (make-rat 2 3)
            (make-rat 4 6))
;; => true

;; Pairs - Concrete representation of rational numbers

(defn make-rat [n d]
  (cons n (cons d ())))
(make-rat 1 2)
;; => (1 2)
(defn numer [x]
  (first x))
(numer (make-rat 1 2))
;; => 1
(defn denom [x]
  (first (rest x)))
(denom (make-rat 1 2))
;; => 2


;; Rat. numbers representation
(defn print-rat [x]
  (print  "\n" (numer x) "/"  (denom x)))
(print-rat (make-rat 2 3))


;; Let's improve our representation by reducing rational numbers to the least common denominator:
(defn gcd [x y]
  (if (zero? y)
    x
    (recur y (mod x y))))

(defn make-rat [n d]
  (let [g (gcd n d)]
    (cons (/ n g) (cons (/ d g) ()))))

;; we may also defer gcd computation until numer/denom is called
(comment

  (defn numer [x]
    (let [g (gcd (first x) (first (rest x)))]
      (/ (first x) g)))
  (numer (make-rat 3 9))
  ;; => 1

  (defn denom [x]
    (let [g (gcd (first x) (first (rest x)))]
      (/ (first (rest x)) g)))
  (denom (make-rat 3 9))
  ;; => 3
  )


;; Exercise 2.1 (p. 87)
;; Define a better `make-rat` that handles positive and negative args
;; it should normmalize the sign:
;; - if number is positive both num. and den. are positive
;; - if number is negative then only num. is negative
(make-rat -3 7)
;; => (-3 7)
(make-rat 3 -7)
;; => (-3 7)
(make-rat -3 -7)
;; => (3 7)
;;=> Funny enough, this is already done thanks to the `mod` implementation in Clojure








