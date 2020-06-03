(ns clojure-experiments.books.sicp.ch2-abstractions-data.s1-intro
  "This is the section 2.1 Introduction to Data Abstraction.
  It demonstrates the technique 'constructors & selectors' using rational numbers as an example
  (sections 2.1.1 and 2.1.2 - Abstraction Barriers).
  2.1.4 contains an extended exercise - Interval Arithmetic."
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements
             :refer [square sqrt]]
            ;; this is the iterative version of fast-exp to avoid StackOverlowError
            [clojure-experiments.books.sicp.ch1-abstractions-procedures.exercise :refer [fast-exp]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))


;;;; -----------------------------------------------------------------------------------------------
;;;; 2.1.1 - Rational Numbers (p. 83)
;;;; We try to implement operations on rational numbers
;;;; starting just by 'wishful thinking' - that is not caring about particular implementation,
;;;; rather assuming that we have basic functions already available to us:

;; this is just a first representation that came to my mind...
;; I added it because I wanted to write tests early on
(defn make-rat [n d]
  {:num n :den d})
(defn numer [x]
  (:num x))
(defn denom [x]
  (:den x))

;;;---------------------
;;; now implement basic operations on rational numbers
(defn add-rat [d1 d2]
  (make-rat (+ (* (numer d1) (denom d2))
               (* (numer d2) (denom d1)))
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
  (make-rat (- (* (numer d1) (denom d2))
               (* (numer d2) (denom d1)))
            (* (denom d1) (denom d2))))
(deftest test-sub
  (testing "simple subtraction"
    (test-rat sub-rat
      1/6 1 3 1 6
      -1/8 1 8 2 8)))

(defn mult-rat [d1 d2]
  (make-rat (* (numer d1) (numer d2))
            (* (denom d1) (denom d2))))
(deftest test-mult
  (testing "simple multiplication"
    (test-rat mult-rat
              1/18 1 3 1 6
              2/64 1 8 2 8
              -15/4 -3 2 5 2)))

(defn div-rat [d1 d2]
  (make-rat (* (numer d1) (denom d2))
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

;;; Pairs - Concrete representation of rational numbers

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


;;; Let's improve our representation by reducing rational numbers to the least common denominator:
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


;;; Exercise 2.1 (p. 87)
;;; Define a better `make-rat` that handles positive and negative args
;;; it should normmalize the sign:
;;; - if number is positive both num. and den. are positive
;;; - if number is negative then only num. is negative
(make-rat -3 7)
;; => (-3 7)
(make-rat 3 -7)
;; => (-3 7)
(make-rat -3 -7)
;; => (3 7)
;;=> Funny enough, this is already done thanks to the `mod` implementation in Clojure


;;;; -----------------------------------------------------------------------------------------------
;;;; 2.1.2 Abstraction Barriers
;;;; add-rat, sub-rat, mult-rat, div-rat makes higher-level abstraction barrier
;;;; make-rat, numer, denom are another lower-level barrier
;;;; even more level is cons, car, cdr,
;;;; etc.

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
                        :end {:x 0 :y 0}}})
(perimeter3 my-rectangle3)
;; => 36.970571637343554


;;; TAKEAWAY:
;;; My take on this is that it really doesn't help much to build this constructors and selectors
;;; because you need to expose the underlying structure anyway (but you call functions instead
;;; of accessing data directly)
;;; => It's just mimicking the structure of data and probably not worth the effort in most cases


;;;; 2.1.3 What is Meant by Data? (p. 90)
;;;; Interesting section showing that we can represent data completely via procedures + conditions
;;;; such as make-rat, numer, and denom fns satisfying condition:   (numer x) / (denom x) = n / d;
;;;;     where x = (make-rat n d)

;;; Example of such implementation of pairs completely via procedures
;;; defining functions cons, car, cdr

;; notice that `my-cons` actually return a procedure
(defn my-cons [x y]
  (fn dispatch [m]
    (case m
      0 x
      1 y)))

(defn my-car [z] (z 0))

(defn my-cdr [z] (z 1))

(my-car (my-cons 1 2))
(my-cdr (my-cons 1 2))
(my-car (my-cons 10 (my-cons 1 2)))
;; => 10



;;; Ex. 2.4 (p. 92)
;;; alternative representation of pairs
(defn my-cons [x y]
  (fn [m] (m x y)))
(defn my-car [z]
  (z (fn [p q] p)))
;; Implement cdr
(defn my-cdr [z]
  (z (fn [p q] q)))

(my-car (my-cons 1 2))
;; => 1
(my-car (my-cons 10 (my-cons 1 2)))
;; => 10
(my-car (my-cons (fn a [x])
                 (fn b [x])))
