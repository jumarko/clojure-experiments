(ns clojure-experiments.books.clojure-brain-teasers.02-promotion
  "NOTE: The promoting arithmetic operations are slower
  because they are not typically supported directly by the underlying CPU instruction set.
  ")

(def bignum 9223372036854775807)
(def biggernum 9223372036854775808)

(= biggernum (+ bignum 1))
;; => 
;; 1. Unhandled java.lang.ArithmeticException
;; long overflow

;; here it's automatically promoted to BigInt
;; NOTE
(= biggernum (+' bignum 1))
;; => true

;; uncheck-add doesn't promote but instead produces Long/MIN_VALUE
(= biggernum (unchecked-add bignum 1))
;; => false
(unchecked-add bignum 1)
;; => -9223372036854775808
(= Long/MIN_VALUE (unchecked-add bignum 1));; => true
