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

;; Here we have two representations - Ben's and Alyssa's
;; In this shape, they aren't going to work properly =>
;; see 2.4.2 Tagged data for an implementation that allows both representations
;; to coexist in the same system

;; Ben's representation -> rectangular form
(comment 
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

  ;; Alyssa's representation -> polar form
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

  (real-part [1 2]))
;; => -0.4161468365471424


;;; 2.4.2 Tagged data
;;; Introducing typed tags to distinguish between multiple representations
;;; coexisting in the same system.
;;; We'll define new procedures `attach-tag`, `type-tag`, and `contents` to deal with tagged data
(defn attach-tag [type-tag contents]
  [type-tag contents])

(defn type-tag [datum]
  (if (vector? datum)
    (first datum)
    (throw (ex-info "Bad tagged datum" {:datum datum}))))

(defn contents [datum]
  (if (vector? datum)
    (second datum)
    (throw (ex-info "Bad tagged datum" {:datum datum}))))

;; let's define rectangular? and polar? predicates using the above procedures
(defn rectangular? [datum]
  (= :rectangular (type-tag datum)))
(defn polar? [datum]
  (= :polar (type-tag datum))
  
  (= :polar (type-tag datum)))


;; now let Ben and Alyssa reimplement their procedures properly => they must use different names!

;; First Ben
(defn real-part-rectangular [z]
  (first z))
(defn imag-part-rectangular [z]
  (second z))
(defn magnitude-rectangular [z]
  (Math/sqrt (+ (c/square (real-part-rectangular z))
                (c/square (imag-part-rectangular z)))))
(defn angle-rectangular [z]
  ;; note that this is different from Scheme's atan in that it accepts a single arg
  ;; which should be y/x: https://communityviz.city-explained.com/communityviz/s360webhelp4-1/Formulas/Function_library/Atan_function.htm
  (Math/atan (/ (imag-part-rectangular z)
                (real-part-rectangular z))))
(defn make-from-real-imag-rectangular [real imag]
  (attach-tag :rectangular [real imag]))
(defn make-from-mag-ang-rectangular [mag ang]
  (attach-tag :rectangular [(* mag (Math/cos ang)) (* mag (Math/sin ang))]))

;; Alyssa's representation -> polar form
(defn magnitude-polar [z]
  (first z))
(defn angle-polar [z]
  (second z))
(defn real-part-polar [z]
  (* (magnitude-polar z) (Math/cos (angle-polar z))))
(defn imag-part-polar [z]
  (* (magnitude-polar z) (Math/sin (angle-polar z))))
(defn make-from-real-imag-polar [real imag]
  (attach-tag :polar
              [(Math/sqrt (+ (c/square real) (c/square imag)))
               (Math/atan (/ imag real))]))
(defn make-from-mag-ang-polar [mag ang]
  (attach-tag :polar [mag ang]))

;; Now implement generic selectors:
(defn real-part [z]
  (cond
    (rectangular? z) (real-part-rectangular (contents z))
    (polar? z) (real-part-polar (contents z))
    :else (throw (ex-info "Unknown represetation" {:z z}))))
(defn imag-part [z]
  (cond
    (rectangular? z) (imag-part-rectangular (contents z))
    (polar? z) (imag-part-polar (contents z))
    :else (throw (ex-info "Unknown represetation" {:z z}))))
(defn magnitude [z]
  (cond
    (rectangular? z) (magnitude-rectangular (contents z))
    (polar? z) (magnitude-polar (contents z))
    :else (throw (ex-info "Unknown represetation" {:z z}))))
(defn angle [z]
  (cond
    (rectangular? z) (angle-rectangular (contents z))
    (polar? z) (angle-polar (contents z))
    :else (throw (ex-info "Unknown represetation" {:z z}))))

;; add 2 constructors - each one handy for a particular situation
(defn make-from-real-imag [x y]
  (make-from-real-imag-rectangular x y))

(defn make-from-mag-ang [x y]
  (make-from-mag-ang-polar x y))

;; Now test both representations
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
;; => [:rectangular [22 33]]

(def alysi (make-from-mag-ang 3.605551275463989 0.982793723247329))
(real-part alysi)
;; => 2.0
(imag-part alysi)
;; => 3.0
(magnitude alysi)
;; => 3.605551275463989
(angle alysi)
;; => 0.982793723247329