;; notice `a--...` at the end (`a` is the name of the fn)
;; => #function[clojure-experiments.books.sicp.ch2-abstractions-data.s1-intro/eval26230/a--26231]

;; how to use substitution model to prove this?
(my-car (my-cons 1 2))
;; =>
((my-cons 1 2) (fn [p q] p))
;; => 
((fn [m] (m  1 2)) (fn [p q] p))
;; =>
((fn [p q] p) 1 2)
;; =>
1

;;; Ex 2.5. (p. 92)
;;; Show that we can represent a pair of non-negative numbers `a` and `b` as a single number 
;;; 2^a * 3^b
;;; Resources
;;; - https://www.mathsisfun.com/prime-factorization.html
;;; - https://www.geeksforgeeks.org/print-all-prime-factors-of-a-given-number/

;; First a generative test
(defspec pairs-as-number 10
  (prop/for-all [[x y] (gen/tuple gen/small-integer)]
                (let [my-pair (my-cons x y)]
                  (and (= x (my-car my-pair))
                       (= y (my-cdr my-pair))))))

;; then implementations of cons, car, cdr
(defn my-cons [x y]
  (*' (fast-exp 2 x)
      (fast-exp 3 y)))

;; helper function needed to find prime factors and corresponding exponents
(defn- factor-exponent [z factor]
  (loop [n z
         exp 0]
    (if (zero? (mod n factor))
      (recur (quot n factor)
             (inc exp))
      exp)))

(defn my-car [z]
  (factor-exponent z 2))
(my-car (my-cons 10 9))
;; => 10

(defn my-cdr [z]
  (factor-exponent z 3))
(my-cdr (my-cons 10 9))
;; => 9

;; this representaiton doesn't play well with huge numbers :(
;; takes way too long (and much longer for even larger numbers)
#_(time (my-car (my-cons 102401 120)))
;; "Elapsed time: 4017.907974 msecs"


;;; Ex 2.6. Church Numerals (p. 93)
;;; Check http://community.schemewiki.org/?sicp-ex-2.6
;;; This is quite hard :(

(defn zero [f]
  (fn [x] x))
((zero identity) 0)

(defn add-1 [n]
  (fn [f]
    (fn [x] (f ((n f) x)))))

(defn one [f]
  (fn [x] (f x)))
(defn two [f]
  (fn [x] (f (f x))))
(defn three [f]
  (fn [x] (f (f (f x)))))

;; how to test? => use `inc` function!
(defn church-to-int [cn]
  ((cn inc) 0))
(church-to-int three)
;; => 3
(church-to-int (add-1 (add-1 zero)))
;; => 2

(defn plus [a b]
  (fn [f]
    (fn [x]
      ((a f) ((b f) x)))))

(church-to-int (plus two three))
;; => 5

((two square) 2)
;; => 16

(((plus two one) square) 2)
;; => 256 (that is 2^(square^3) )




;;;; -----------------------------------------------------------------------------------------------
;;;; 2.1.4 Interval Arithmetic (p. 93)
;;;;
(defn- make-interval [l u]
  [l u])

;;; This is actually an exercise 2.7
(defn- lower-bound [x]
  (first x))
(defn- upper-bound [x]
  (second x))

(defn add-interval [x y]
  (make-interval
   (+ (lower-bound x) (lower-bound y))
   (+ (upper-bound x) (upper-bound y))))

(add-interval (make-interval 8 10)
              (make-interval 7.2 11.4))
;; => [15.2 21.4]

(defn mult-interval [x y]
  (let [p1 (* (lower-bound x) (lower-bound y))
        p2 (* (lower-bound x) (upper-bound y))
        p3 (* (upper-bound x) (lower-bound y))
        p4 (* (upper-bound x) (upper-bound y))]
    (make-interval
     (min p1 p2 p3 p4)
     (max p1 p2 p3 p4))))

(mult-interval (make-interval 8 10)
              (make-interval 4 11))
;; => [32 110]

;; my div-interval
;; => this is actually incorrect for negative numbers?
(defn div-interval [x y]
  (make-interval
   (/ (lower-bound x) (upper-bound y))
   (/ (upper-bound x) (lower-bound y))))

(div-interval (make-interval 1 4)
              (make-interval 10 40))
;; => [1/40 2/5]

;; But this is Incorrect!!!
(div-interval (make-interval -4 4)
              (make-interval -10 40))
;; => [-1/10 -2/5]

;; div-interval from the book
(defn div-interval [x y]
  (mult-interval
   x
   (make-interval
    (/ 1.0 (upper-bound y))
    (/ 1.0 (lower-bound y)))))

(div-interval (make-interval 1 4)
              (make-interval 10 40))
;; => [0.025 0.4] (the same thing as above)

;; But this gives correct result!!
(div-interval (make-interval -4 4)
              (make-interval -10 40))
;; => [-0.4 0.4]

;;; Exercise 2.9 (p. 95)
;;; sub-interval
(defn sub-interval [x y]
  (make-interval
   (- (lower-bound x) (upper-bound y))
   (- (upper-bound x) (lower-bound y))))

(sub-interval (make-interval 8 10)
              (make-interval 4 11))
;; => [-3 6]

;; is alternative implementation possible?
(defn sub-interval [x y]
  (add-interval
   x
   (make-interval
    (- (upper-bound y))
    (- (lower-bound y)))))
(sub-interval (make-interval 8 10)
              (make-interval 4 11))
;; => [-3 6]


;;; 2.9. (p. 95)
(defn interval-width [x]
  (/ (- (upper-bound x) (lower-bound x))
     2))
(interval-width (make-interval 40 50))
;; => 5
(interval-width (make-interval 1 2))
;; => 1/2

;; width of the interval is really just sum of them
(interval-width (add-interval (make-interval 40 50)
                              (make-interval 1 2)))
;; => 11/2

;; and the same holds for subtraction!
(interval-width (sub-interval (make-interval 40 50)
                              (make-interval 1 2)))
;; => 11/2


;; ... but not multiplication and division
(interval-width (mult-interval (make-interval 40 50)
                               (make-interval 1 2)))
;; => 30

(interval-width (div-interval (make-interval 40 50)
                              (make-interval 1 2)))
;; => 15.0


;;; Ex. 2.10 (p. 95)
;;; What does it mean to divide by an interval that spans zero?

(defn div-interval [x y]
  (when (or (zero? (upper-bound y))
            (zero? (lower-bound y)))
    (throw (IllegalArgumentException. (str "second interval cannot span zero!" y))))
  (mult-interval
   x
   (make-interval
    (/ 1.0 (upper-bound y))
    (/ 1.0 (lower-bound y)))))

#_(div-interval (make-interval 1 4)
              (make-interval 0 40))


