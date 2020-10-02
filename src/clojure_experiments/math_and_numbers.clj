(ns clojure-experiments.math-and-numbers
  (:require [clojure.math.numeric-tower :as m]))

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


;;; Integer division always rounds towards zero
;;; while bit-shift-right rounds down!
(quot 13 4)
;; => 3
(bit-shift-right 13 2)
;; => 3
(quot -13 4)
;; => -3
(bit-shift-right -13 2)
;; => -4
;; but we can fix it by "biasing" the value before shifting
(bit-shift-right
 (+ -13
    (bit-shift-left 1 2)
    -1)
 2)
;; => -3
   

;;; Floating point representation (IEEE 754)
;;; Typical double precision is 52 bits for Mantisa (fraction) and 11 bits for Exponent
;;; The representation is:  (-1)^s * M * 2^E

;; Note: if we use 2^2^10 we get ##Inf
;; => this corresponds to 11 bits used for exponents: -1022;1023 range can be represented by 11 bits
(Math/pow 1.9999 (Math/pow 2 10))
;; => 1.7079656282274296E308
(Double/MAX_VALUE)
;; => 1.7976931348623157E308
(Double/MIN_VALUE)
;; => 4.9E-324


;; Floating points, BigDecimal-s and precision
(= 0.57 (float 0.57))

;; this yields double
(defn ratio->centi-float-precision
  [v]
  (* 0.01 (m/round (* 100 (float v)))))

(ratio->centi-float-precision 0.57)
;; => 0.5700000000000001

;; this yields float and although less precise it rounds "better" naturally
(defn ratio->centi-float-precision2
  [v]
  (-> (* 0.01M (m/round (* 100 v)))
      float ))

(ratio->centi-float-precision2 0.57)
;; => 0.57

(float (* (float 1.89) 791))
;; => 1494.99
(* (float 1.89) 791)
;; => 1494.9899886846542

