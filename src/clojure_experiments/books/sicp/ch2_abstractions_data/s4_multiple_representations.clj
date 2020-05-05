(ns clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations
  "Examples from Chapter 2 - section 4: Multiple Representations for Abstract Data"
  (:require [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :as c]))


;;; 2.4.1 Representation for Complex Numbers

;; We can start by desining constructors and selectors:


(defn make-from-real-imag [real imag])

(defn make-from-mag-ang [mag ang])

(defn real-part [z])
(defn imag-part [z])
(defn magnitude [z])
(defn angle [z])

;; then define operations in terms of the constructors and selectors


(defn add-complex [z1 z2]
  (make-from-real-imag
   (+ (real-part z1) (real-part z2))
   (+ (imag-part z1) (imag-part z2))))

(defn sucomplex [z1 z2]
  (make-from-real-imag
   (- (real-part z1) (real-part z2))
   (- (imag-part z1) (imag-part z2))))

(defn mul-complex [z1 z2]
  (make-from-mag-ang
   (* (magnitude z1) (magnitude z2))
   (+ (angle z1) (angle z2))))

(defn div-complex [z1 z2]
  (make-from-mag-ang
   (/ (magnitude z1) (magnitude z2))
   (- (angle z1) (angle z2))))


;; Ben's representation -> rectangular form
(let []
  (defn make-from-real-imag [real imag]
    [real imag])
  (defn make-from-mag-ang [mag ang]
    [(* mag (Math/cos ang)) (* mag (Math/sin ang))])
  (defn real-part [z]
    (first z))
  (defn imag-part [z]
    (second z))
  (defn magnitude [z]
    (Math/sqrt (+ (c/square (real-part z))
                  (c/square (imag-part z)))))
  (defn angle [z]
    ;; note that this is different from Scheme's atan in that it accepts a single arg
    ;; which should be y/x: https://communityviz.city-explained.com/communityviz/s360webhelp4-1/Formulas/Function_library/Atan_function.htm
    (Math/atan (/ (imag-part z)
                  (real-part z))))

  (def beni (make-from-real-imag 2 3))
  (real-part beni)
  ;; => 2.0
  (imag-part beni)
  ;; => 3.0
  (magnitude beni)
  ;; => 3.605551275463989
  (angle beni)
;; => 0.982793723247329

  (add-complex beni (make-from-real-imag 20 30))
  ;; => [22 33]
  )

;; Alyssa's representation -> polar form
(let []
  (defn make-from-real-imag [real imag]
    [(Math/sqrt (+ (c/square real) (c/square imag)))
     (Math/atan (/ imag real))])
  (defn make-from-mag-ang [mag ang]
    [mag ang])
  (defn magnitude [z]
    (first z))
  (defn angle [z]
    (second z))
  (defn real-part [z]
    (* (magnitude z) (Math/cos (angle z))))
  (defn imag-part [z]
    (* (magnitude z) (Math/sin (angle z))))

  (def alysi (make-from-mag-ang 3.605551275463989 0.982793723247329))
  (real-part alysi)
  ;; => 2.0
  (imag-part alysi)
  ;; => 3.0
  (magnitude alysi)
  ;; => 3.605551275463989
  (angle alysi)
  ;; => 0.982793723247329

  ;; This doesn't work?!
  (add-complex alysi (make-from-real-imag 20 30))
  ;; => [23.605551275463988 30.98279372324733]
  )

(real-part [1 2])
;; => -0.4161468365471424