;;; 2.4.3 Data-Directed Programming and Additivity (p. 179 - 187)
;;; The issue with previous solution is that Alyssa's and Ben's implementations can easilly coexist
;;; in the same system (without having different names) and also that we need to modify the generic
;;; procedures (real-part, imag-part, magnitude, angle) every time we add a new implementation
;;;
;;; In this section we try to use something different to overcome this limitations
;;; => use a table of operations

;; First assume we have the table of operations and two procedurs `get` and `put`:
(def ^:private op-table (atom {}))

(defn put-op [op typ item]
  (swap! op-table assoc [op typ] item))

(defn get-op [op typ]
  (get @op-table [op typ]))

;; Now install the two implementations
(defn install-rectangular-package []
  (letfn [;; internal procedures
          (real-part [z] (first z))
          (imag-part [z] (second z))
          (magnitude [z] (Math/sqrt (+ (c/square (real-part z))
                                       (c/square (imag-part z)))))
          (angle [z] (Math/atan (/ (imag-part z)
                                   (real-part z))))
          (make-from-real-imag [real imag] [real imag])
          (make-from-mag-ang [mag ang] [(* mag (Math/cos ang)) (* mag (Math/sin ang))])
          ;; interface to the rest of the system
          (tag [x] (attach-tag :rectangular x))]
    (put-op :real-part [:rectangular] real-part)
    (put-op :imag-part [:rectangular] imag-part)
    (put-op :magnitude [:rectangular] magnitude)
    (put-op :angle [:rectangular] angle)
    (put-op :make-from-real-imag [:rectangular]
            (fn [x y] (tag (make-from-real-imag x y))))
    (put-op :make-from-mag-ang [:rectangular]
            (fn [x y] (tag (make-from-mag-ang x y))))
    :done))
(install-rectangular-package)
((get-op :make-from-real-imag [:rectangular]) 10 20)
;; => [:rectangular [10 20]]

(defn install-polar-package []
  (letfn [;; internal procedures
          (real-part [z] (* (magnitude-polar z) (Math/cos (angle-polar z))))
          (imag-part [z] (* (magnitude-polar z) (Math/sin (angle-polar z))))
          (magnitude [z] (first z))
          (angle [z] (second z))
          (make-from-real-imag [real imag] [(Math/sqrt (+ (c/square real) (c/square imag)))
                                            (Math/atan (/ imag real))])
          (make-from-mag-ang [mag ang] [mag ang])
          ;; interface to the rest of the system
          (tag [x] (attach-tag :polar x))]
    (put-op :real-part [:polar] real-part)
    (put-op :imag-part [:polar] imag-part)
    (put-op :magnitude [:polar] magnitude)
    (put-op :angle [:polar] angle)
    (put-op :make-from-real-imag [:polar]
            (fn [x y] (tag (make-from-real-imag x y))))
    (put-op :make-from-mag-ang [:polar]
            (fn [x y] (tag (make-from-mag-ang x y))))
    :done))
(install-polar-package)
((get-op :make-from-real-imag [:rectangular]) 10 20)
((get-op :make-from-real-imag [:polar]) 10 20)
;; => [:polar [22.360679774997898 1.1071487177940904]]

;; now we need to define the `apply-generic` procedure which uses the table
;; and applies proper procedure to all the arguments
(defn apply-generic [op & args]
  (let [type-tags (map type-tag args)
        proc (get-op op type-tags)]
    (if proc
      (apply proc (map contents args))
      (throw (ex-info "No method for types" {:op op
                                             :types type-tags})))))

;; and we can define our generic selectors:
(defn real-part [z] (apply-generic :real-part z))
(defn imag-part [z] (apply-generic :imag-part z))
(defn magnitude [z] (apply-generic :magnitude z))
(defn angle [z] (apply-generic :angle z))

;; and we can also define constructors for external users
;; Notice we use the more suitable representation based on the actual constructor used.
(defn make-from-real-imag [real imag]
  ((get-op :make-from-real-imag [:rectangular])
   real
   imag))
(defn make-from-mag-ang [mag ang]
  ((get-op :make-from-mag-ang [:polar])
   mag
   ang))
(real-part (make-from-real-imag 10 20))
;; => 10
