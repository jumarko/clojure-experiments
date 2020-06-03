(ns clojure-experiments.books.sicp.ch2-abstractions-data.s5-system-with-generic-operations
  "Examples and exercises from section 2.5: Systems with Generic Operations.
  Pages - 187 - 216."
  (:require
   [clojure-experiments.books.sicp.ch2-abstractions-data.s1-intro :as s1]
   [clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations :as s4
    :refer [apply-generic attach-tag put-op get-op]]
   [clojure.spec.alpha :as s]))


;;; 2.5.1 (p. 189) We'll implement generic arithmetic operations `add`, `sub`, `mul`, `div`,
;;; working on ordinary, rational, and complex numbers
;;; using a type tag
(defn add [x y] (apply-generic 'add x y))
(defn sub [x y] (apply-generic 'sub x y))
(defn mul [x y] (apply-generic 'mul x y))
(defn div [x y] (apply-generic 'div x y))

;;install ordinary numbers arithmetic
;; called "scheme-number" in the book
;; here represented by '(number number)
(defn install-ordinary-numbers []
  (letfn [(tag [x] (attach-tag 'number x))]
    (put-op 'add '(number number)
            (fn [x y] (tag (+ x y))))
    (put-op 'sub '(number number)
            (fn [x y] (tag (- x y))))
    (put-op 'mul '(number number)
            (fn [x y] (tag (* x y))))
    (put-op 'div '(number number)
            (fn [x y] (tag (/ x y))))
    (put-op 'make 'number
            (fn [x] (tag x)))
    :done))
(install-ordinary-numbers)

;; now we also need a constructor for ordinary numbers
(defn make-number [n]
  ((get-op 'make 'number) n))
(add (make-number 10) (make-number 20))
;; => [number 30]
(add
 (add (make-number 10) (make-number 20))
 (add (make-number 10) (make-number 20)))
;; => [number 60]


;; Rational Numbers
(defn install-rational-numbers []
  (letfn [(tag [x] (attach-tag 'rational x))]
    (put-op 'add '(rational rational)
            (fn [x y] (tag (s1/add-rat x y))))
    (put-op 'sub '(rational rational)
            (fn [x y] (tag (s1/sub-rat x y))))
    (put-op 'mul '(rational rational)
            (fn [x y] (tag (s1/mult-rat x y))))
    (put-op 'div '(rational rational)
            (fn [x y] (tag (s1/div-rat x y))))
    (put-op 'make 'rational
            (fn [n d] (tag (s1/make-rat n d))))
    :done))
(install-rational-numbers)

;; now we also need a constructor for ordinary numbers
(defn make-rational [n d]
  ((get-op 'make 'rational) n d))
;; just a quick check
(s1/add-rat (s1/make-rat 1 2)
            (s1/make-rat 1 3))
(add (make-rational 1 2) (make-rational 1 3))
;; => [rational (5 6)]
(add
 (add (make-rational 1 2) (make-rational 1 3))
 (add (make-rational 1 2) (make-rational 1 3)))
;; => [rational (5 3)]



