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

;; now we also need a constructor for rational numbers
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


(defn install-complex-numbers []
  (letfn [(tag [z] (attach-tag 'complex z))]
    (put-op 'add '(complex complex)
            (fn [z1 z2] (tag (s4/add-complex z1 z2))))
    (put-op 'sub '(complex complex)
            (fn [z1 z2] (tag (s4/sub-complex z1 z2))))
    (put-op 'mul '(complex complex)
            (fn [z1 z2] (tag (s4/mul-complex z1 z2))))
    (put-op 'div '(complex complex)
            (fn [z1 z2] (tag (s4/div-complex z1 z2))))
    (put-op 'make-from-real-imag 'complex
            (fn [z1 z2] (tag (s4/make-from-real-imag z1 z2))))
    (put-op 'make-from-mag-ang 'complex
            (fn [z1 z2] (tag (s4/make-from-mag-ang z1 z2))))))

(install-complex-numbers)

;; now we also need a constructors for complex numbers
(defn make-complex-from-real-imag [x y]
  ((get-op 'make-from-real-imag 'complex) x y))
(defn make-complex-from-mag-ang [r a]
  ((get-op 'make-complex-from-mag-ang 'complex) r a))
(add
 (add (make-complex-from-real-imag 1 2) (make-complex-from-real-imag 2 3))
 (add (make-complex-from-real-imag 4 5) (make-complex-from-real-imag 5 6)))
;; => [complex [:rectangular [12 16]]]



;;; Ex. 2.77
;;; Discuss why this doesn't work and what has to be done to make it work
;;; Trace how apply-generic invocations
#_(s4/magnitude (make-complex-from-real-imag 1 2))
;; => 
;; 1. Unhandled clojure.lang.ExceptionInfo
;; No method for types
;; {:op :magnitude, :types (complex)}
;; s4_multiple_representations.clj:  307  clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations/apply-generic
;; s4_multiple_representations.clj:  302  clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations/apply-generic
;; RestFn.java:  423  clojure.lang.RestFn/invoke
;; s4_multiple_representations.clj:  313  clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations/magnitude
;; s4_multiple_representations.clj:  313  clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations/magnitude
;; REPL:  108  clojure-experiments.books.sicp.ch2-abstractions-data.s5-system-with-generic-operations/eval23619
;; ...

;; it helps to show what really is this number under the hood
(make-complex-from-real-imag 1 2)
;; => [complex [:rectangular [1 2]]]

;; so when we call  magniture, there's no operation `:magnitude` in the op-table
;; defined for tag 'complex

;; now let's add these selectors
;; note: we use keywords (:magnitude) instead of symbols ('magnitude)
;; to be consistent with `s4`
(put-op :real-part '(complex) s4/real-part)
(put-op :imag-part '(complex) s4/imag-part)
(put-op :magnitude '(complex) s4/magnitude)
(put-op :angle '(complex) s4/angle)

;; and try again
(s4/magnitude (make-complex-from-real-imag 1 2))
;; => 2.23606797749979
