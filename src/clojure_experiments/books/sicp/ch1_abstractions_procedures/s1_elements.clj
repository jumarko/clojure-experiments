(ns clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements)

;;; 1.1.4 Compound Procedures

(defn square [x] (* x x))

(defn sum-of-squares [x y]
  (+ (square x) (square y)))

(sum-of-squares 3 4)

(defn f [a]
  (sum-of-squares (+ a 1) (* a 2)))

(f 5)

(defn abs [x]
  (if (< x 0) (- x) x))
(abs 10)
(abs 0)
(abs -10)

;;; 1.1.7 Example: Square Roots by Newton's Method
;; the first quite bad implementation of `good-enough?`
;; it's bad because it doesn't really for for either small or large numbers
(defn good-enough? [guess x]
  (< (abs (- (square guess)
             x))
     0.001))

(defn avg [x y]
  (/ (+ x y) 2))

(defn improve [guess x]
  (avg guess (/ x guess)))

(defn sqrt-iter
  [guess x]
  (if (good-enough? guess x)
    guess
    (sqrt-iter (improve guess x)
               x)))
(defn sqrt [x]
  (-> (sqrt-iter 1 x)
      double))

(sqrt 9)

(sqrt (+ 100 37))

(sqrt (+ (sqrt 2) (sqrt 3)))

(square (sqrt 1000))


;;; 1.2.1 Factorials and recursion

;; one way to compute factorial is a simple recursion:


(defn fact [n]
  (if (<= n 1)
    1
    (* n (fact (dec n)))))
(fact 0)
;; => 1
(fact 5)
;; => 120

;; we can also take a different perspective
;; using an intereative approach: 1 * 2 * 3 * 4 * 5
;; Note: that in the book they also use `counter` and `max-count` arguments
;; but that's not strictly necessary for our computation
(defn facti [n]
  (letfn [(fact-iter [acc n]
            (if (<= n 0)
              acc
              (fact-iter (* n acc)
                         (dec n))))]
    (fact-iter 1 n)))
(facti 0)
;; => 1
(facti 5)
;; => 120




;;; 1.2.2 Tree Recursion (Fibonacci numbers et al.)

;; fibonacci numbers can be computed via straightforward recursive procedure:
(defn fibr [n]
  (cond
    (zero? n) 0
    (= 1 n) 1
    (< 1 n) (+ (fibr (- n 1))
               (fibr (- n 2)))))

(fibr 0)
(fibr 1)
(fibr 7)
;; => 13
;; Notice it takes a while to compute fibonacci even for relatively small n
#_(time (fibr 35))
;; => "Elapsed time: 2837.268809 msecs"

;; Compare this to iterative approach
(defn fibi [n]
  (loop [a 1
         b 0
         counter n]
    (if (zero? counter)
      b
      ;; for larger n-s you'd have to use `+'` here to avoid integer overflow
      (recur (+' a b) a (dec counter)))))

(fibi 0)
(fibi 1)
(fibi 7)
;; and it's much faster too:
(time (fibi 35))
;; => "Elapsed time: 0.0512 msecs"
(time (fibi 100))
;; => "Elapsed time: 0.167737 msecs"
#_(time (fibi 500000))
;; => "Elapsed time: 7483.735318 msecs"


;;; 1.2.2 Example: Counting Change (p. 40)
;;; Write a procedure to compute the number of ways to change ani given amount of money
;;; given that we have half-dollars, quarters, dimes, nickles, pennies
(defn- first-denomination [kinds-of-coins]
  (condp = kinds-of-coins
    1 1
    2 5
    3 10
    4 25
    5 50))

(defn change
  ([amount] (change amount 5))
  ([amount kinds-of-coins]
   (cond
     (zero? amount)
     1

     (or (neg? amount) (zero? kinds-of-coins))
     0

     :else
     (+ (change amount
                (dec kinds-of-coins))
        (change (- amount (first-denomination kinds-of-coins))
                kinds-of-coins)))))

(change 100)
;; => 292




