(ns clojure-experiments.math-and-numbers)

;; interesting example used for demonstrating JVM failure
;; in apangin's talk "JVM crash dump analysis": https://www.youtube.com/watch?v=jd6dJa7tSNU

;; This should be positive but it's not!
(/ Long/MIN_VALUE -1)
;; => -9223372036854775808

;; just add one and it's again in the range of max positive long
(/ (inc Long/MIN_VALUE) -1)
;; => 9223372036854775807 ; this is Long/MAX_VALUE
(= (/ (inc Long/MIN_VALUE) -1)
   Long/MAX_VALUE)
;; => true



;; right shift in java: >> (bit-shift-right) vs. >>> (unsigned-bit-shift-right)
;; motivated by the Computer Systems book (p. 94)


(bit-shift-right 8 2)
;; => 2
(unsigned-bit-shift-right 8 2)
;; => 2

(bit-shift-right -8 2)
;; => -2
(unsigned-bit-shift-right -8 2)
;; => 4611686018427387902
;; (Long/MAX_VALUE) = 9223372036854775807
(long (/ Long/MAX_VALUE  2))
;; => 4611686018427387903