;;; Ex. 2.11 (p. 95)
;;; By testing the signs of endpoints of an interval it's possible to break down
;;; `mult-interval` into 9 cases only one of which requires more than 2 multiplications

;; try to start by enumerating all posibilities:

;; 1. everything positive
(mult-interval
 (make-interval 1 2)
 (make-interval 10 15))
;; => [10 30]  (x_l * y_l; x_h * y_h)

;; 2. everything negative
(mult-interval
 (make-interval -2 -1)
 (make-interval -15 -10))
;; => [10 30] (x_h * y_h; x_l * y_l)

;; 3. x_l negative
(mult-interval
 (make-interval -1 2)
 (make-interval 10 15))
;; => [-15 30]  (x_l * y_h; x_h * y_h)

;; 4. y_l negative
(mult-interval
 (make-interval 1 2)
 (make-interval -10 15))
;; => [-20 30] (x_h * y_l; x_h * y_h)

;; 5. x_l and x_h negative
(mult-interval
 (make-interval -2 -1)
 (make-interval 10 15))
;; => [-30 -10] (x_l * y_h; x_h * y_l)

;; 6. y_l and y_h negative
(mult-interval
 (make-interval 1 2)
 (make-interval -15 -10))
;; => [-30 -10] (x_h * y_l; x_l * y_h)

;; 7. x_l, y_l negative
(mult-interval
 (make-interval -1 2)
 (make-interval -10 15))
;; => [-20 30] (x_h * y_l; x_h * y_h)

;; 8. x_l, y_l, y_h negative
(mult-interval
 (make-interval -1 2)
 (make-interval -15 -10))
;; => [-30 15] (x_h * y_l; x_l * y_l)

;; 9. x_l, x_h, y_l negative
(mult-interval
 (make-interval -2 -1)
 (make-interval -10 15))
;; => [-30 20] (x_l * y_h; x_l * y_l)


