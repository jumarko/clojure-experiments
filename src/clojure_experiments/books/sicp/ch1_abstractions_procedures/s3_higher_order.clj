(ns clojure-experiments.books.sicp.ch1-abstractions-procedures.s3-higher-order
  "Higher order procedures.
  Video Lecture 2A: https://www.youtube.com/watch?v=erHp3r6PbJk&list=PLE18841CABEA24090&index=3"
  (:require
   [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :as c]
   [clojure-experiments.books.sicp.ch1-abstractions-procedures.s2-procedures-and-processes :as c2]
   [clojure-experiments.books.sicp.ch1-abstractions-procedures.exercise :as e]))

;;; Similar patterns to refactor...
(defn sum-ints
  "Sum ints from a to b (inclusively)."
  [a b]
  (if (> a b)
    0
    (+ a (sum-ints (inc a) b))))
(sum-ints 1 10)
;; => 55

(defn sum-sq
  "Sum ints from a to b (inclusively)."
  [a b]
  (if (> a b)
    0
    (+ (c/square a)
       (sum-sq (inc a) b))))
(sum-sq 2 5)
;; => 54

(defn pi-sum
  "Computers a sequence `pi/8 = 1/1*3 + 1/5*7 + 1/9*11 + ...`.
  This is an interesting sequence originally discovered by Leibnitz
  and usually written as `pi/4 = 1 - 1/3 + 1/5 - 1/7 + 1/9 ...`."
  [a b]
  (if (> a b)
    0
    (+
     (/ 1 (* a (+ a 2)))
     (pi-sum (+ a 4) b))))

;; coerce to double to get more familiar result
(double (* 8 (pi-sum 1 1000)))
;; => 3.139592655589783


(defn sum
  "General summing procedure.
  `term` computes the current value (e.g. square root)
  `a` is the start of the interval
  `next` next is successing function (e.g. increment)
  `b` is the end of the interval (inclusive)"
  [term a next b]
  (if (> a b)
    0
    (+ (term a)
       (sum term (next a) next b))))
;; now let's redefine our former procedures
(defn sum-ints [a b]
  (sum identity a inc b))
(sum-ints 1 10)
;; => 55

(defn sum-sq [a b]
  (sum c/square a inc b))
(sum-sq 2 5)
;; => 54

(defn pi-sum [a b]
  (sum #(/ 1 (* % (+ % 2)))
       1
       #(+ % 4)
       1000))
(double (* 8 (pi-sum 1 1000)))
;; => 3.139592655589783



;;; Let's define sqrt in terms of general fixed-point function


(defn fixed-point [improve-fn start]
  (letfn [(iter [old new]
            (if (e/better-good-enough? old new)
              new
              (iter new (improve-fn new))))]
    (iter start (improve-fn start))))

;; better alternative in clojure?
(defn fixed-point [improve-fn start]
  (reduce #(if (e/better-good-enough? %1 %2)
             (reduced %2)
             %2)
          (iterate improve-fn start)))

(defn average-damp [f]
  (fn [x] (c/avg (f x) x)))

(defn sqrt [x]
  (fixed-point (average-damp (fn [y] (/ x y)))
               1))

(double (sqrt 4))
(double (sqrt 9))
(double (sqrt 100))

;;; SQRT in terms of General Newton method
;;; See the book - p. 74

;; start with sqrt & empty skeleton for newton's procedure ("wishfull thinking")
(defn newton [improve-fn guess])
(defn sqrt [x]
  (newton
   (fn [y] (- x (c/square y)))
   1))

;; now let's define newton fn:
(declare deriv)
(defn newton [f guess]
  (let [df (deriv f)]
    (fixed-point
     (fn [x] (- x
                (/ (f x) (df x))))
     guess)))

;; finally, let's implement `deriv`:
(defn deriv [f]
  (let [dx 0.00001] ;; let dx be very small number...
    (fn [x] (/ (- (f (+ x dx))
                  (f x))
               dx))))
(sqrt 4)


;;; Definite integral - (p. 59-60)

;; naive approach (p.59)


