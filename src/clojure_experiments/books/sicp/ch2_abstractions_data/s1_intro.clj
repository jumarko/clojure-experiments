(ns clojure-experiments.books.sicp.ch2-abstractions-data.s1-intro
  "This is the section 2.1 Introduction to Data Abstraction.
  It demonstrates the technique 'constructors & selectors' using rational numbers as an example
  (sections 2.1.1 and 2.1.2 - Abstraction Barriers).
  2.1.4 contains an extended exercise - Interval Arithmetic."
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :refer [square sqrt]]))


;;; 2.1.1 - Rational Numbers (p. 83)
;;; We try to implement operations on rational numbers
;;; starting just by 'wishful thinking' - that is not caring about particular implementation,
;;; rather assuming that we have basic functions already available to us:

;; this is just a first representation that came to my mind...
;; I added it because I wanted to write tests early on
(defn make-rat [n d]
  {:num n :den d})
(defn numer [x]
  (:num x))
(defn denom [x]
  (:den x))

;;---------------------
;; now implement basic operations on rational numbers
(defn add-rat [d1 d2]
  (/ (+ (* (numer d1) (denom d2))
      (* (numer d2) (denom d1))
      )
     (* (denom d1) (denom d2))))

;; Now, how can I implement a test?
;; => first implementation try with maps
(defmacro test-rat [rat-operation & nums]
  `(are [expected n1 d1 n2 d2] (= expected (~rat-operation (make-rat n1 d1) (make-rat n2 d2)))
     ~@nums))

(deftest test-add
  (testing "simple add"
    (test-rat add-rat
      3/6 1 3 1 6
      1/4 1 8 1 8)))

(defn sub-rat [d1 d2]
  (/ (- (* (numer d1) (denom d2))
        (* (numer d2) (denom d1))
        )
     (* (denom d1) (denom d2))))
(deftest test-sub
  (testing "simple subtraction"
    (test-rat sub-rat
      1/6 1 3 1 6
      -1/8 1 8 2 8)))

(defn mult-rat [d1 d2]
  (/ (* (numer d1) (numer d2))
     (* (denom d1) (denom d2))) )
(deftest test-mult
  (testing "simple multiplication"
    (test-rat mult-rat
              1/18 1 3 1 6
              2/64 1 8 2 8
              -15/4 -3 2 5 2)))

(defn div-rat [d1 d2]
  (/ (* (numer d1) (denom d2))
     (* (numer d2) (denom d1))))
(deftest test-div
  (testing "simple division"
    (test-rat div-rat
              2 1 3 1 6
              1/2 1 8 2 8
              -3/5 -3 2 5 2)))

;; equal-rat? procedure (although I didnt' use it yet)
(defn equal-rat? [x y]
  (= (* (numer x) (denom y))
     (* (numer y) (denom x))))
(equal-rat? (make-rat 2 3)
            (make-rat 4 6))
;; => true

;; Pairs - Concrete representation of rational numbers

(defn make-rat [n d]
  (cons n (cons d ())))
(make-rat 1 2)
;; => (1 2)
(defn numer [x]
  (first x))
(numer (make-rat 1 2))
;; => 1
(defn denom [x]
  (first (rest x)))
(denom (make-rat 1 2))
;; => 2


;; Rat. numbers representation
(defn print-rat [x]
  (print  "\n" (numer x) "/"  (denom x)))
(print-rat (make-rat 2 3))


;; Let's improve our representation by reducing rational numbers to the least common denominator:
(defn gcd [x y]
  (if (zero? y)
    x
    (recur y (mod x y))))

(defn make-rat [n d]
  (let [g (gcd n d)]
    (cons (/ n g) (cons (/ d g) ()))))

;; we may also defer gcd computation until numer/denom is called
(comment

  (defn numer [x]
    (let [g (gcd (first x) (first (rest x)))]
      (/ (first x) g)))
  (numer (make-rat 3 9))
  ;; => 1

  (defn denom [x]
    (let [g (gcd (first x) (first (rest x)))]
      (/ (first (rest x)) g)))
  (denom (make-rat 3 9))
  ;; => 3
  )


;; Exercise 2.1 (p. 87)
;; Define a better `make-rat` that handles positive and negative args
;; it should normmalize the sign:
;; - if number is positive both num. and den. are positive
;; - if number is negative then only num. is negative
(make-rat -3 7)
;; => (-3 7)
(make-rat 3 -7)
;; => (-3 7)
(make-rat -3 -7)
;; => (3 7)
;;=> Funny enough, this is already done thanks to the `mod` implementation in Clojure


;;; 2.1.2 Abstraction Barriers
;;; add-rat, sub-rat, mult-rat, div-rat makes higher-level abstraction barrier
;;; make-rat, numer, denom are another lower-level barrier
;;; even more level is cons, car, cdr,
;;; etc.

;;; Exercise 2.2. (p. 89)
;;; Constructors and selectors for line segments and their start/end points:
;;; Solutions: http://community.schemewiki.org/?sicp-ex-2.2
(defn make-point [x y]
  [x y])
(defn x-point [point] (first point))
(defn y-point [point] (second point))

(defn make-segment [start-point end-point]
  [start-point end-point])
(defn start-segment [segment] (first segment))
(defn end-segment [segment] (second segment))

(defn- avg [x y]
  (/ (+ x y)  2))
(defn midpoint-segment [segment]
  (let [start (start-segment segment)
        end (end-segment segment)]
    (make-point (avg (x-point start) (x-point end))
                (avg (y-point start) (y-point end)))))

(defn print-point [point]
  (printf "\n(%d,%d)" (x-point point) (y-point point)))

(def midpoint (midpoint-segment
               (make-segment (make-point 20 100)
                             (make-point 40 70))))
#_(print-point midpoint)
;;=> (30,85)

;; To be fair, the implementation above is quite complex without having any obvious benefits
;; => More conventional  Clojure way:

(defn midpoint-segment [[[start-x start-y :as segment-start]
                         [end-x end-y :as segment-end]]]
  [(avg start-x end-x) (avg start-y end-y)])

(def midpoint (midpoint-segment [[20 100]
                                 [40 70]]))
#_(print-point midpoint)
;;=> (30,85)

;; but hashmap-based impl would be even better!
(defn midpoint-segment [{:keys [start end]}]
  [(avg (:x start) (:x end))
   (avg (:y start) (:y end))])

(def midpoint (midpoint-segment {:start {:x 20 :y 100}
                                 :end {:x 40 :y 70}}
                                ))
#_(print-point midpoint)
;;=> (30,85)


;; From solutions: interesting alternative: http://community.schemewiki.org/?sicp-ex-2.2
;; > I think the above solution misses part of the point about abstraction barriers; midpoint-segment reaches through both layers to achieve its goal.
(defn average-points [a b]
  (make-point (avg (x-point a) (x-point b)) 
              (avg (y-point a) (y-point b)))) 

(defn midpoint-segment [seg] 
  (average-points (start-segment seg) 
                  (end-segment seg))) 


;;; Ex. 2.3. Rectangles
;;; Using above 'segments' abstraction build 'rectangles' on top of that.
;;; Implement representation-independent functions `perimeter` and `area`
;;; that don't change if you change your representation of rectangles (try that!)
;;; Solutions here: http://community.schemewiki.org/?sicp-ex-2.3 (not very interesting)
(defn make-rect
  "Makes a new rectangle consisting given line segments."
  [a b c d]
  [a b c d])
(defn rect-a [rect] (first rect))
(defn rect-b [rect] (second rect))
(defn rect-c [rect] (nth rect 2))
(defn rect-d [rect] (last rect))

;; helper functions for perimeter and area
(defn- abs [x] (if (pos? x) x (- x)))

(defn- length [segment]
  (let [start (start-segment segment)
        end (end-segment segment)
        x-diff (abs (- (x-point start) (x-point end)))
        y-diff (abs (- (y-point start) (y-point end)))]
    (sqrt (+ (square x-diff)
             (square y-diff)))))
(length (make-segment (make-point 0 0)
                      (make-point 8 6)))
;; => 10.0000000001399

(defn perimeter [rect]
  (+ (length (rect-a rect))
     (length (rect-b rect))
     (length (rect-c rect))
     (length (rect-d rect))))

(def my-rectangle (make-rect
                    (make-segment (make-point 0 0)
                                  (make-point 8 6))
                    (make-segment (make-point 8 6)
                                  (make-point 2 12))
                    (make-segment (make-point 2 12)
                                  (make-point -6 6))
                    (make-segment (make-point -6 6)
                                  (make-point 0 0))))
(perimeter my-rectangle)
;; => 36.970571637343554

;; No different representation
(defn make-rect
  "Makes a new rectangle consisting given line segments."
  [a b c d]
  {:a a :b b :c c :d d})
(defn rect-a [rect] (:a rect))
(defn rect-b [rect] (:b rect))
(defn rect-c [rect] (:c rect))
(defn rect-d [rect] (:d rect))

(def my-rectangle2 (make-rect
                   (make-segment (make-point 0 0)
                                 (make-point 8 6))
                   (make-segment (make-point 8 6)
                                 (make-point 2 12))
                   (make-segment (make-point 2 12)
                                 (make-point -6 6))
                   (make-segment (make-point -6 6)
                                 (make-point 0 0))))
(perimeter my-rectangle2)
;; => 36.970571637343554


;; Now try to use more idiomatic Clojure solution (leave out accessors and constructors)
;; 

;; helper functions for perimeter and area
(defn- length [segment]
  (let [start (:start segment)
        end (:end segment)
        x-diff (abs (- (:x start) (:x end)))
        y-diff (abs (- (:y start) (:y end)))]
    (sqrt (+ (square x-diff)
             (square y-diff)))))
(length {:start {:x 0 :y 0}
         :end {:x 8 :y 6}})
;; => 10.0000000001399

(defn perimeter3 [{:keys [a b c d] :as _rect}]
  (+ (length a) (length b) (length c) (length d)))

(def my-rectangle3 {:a {:start {:x 0 :y 0}
                       :end {:x 8 :y 6}}
                   :b {:start {:x 8 :y 6}
                       :end {:x 2 :y 12}}
                   :c {:start {:x 2 :y 12}
                       :end {:x -6 :y 6}}
                   :d {:start {:x -6 :y 6}
                       :end {:x 0 :y 0}}}
  )
(perimeter3 my-rectangle)
;; => 36.970571637343554


;;; TAKEAWAY:
;;; My take on this is that it really doesn't help much to build this constructors and selectors
;;; because you need to expose the underlying structure anyway (but you call functions instead
;;; of accessing data directly)
;;; => It's just mimicking the structure of data and probably not worth the effort in most cases