(defn ben-mult-interval [x y]
  (let [xl (lower-bound x)
        xh (upper-bound x)
        yl (lower-bound y)
        yh (upper-bound y)
        p1 (* xl yl)
        p2 (* xl yh)
        p3 (* xh yl)
        p4 (* xh yh)]
    (make-interval
     (min p1 p2 p3 p4)
     (max p1 p2 p3 p4))

    (cond
      (every? pos? [xl xh yl yh])
      (make-interval (* xl yl) (* xh yh))

      (and (neg? xl) (neg? xh) (neg? yl) (neg? yh))
      (make-interval (* xh yh) (* xl yl))

      (and (neg? xl) (pos? xh) (pos? yl) (pos? yh))
      (make-interval (* xl yh) (* xh yh))

      (and (pos? xl) (pos? xh) (neg? yl) (pos? yh))
      (make-interval (* xh yl) (* xh yh))

      (and (neg? xl) (neg? xh) (pos? yl) (pos? yh))
      (make-interval (* xl yh) (* xh yl))

      (and (pos? xl) (pos? xh) (neg? yl) (neg? yh))
      (make-interval (* xl yh) (* xh yl))

      ;;; 
      ;;; too much busy work; check the solutions
      ))
  )


;;; dealing with center values and additive tolerance (p. 95)

;;; Ex. 2.12
(defn make-center-width [c w]
  (make-interval (- c w) (+ c w)))

(defn center [i]
  (/ (+ (lower-bound i) (upper-bound i))
     2))

(defn width [i]
  (/ (- (upper-bound i) (lower-bound i))
     2))

(width (make-center-width 0.5 0.1))
;; => 0.09999999999999998
(width (make-interval 0.4 0.6))
;; => 0.09999999999999998
(center (make-interval 0.4 0.6))
;; => 0.5
(center (make-center-width 0.5 0.1))
;; => 0.5

(defn make-center-percent [c p]
  (make-center-width c (double (* c (/ p 100)))))

(make-center-percent 0.5 20)
;; => [0.4 0.6]

(defn percent [i]
  (* 100
     (/ (width i)
        (center i))))
(percent (make-center-percent 0.5 20))
;; => 19.999999999999996

;;; Ex. 2.13 (p. 96)
;;; Show that under the assumption of small percentage tolerances
;;; there's a simple formula for a product of two intervals in terms of the tolerances of the factors

;; simple guess
;; let's say that intervals x, y defined by
;; x = 10 +- 0.01 (1%)
;; y = 8 +- 0.02
;; x * y = 10 * 8 +- ???
;; where ??? could be ?
;; ....
[(* 9.9 7.84) (* 10.1 8.16)]
;; => [77.616 82.416]
(apply - [(* 10.1 8.16) (* 9.9 7.84)])
;; => 4.799999999999997
;; 4.8 is the result of what operation??
(/ 4.8 80)
;; => 0.06 (6 %)
;; could it be 2 * (x_tolerance + y_tolerance)
;; that is in our case 2 * (0.01 + 0.02) ?
(percent [9.9 10.1])
;; => 0.9999999999999963
(defn product-with-small-tolerance [x y]
  (let [product-center (* (center x) (center y))
        product-percent (+ (percent x) (percent y))]
    (make-center-percent product-center product-percent)))

;; THe result of this is almost the same as our handwritten computation
;; -> see line 759
(product-with-small-tolerance
 (make-center-percent 10 1)
 (make-center-percent 8 2))
;; => [77.6 82.4]



;;; Ex. 2.14 and 2.15 (p. 96-97)
;;; Issues with equivalent algebraic representations
;;; (R1*R2) / (R1+R2)
;;; Vs.
;;; 1 / (1/R1 + 1/R2)
(defn par1 [r1 r2]
  (div-interval (mult-interval r1 r2)
                (add-interval r1 r2)))
(def par1-result (par1 (make-center-percent 10 1)
                       (make-center-percent 8 2)))
;; => [4.250602409638555 4.645772266065388]
(percent par1-result)
;; => 4.441920117259056

(defn par2 [r1 r2]
  (let [interval-1 (make-interval 1 1)]
    (div-interval interval-1
                  (add-interval (div-interval interval-1
                                              r1)
                                (div-interval interval-1
                                              r2)))))
(def par2-result (par2 (make-center-percent 10 1)
                       (make-center-percent 8 2)))
;; => [4.375197294250282 4.513472070098576]
(percent par2-result)
;; => 1.5556296469176119

;;=> it seems that `par2` produces 'tighter' error interval
;; that means (ex. 2.15) that Eva Lu Ator could be right
;; that a formula where no variable representing an interval (uncertain number)
;; is repeated is BETTER than formulas with repeated variables
;; WHY?
;; That might be true because when we don't repeat variables we have less number of "uncertain" operations
;; e.g. with R1 * R2 / (R1 + R2) we have 3 operations on uncertain numbers
;; whereas with 1 / (1/R1 + 1/R2) we have only 1 operaiton (that is +) because [1 1] interval is "precise"


;; 2.16: http://wiki.drewhess.com/wiki/SICP_exercise_2.16
;; I didn't bother to solve this but it's interesting explanation
;; that, in general, equivalent algebraic expressions using Interval arithmetic may give different answers!