(defn integral
  "Approximates the value of definite integral of `f` between the limits `a` and `b`.
  Note: this implementation doesn't optimize tail-recursion so the dx shouldn't be smaller
  1/1000 of (b - a) )."
  [f a b dx]
  (let [add-dx #(+ % dx)]
    (* (sum f
            (+ a (/ dx 2))
            add-dx
            b)
       dx)))

(integral e/cube 0 1 0.01)
;; => 0.24998750000000042
(integral e/cube 0 1 0.001)
;; => 0.249999875000001
;; (integral e/cube 0 1 0.0001) => throws StackOverflow


;; Ex. 1.29: simpson's integral
;; The hard thing about this exercise are alternating factors (4 and 2)
;; which don't play nicely with the `term` and `next` functions used in the helper `sum` procedure.
;; The realization you need to make is that you can actually derive the proper coefficient (1, 4, or 2)
;; without complicating `next` function (which will be just `inc`):
(defn- coefficient [k n]
  (cond
    (or (= k 0) (= k n))
    1

    (odd? k)
    4

    (even? k)
    2))
(defn simpson-integral
  "Computes a definite integral of `f` between the limits `a` and `b` using
  the Simpson's rule.
  `n` determines number of iterations - the grater, the more accurate the result is."
  [f a b n]
  (let [h (/ (- b a) n)
        f-k (fn [k] (*
                     (coefficient k n)
                     (f (+ a (* k h)))))]
    (* (/ h 3)
       (sum f-k 0 inc n))))

(double (simpson-integral e/cube 0 1 5))
;; => 0.2032
(double (simpson-integral e/cube 0 1 20))
;; => 0.25
(double (simpson-integral e/cube 0 1 100))
;; => 0.25
(double (simpson-integral e/cube 0 1 1000))
;; => 0.25


;;; 1.30 Iterative version of `sum`
;; let's start by creating skeletons:
(defn sum-iter [acc term a next b]
  acc)
(defn sum [term a next b]
  (sum-iter 0 term a next b))
;; then we need to improve `sum-iter`
(defn sum-iter [acc term a next b]
  (if (> a b)
    acc
    ;; it' simple just add to accumulator
    (recur (+ acc (term a))
           term
           (next a)
           next
           b)))
;; now we can compute large sums
;; this would fail on StackOverflow before...
(sum-ints 1 10000)
;; => 50005000
;; Alternatively we can follow the book recommandation and save some arguments
(defn sum2 [term a next b]
  (let [iter (fn iter [a result]
               (if (> a b)
                 result
                 (recur (next a)
                        (+ result (term a)))))]
    (iter a 0)))
(sum2 identity 1 inc 10000)
;; => 50005000


;;; 1.31 product function
;;; Show how to define factorial via `product`
;;; Also computers pi approximation via John Wallis' formula pi/4 = 2 * 4 * 4 * 6 * 6 * 8 ... / (3 * 3 * 5 * 5 * 7 * 7)
(defn product-recursive [term a next b]
  (if (> a b)
    0
    (* (term a)
       (product-recursive term (next a) next b))))
(defn product-iterative [term a next b]
  (let [iter (fn iter [a result]
               (if (> a b)
                 result
                 (recur (next a)
                        (* result (term a)))))]
    (iter a 1)))
(def product product-iterative)
(defn ints-product [a b]
  (product identity a inc b))
(def factorial (partial ints-product 1))
(factorial 5)
;; => 120

(defn- pi-term [a]
  (if (odd? a)
    (/ (+ a 1) (+ a 2))
    (/ (+ a 2) (+ a 1))))
(defn pi-wallis
  "Computes approximation of pi using John Wallis' formula:
  `pi/4 = (2 * 4 * 4 * 6 * 6 * 8 * ...) / (3 * 3 * 5 * 5 * 7 * 7 * ...)`"
  [n]
  ;; we need to realize that it's just about simple transformation in the `term` function
  ;; taking into account which element we're dealing with
  (* 4
     (product pi-term 1 inc n)))
(double (pi-wallis 10))
;; => 3.275101041334808
(double (pi-wallis 100))
;; => 3.157030176455168


;;; Ex. 1.32 (p. 61)
;;; Observe that sum and product are just special cases of more general accumulate function:
(defn accumulate-recursive
  [combiner null-value term a next b]
  (if (> a b)
    null-value
    (combiner (term a)
              (accumulate-recursive combiner null-value term (next a) next b))))

