(ns clojure-experiments.books.clojure-brain-teasers.01-equality
  "See also
  - https://clojure.org/reference/data_structures#Numbers")

(= 1 1N 1.0 1.0M)
;; => false

(== 1 1N 1.0 1.0M)
;; => true

;;; further checks
(= 1 1N) ; notice that bigint and int are equal!
;; => true

(= 1.0 1.0M)
;; => false

(= 1 1.0)
;; => false

;;; Discussion

;; The == operator works only on numbers, so you can’t use it on collections of numbers or anything else
(== [1 2] [1.0 2.0])
;; => 1. Unhandled java.lang.ClassCastException
;; class clojure.lang.PersistentVector cannot be cast to class java.lang.Number

