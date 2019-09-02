(ns clojure-experiments.books.sicp.ch1-abstractions-procedures.quiz
  "For quick experiments and active testing yourself (reimplementation of core fns).")


;; iterative improvement
;; how about helper fn?
(defn sqrt [n]
  )


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