(defn accumulate-iter
  "Accumulator which uses `combiner` (such as + or *) and null value (0 for sum, 1 for product, ...)
  to compute 'accumulation' of all values between a and b (inclusive)
  by calling `term` on each item of a sequence and using `next` to compute next item in the sequence.
  The most primitive example is sum of integers from 1 to 10:
    `(accumulate + 0 identity 1 inc 10)`"
  [combiner null-value term a next b]
  (let [iter (fn iter [a result]
               (if (> a b)
                 result
                 (recur (next a)
                        (combiner result (term a)))))]
    (iter a null-value)))

(def accumulate accumulate-iter)

;; sum-ints:
(accumulate + 0 identity 1 inc 10)
;; => 55
(accumulate + 0 identity 1 inc 10000)
;; => 50005000
;; factorial:
(accumulate * 1 identity 1 inc 5)
;; => 120
;; Wallis' pi approximation
;; notice that with large n like 10,000 it takes VERY LONG TIME!
(double (* 4 (accumulate * 1 pi-term 1 inc 100)))
;; => 3.157030176455168


;;; Ex. 1.33 (p.61) - filtered-accumulate

(defn filtered-accumulate [combiner null-value term a next b filter-fn]
  (let [iter (fn iter [a result]
               (if (> a b)
                 result
                 (let [next-result (if (filter-fn a)
                                     (combiner result (term a))
                                     result)]
                   (recur (next a) next-result))))]
    (iter a null-value)))

