(ns clojure-experiments.books.sicp.ch2-abstractions-data.s5-system-with-generic-operations
  "Examples and exercises from section 2.5: Systems with Generic Operations.
  Pages - 187 - 216."
  (:require [clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations :as s4
             :refer [apply-generic attach-tag put-op get-op]]))


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
            (fn [x y] (+ x y)))
    (put-op 'sub '(number number)
            (fn [x y] (- x y)))
    (put-op 'mul '(number number)
            (fn [x y] (* x y)))
    (put-op 'div '(number number)
            (fn [x y] (/ x y)))
    (put-op 'make 'number
            (fn [x] (tag x)))
    :done))
(install-ordinary-numbers)

;; now we also need a constructor for ordinary numbers
(defn make-number [n]
  ((get-op 'make 'number) n))
(add (make-number 10) (make-number 20))
;; => 30