(defn sum-primes-squares [a b]
  (filtered-accumulate + 0 c2/square' a inc b c2/prime?))
(sum-primes-squares 2 5)
;; => 38

(defn relative-primes-product
  "Computes product of all positive integers less than n that are relatively prime to n;
  that is for all i < n: GCD(i, n) = 1."
  [n]
  (let [relative-prime? (fn [i]
                          (= 1 (c2/gcd i n)))]
    (filtered-accumulate * 1 identity 1 inc (dec n)
                         relative-prime?)))
;; 3 * 7 * 9
;; apart from obvious even numbers, the 5 also divides 10
(relative-primes-product 10)
;; => 189


;;; 1.3.2 Lambdas & let

;; let is just a syntactic sugar for lambda parameters (in Scheme)
;; consider this function
(defn f [x y]
  (+ (* x
        (+ 1 (c/square (* x y))))
     (* y (- 1 y))
     (* (+ 1 (* x y))
        (- 1 y))))
(f 2 3)
;; => 78

;; we'd like to simplify and reuse by using:
;; a = 1 + xy
;; b = 1 - y
;; => f(x, y) = xa^2 + yb + ab
(defn f-lambda [x y]
  ((fn [a b]
     (+ (* x (c/square a))
        (* y b)
        (* a b)))
   (+ 1 (* x y))
   (- 1 y)))
(f-lambda 2 3)
;; => 78

;; with let it's easier
(defn f-let [x y]
  (let [a (+ 1 (* x y))
        b (- 1 y)]
    (+ (* x (c/square a))
       (* y b)
       (* a b))))
(f-let 2 3)
;; => 78

;; BUT notice that in Clojure let doesn't behave as in Scheme
;; in Scheme this would return 12!
(let [x 5]
  (let [x 3
        y (+ x 2)]
    (* x y)))
;; => 15
;; (let ((x 2))
;;   (let ((x 3)
;;         (y (+ x 2)))
;;     (* x y)))
;; => ;Value: 12


;;; Ex. 1.34 (p. 66)
(defn f [g]
  (g 2))
(f c/square)
;; => 4
;;what happens now?
#_(f f)
;;=>    java.lang.Long cannot be cast to clojure.lang.IFn


;;; Half-interval method (p. 67)

(defn half-interval-search
  "Half-interval method starts with given interval (a,b) such that
  f(a) < 0 < f(b)
  and continues halving the interval using x = avg(a, b)
  until it finds an x where f(x) = 0 (or is close enough)."
  [f neg-point pos-point]
  (let [close-enough? (fn [x y] (< (Math/abs (- x y)) 0.001))
        avg (c/avg neg-point pos-point)
        avg-val (f avg)]
    (if (close-enough? neg-point pos-point)
      avg
      (cond
        (neg? avg-val) (half-interval-search f avg pos-point)
        (pos? avg-val) (half-interval-search f neg-point avg)
        :else avg))))

;; but search is awkward to use directly because it may be hard to see for which value the function
;; returns negative value and for which the positive one:
(defn half-interval-method [f a b]
  (let [fa (f a)
        fb (f b)]
    (cond
      (and (neg? fa) (pos? fb))
      (half-interval-search f a b)

      (and (neg? fb) (pos? fa))
      (half-interval-search f b a)

      :else
      (throw (ex-info "Valures are not of opposite sign" {:a a :b b})))))

(half-interval-method e/sine 2.0 4.0)
;; => 3.14111328125

(half-interval-method
 (fn [x] (- (* x x x)
            (* 2 x)
            3))
 1.0
 2.0)
;; => 1.89306640625


;;; Fixed points (p.68)
;;; Fixed point of a function is a number x: f(x) = x
;;; For some functions we can find it by taking initial guess and repeatedly applying the function
;;; until the difference is smaller than some predefined tolerance

(def tolerance 0.00001)

(defn close-enough? [v1 v2]
  (< (c/abs (- v1 v2))
     tolerance))

(defn fixed-point [f first-guess]
  (let [next-guess (f first-guess)]
    ;; be careful when debugging infinite recursions
    #_(prn "guess: " next-guess)
    (if (close-enough? first-guess next-guess)
      next-guess
      (recur f next-guess))))

(fixed-point #(Math/cos %) 1.0)
;; => 0.7390822985224024

(fixed-point #(+  (Math/sin %) (Math/cos %)) 1.0)
;; => 1.2587315962971173

;; let's try to use fixed-point to find a square root of a function
;; => DOESN'T CONVERGE! (guesses oscilate between x and 1.0)
(defn sqrt [x]
  (fixed-point (fn [y] (/ x y))
               1.0))
#_(sqrt 4)
;; => infinite loop

;; Let's try a better one by using "average damping" technique
;; Note that we use simple transformation of the original function y = x/y:
;;   y + y = y + x/y => y + y / 2 = (y + x/y) / 2 => y = 1/2 * (y + x/y)
(defn sqrt [x]
  (fixed-point (fn [y] (* 1/2 (+ y (/ x y))))
               1.0))
(sqrt 4)
;; => 2.000000000000002


;;; Exc. 1.35 Show that the golden ratio (section 1.2.2 on p. 38)
;;; is a fixed point of the transformation:  x -> 1 + 1/x
;;; Use this fact to compute golden ratio by means of the fixed point

;; The transformation x -> 1 + 1/x
;; derives directly from the definition x^2 = x + 1 (just divide both sides by x)

;; We can also derive specific formula for phi by solving quadratic equation: http://community.schemewiki.org/?sicp-ex-1.35
;; x^2 - x - 1 = 0
;; x = ( 1 + sqrt(5)) / 2

;; Manually, I can compute a few values
;; 1 + 1/2 = 3/5
;; 1 + 2/3 = 5/3
;; 1 + 3/5 = 8/5
;; 1 + 5/8 = 13/8
;; 1 + 8/13 = 21/13
(map double [5/3 8/5 13/8 21/13])
;; => (1.666666666666667 1.6 1.625 1.615384615384615)

(defn golden-ratio []
  (fixed-point
   (fn [x] (+ 1 (/ 1 x)))
   1))

(double (golden-ratio))
;; => 1.618032786885246


;;; Ex. 1.36 (p.70)
;;; Add debug print statements to fixed-point function
;;; and use it to compute x^x = 1000
;;; Note that x^x is equivalent with transformation x -> ln 1000 / ln x
;;; You also cannot start fixed-point iteration with 1.0 because ln(1.0) = 0

(defn fixed-point-trace [f first-guess]
  (let [next-guess (f first-guess)]
    (if (close-enough? first-guess next-guess tolerance)
      next-guess
      (do
        (println "Next guess: " next-guess)
        (recur f next-guess)))))

#_(fixed-point-trace
   (fn [x] (/ (Math/log 1000)
              (Math/log x)))
   2.0)
;; => 4.555532270803653

(Math/pow 4.555532270803653 4.555532270803653)
;; => 999.9913579312362


;;; Ex. 1.37 (p.71)
;;; Infinite continued fraction.
;;; 1/golden_ratio can be computed as continued fraction expansion
;;; where N = 1,1,1, .... and also D = 1,1,1,1, ....
;;; Define cont-frac procedure  that approximates continued fraction by limiting expansions to number k.

(defn cont-frac-rec
  "Computes finite continued fraction using `n` to produce elements of set N,
  `d` to produce elements of set D, and k as a limit of max number of expansions"
  [n d k]
  (letfn [(frac-rec [i]
            (if (>= i k)
              (/ (n k) (d k))
              (/ (n i)
                 (+ (d i) (frac-rec (inc i))))))]
    (frac-rec 1)))

;; Check what's the value of 1/golden-ratio:
(double (/ 1 (golden-ratio)))
;; => 0.6180344478216819

;; now compute it via `cont-frac` function
(double (cont-frac-rec (constantly 1)
                       (constantly 1)
                       15))
;; => 0.6180344478216819

;; Write also an iterative alternative of my `cont-frac`
;; The iterative version has to work backwards from index k to 1.
;; It first computes the value of (Nk / Dk) and then uses this to compute (N(k-1) / (D(k-1) + previous-result))
;; It continues in this manner until it reaches index one where it returns (N(1) / (D(1) + accumulated-result))
(defn cont-frac-iter [n d k]
  (letfn [(frac-iter [i acc]
            (if (< i 1)
              acc
              ;; WARNING: if you don't use double coercion then the procedure will be slow for large k
              (recur (dec i) (double (/ (n i) (+ (d i) acc))))))]
    (frac-iter k 0)))
(double (cont-frac-iter (constantly 1)
                        (constantly 1)
                        15))
;; => 0.6180344478216819
;; notice that this would lead to StackOverflow when using the recursive version in Clojure
;; WARNING: if you don't use double coercion in `cont-frac-iter`'s recur
;; then this will be very slow!!! (Ratio is slow?)

(time (double (cont-frac-iter (constantly 1)
                              (constantly 1)
                              100000)))
;; "Elapsed time: 14.10361 msecs"
;; => 0.6180339887498948

;; Original version without double coercion and k=5000 took more than 3 seconds!!!
;; "Elapsed time: 3642.036658 msecs"
;; => 0.6180339887498948



;;; Ex. 1.38 (p.17)
;;; continued fraction expansion for e-2 (Euler)
;;; Ni is always 1
;;; Di are 1,2,1,1,4,1,1,6,1,1,8,...
;;; => use this to approximate e
(defn e-approximation [k]

  (letfn [(divisible-by-3? [i] (zero? (mod i 3)))]
    (+ 2.0
       (cont-frac-iter
        (constantly 1)
        ;; we can compute d(i) by realizing that every third element is different from one
        (fn di [i] (if (divisible-by-3? (inc i))
                     (+ i (quot i 3))
                     1))
        k))))

;; Value on wikipedia: 2.7182818284590452353602874713527
;; Our value
(e-approximation 1000)
;; => 2.717162485326501
;; I couldn't get much better value even after more iterations


;;; Ex. 1..39 TODO?
;;; => SKIPPED (doesn't seem to provide much value after doing all the other exercises) 




;;; 1.3.4 Procedures as return values (p.72)
;;; fixed point, average damping, Newton's method

;; Going back to our sqrt function we can reimplement it using idea of "average damping"
;; and end up with clearer implementation:
(defn average-damp
  "Returns a function which given `x` returns an average of `x` and `f x`.
  Useful for fixed points to make them converge."
  [f]
  (fn [x] (c/avg x (f x))))

(defn sqrt-damp [x]
  (fixed-point (average-damp (fn [y] (/ x y)))
               1.0))
(sqrt-damp 4)
;; => 2.000000000000002

;; Newton method
;; https://medium.com/@ruhayel/an-intuitive-and-physical-approach-to-newtons-method-86a0bd812ec3
;; https://math.stackexchange.com/questions/350740/why-does-newtons-method-work
;; says that to find a root of function g(x) we can find a fixed point of f(x) = x - g(x)/g'(x)
;; where g'(x) is derivative

(def dx 0.00001)
(defn deriv
  "Returns a function that is a derivation (approximation of)
  of the function g."
  [g]
  (fn [x]
    (/ (- (g (+ x dx)) (g x))
       dx)))

( (deriv c/square) 4)
;; => 8.00000999952033

;; first we need to get transformed version of our original function
;; which we can then feed into fixed-point
(defn newton-transform [g]
  (fn [x] (- x (/ (g x)
                  ((deriv g) x)))))

(defn newtons-method [g guess]
  (fixed-point (newton-transform g)
               guess))

(defn sqrt-newton [x]
  (newtons-method
   ;; function y^2 - x = 0
   (fn [y] (- (c/square y) x))
   1.0))

(sqrt-newton 2)
;; => 1.4142135623822438
(sqrt-newton 81)
;; => 9.0


;; p.75 We can go even further and define more generic fixed point of transformed function
(defn fixed-point-of-transform
  "Given a function g and the transform function to convert g to f such that
  it can be passed to the fixed-point we compute fixed point starting from the guess."
  [g transform guess]
  (fixed-point (transform g) guess))
;; and we can use that to define sqrt in yet another terms
(defn sqrt-transform [x]
  (fixed-point-of-transform (fn [y] (/ x y))
                            average-damp
                            1.0))

(sqrt-transform 4)
;; => 2.000000000000002


;;; Ex. 1.40 (p.77)
;;; cubic procedure to approximate zeros of the cubic x^3 + ax^2 + bx + c.
;;; via Newton's method:
(defn cubic-transform [a b c]
  (fn [x]
    (+ (e/cube x)
       (* a (c/square x))
       (* b x)
       c)))
(defn cubic-root [a b c]
  (newtons-method (cubic-transform a b c) 1))

;; now we can compute a root of x^3 + ax^2 + bx + c; e.g.:
(cubic-root 2 3 1)
;; => -0.4301597090015873
(cubic-root 1 1 1)
;; => -0.9999999999997796


;;; Ex. 1.41 (p.77)
;;; double procedure which applies function twice
(defn double
  "Returns a function g which applies given function f of one argument twice."
  [f]
  (fn [x]
    (f (f x))))
((double inc) 1)
;; => 3
((double c/square) 2)
;; => 16

;; notice that it grows quadratically!
;; applies inc 2^2^2 = 16 times
(((double (double double)) inc) 5)
(((double (double double)) inc) 5)

;; compare two following!
((double (double (double inc))) 5)
;; => 13

;; so it's tricky!
(((double (double double)) inc) 5)
;; actually exapands to:
((double (double (double (double inc)))) 5)
;; => 21


;;; Ex. 1.42 (p.77)
;;; Implement function composition for two functions of one argument
;;; Note: similar to `double` but with two different functions
(defn compose [f g]
  (fn [x]
    (f (g x))))

((compose c/square inc) 6)
;; => 49


;;; Ex. 1.43 (p.77)
;;; Write procedure that repeatedly applies given function n times

;; let's try it using manual approach
(defn repeated [f n]
  (fn [x]
    (loop [result (f x)
           i (dec n)]
      (if (pos? i)
        (recur (f result) (dec i))
        result))))
((repeated c/square 2) 5)
;; => 625


;; now try it using `compose`???
;; => Check http://community.schemewiki.org/?sicp-ex-1.43
(defn repeated-with-compose [f n]
  (if (< n 1)
    (fn [x] x)
    (compose f (repeated-with-compose f (dec n)))))

((repeated-with-compose c/square 2) 5)
;; => 625

;; notice that it still works even with large number of iterations!
((repeated-with-compose inc 2) 1000000)

;;; Ex. 1.44 (p.78)
;;; Smoothing a function (useful in signal processing)
;;; smoothed function is the one which returns an average of f(x-dx), f(x), f(x+dx)
(def smooth-dx 0.00001)

(defn smooth
  ([f]
   (smooth f smooth-dx))
  ([f dx]
   (fn [x]
     (clojure.core/double (/ (+ (f (- x dx))
                                (f x)
                                (f (+ x dx)))
                             3)))))

((smooth c/square) 5)
;; => 25.000000000066663

;; n-fold smoothed function (repeatedaly applied smoothing)
;; unfortunately, it's not that easy to support both arities in this case
(defn smooth-n
  [f n]
  ((repeated-with-compose smooth n) f ))

;; TODO: why is this slow??
#_((smooth-n c/square 15) 5)
;; => 25.000000001

;;; Exercise 1.45 (p. 78)
;;; Use repeateed average damping to compute n-th roots.
;;; Experiment how many times you have to repeate average damping to make sure that the transformed
;;; function for fixed-point converges.

;; let's start with original sqrt function
(defn sqrt-damp [x]
  (fixed-point (average-damp (fn [y] (/ x y)))
               1.0))

(defn third-root [x]
  (fixed-point (average-damp (fn [y] (/ x (c/square y))))
               1.0))
(third-root 27)
;; => 2.9999972321057697
(third-root 1000)
;; => 10.000002544054729

;; following naive implementation won't converge!
(defn fourth-root [x]
  (fixed-point (average-damp (fn [y] (/ x (* y y y))))
               1.0))
;; inifinite loop 
#_(fourth-root 81)
;; it's oscilating like this:
;; "guess: " 3.0099846201804814
;; "guess: " 2.990114522211396
;; "guess: " 3.0099837398109797
;; "guess: " 2.990115385146659

;; let's try to apply average-damp twice
(defn fourth-root [x]
  (fixed-point ((double average-damp)
                (fn [y] (/ x (* y y y))))
               1.0))

(fourth-root 81)
;; => 3.000000000000033
(fourth-root 10000)
;; => 10.0

;; lets' try nth root
(defn log2 [n]
  (/ (Math/log n) (Math/log 2)))
(defn nth-root [x n]
  (fixed-point ((repeated average-damp (int (log2 n)))
                (fn [y] (/ x (Math/pow y (dec n)))))
               1.0))

(nth-root 81 2)
;; => 9.000006965841198
(nth-root 27 3)
;; => 3.000001464168659
(nth-root 81 4)
;; => 3.000000000000033
(nth-root 243 5)
;; => 3.0000008877496294
(nth-root 729 6)
;; => 2.999996785898161
(nth-root 2181 7)
;; => 2.9988269636913403

(nth-root 100 2)
;; => 10.000023399641949
;; => 10.000023399641949

(nth-root 16 4)
;; => 1.9999751575244828
(nth-root 32 5)
;; => 2.0000200551235574
(nth-root 64 6)
;; => 2.000011071925238
(nth-root 128 7)
;; => 2.0000106805408264
;; BUT it doesn't converge with n=8!
;; => we have to use average-damp at least 3 times
(nth-root 256 8)
;; => 2.000008820504543
(nth-root 512 9)
;; => 2.0000074709755573
(nth-root 8192 13)
;; => 2.000001923509733
(nth-root 32768 15)
;; => 2.0000001141598975
;; BUT it again breaks with n=16!
(nth-root 65536 16)
;; => 2.000000000076957

;;=> it seems that the pattern is that it breaks with n=4, n=8, n=16, n=32?
(nth-root 4294967296 32)
;; => 2.000000000000006

;; use this to get value of 2^64
#_(.pow (BigInteger/valueOf 2) 64)
(nth-root 18446744073709551616 64)
;; => 2.0000000000000853
(nth-root 340282366920938463463374607431768211456 128)
;; => 2.0000000000082006

;;; Exercise 1.46 (p. 78)
;;; Implement general procedure `iterative-improve` to start with initial guess,
;;; check if it's good enough and iterate if not
(defn iterative-improve [good-enough-fn improve-fn]
  (fn [guess] 
    (if (good-enough-fn guess)
      guess
      (recur (improve-fn guess)))))

(defn sqrt-it [x]
  ((iterative-improve
    (fn [guess] (c/good-enough? guess x))
    (fn [guess] (c/avg guess (/ x guess))))
   1.0))
(sqrt-it 4)
;; => 2.0000000929222947

(defn fix-it [f first-guess]
  ((iterative-improve
    ;; notice that f is called twice which may be inefficient
    (fn [guess] (close-enough? guess (f guess)))
    f)
   1.0))

(fix-it
 (average-damp (fn [y] (/ 4 y)))
 1.0)
;; => 2.0000000929222947
 

