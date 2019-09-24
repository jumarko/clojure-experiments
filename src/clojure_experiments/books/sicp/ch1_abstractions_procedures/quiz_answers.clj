(ns clojure-experiments.books.sicp.ch1-abstractions-procedures.quiz
  "For quick experiments and active testing yourself (reimplementation of core fns)."
  (:require [clojure.test :refer [deftest is are testing]]))


;;; iterative improvement (sqrt - p. 22, 25, 30)
;;; how about helper fn?
;;; the idea is to start with initial guess and improve until satisfied
(defn- abs [x]
  (if (> x 0) x (- x)))
(defn- square [x]
  (*' x x))
(def threshold 0.001)
(defn- close-enough? [n guess]
  (< (abs (- n (square guess)))
     threshold))
(defn- avg [x y]
  (/ (+ x y) 2))
(defn- sqrt* [n guess]
  (if (close-enough? n guess)
    (double guess)
    ;; the important idea is that we can improve initial guess by transformation avg (guess + guess/x)
    (let [improved-guess (avg guess (/ n guess))]
      (recur n improved-guess))))
(defn sqrt [n]
  (when (neg? n)
    (throw (IllegalArgumentException.  (str "Cannot be negative: " n))))
  (sqrt* n 1))

(sqrt 4)
(defn- round-to-single-decimal-place [d]
  (Double/parseDouble (format "%.1f" d)))
(deftest sqrt-test
  (testing "works for non-negative numbers"
    (are [x y] (= x (round-to-single-decimal-place (sqrt y)))
      0.0 0
      1.0 1
      2.0 4
      3.0 9
      9.0 81
      100.0 10000))
  (testing "blows for negative numbers"
    (is (thrown? IllegalArgumentException (sqrt -1)))))


;;; what is Iterative process?
;;; A: Iterative process is the one whose state can be completely captured by input parameters.
;;; That is the computation can be effectively "saved" just by storing the args values
;;; It's usually more efficient because it doesn't consume stack
;;; In Clojure it can be optimized via loop-recur


;; iterative fibonacci must somehow capture the notion of
;; "next element being computed from two previous elements"
;; => we will need to extra arguments, not just one!

;; We use loop-recur to optimize recursion
(defn fibonacci [n]
  ;;
  (loop [b 0
         a 1
         n n]
    (if (zero? n)
      b
      (recur a (+' a b) (dec n)))))
  
;; write some tests
(deftest fibonacci-test
  (testing "basic elements"
    (is (= 0 (fibonacci 0)))
    (is (= 1 (fibonacci 1))))
  (testing "a few subsequent elements"
    (is (= 1 (fibonacci 2)))
    (is (= 2 (fibonacci 3)))
    (is (= 3 (fibonacci 4)))
    (is (= 5 (fibonacci 5)))
    (is (= 8 (fibonacci 6))))
  ;; see http://php.bubble.ro/fibonacci/
  (testing "a big number"
    (is (= 354224848179261915075 (time (fibonacci 100)))))) ;; fibonacci 100 takes 0.01 msecs

;; if the `fibonacci` didn't use loop-recur but rather ordinary recursion
;; then the following example would throw StackOverflowError
#_(time (fibonacci 20000))
;; "Elapsed time: 7.655326 msecs"



;;; "invariant quantity" (ex. 1.16)
;;; A: Invariant quantity is an approach how to identify what's "constant" across recursion calls
;;; and so it helps with implementing _iterative processes_.


;; "Invariant quantity"
;;; Ex. 1.16 - iterative variant of `fast-exp` procedure
;;; how can I use this helper fn?

;; this is the slow approach:
(defn fast-exp [x n]
  ;; cheating
  #_(bigint (Math/pow x n))
  (nth (iterate #(*' x %) x)
       (dec n)))
#_(time (fast-exp 2 100000))
;; "Elapsed time: 219.763192 msecs"

;; more expected approach and also much faster
;; invariant quantity is going to be ...
;; ... acc * x^n is unchanged during fn calls
(defn fast-exp-iter [x n]
  (loop [x x
         n n
         acc 1]
    (cond
      (zero? n) acc
      (odd? n) (recur x (dec n) (*' acc x))
      (even? n) (recur (square x) (/ n 2) acc))))
(fast-exp-iter 2 10)
#_(time (fast-exp-iter 2 100000))
;; "Elapsed time: 1.88767 msecs"


;;; GCD: why the algorithm makes sense?
;;; how can you 'invent' it? (pretty hard :o)
(defn gcd [a b]
  (if (zero? b)
    a
    (gcd b (mod a b))))

(deftest gcd-test
  (testing "relative primes"
    (is (= 1
           (gcd 7 36))))
  (testing "the same number"
    (is (= 9 (gcd 9 9))))
  (testing "gcd larger than one"
    (is (= 7
           (gcd 14 21)))))


;;; expmod (p. 51)
;;; x^n mod m
;;; show also the naive version and what's wrong with it..
(defn expmod [x n m])

;; So here's my naive version
(defn expmod-naive [x n m]
  (mod (fast-exp x n)
       m))
;; works fine for small numbers
(expmod-naive 2 10 7)
;; => 2
;; just check the result
(* 146 7)
;; => 1022

;; but not so well for big nums
;; this won't complete in a reasonable time frame
#_(expmod-naive 14115123 107912312 7)

;; => let's try doing a better job...
;; we can use "successive squaring" idea 
(defn expmod
  "Computes x^n mod m.
  x must be a positive integer"
  [x n m]
  {:pre [(pos? x)]}
  (cond
    ;; extra condition added to make sure it works properly with m = 1
    (= m 1) 0

    (= n 0) 1 ; I got this base case wrong (I used (= n 1) (mod x m) instead of the simpler condition)
    (odd? x) (mod (* x (expmod x (dec n) m))
                  m)
    ;; this is the interesting part
    (even? x) (mod
               (square (expmod x (quot n 2) m))
               m)))
  
;; 243 mod 7
(expmod 3 5 7)
;; 34 * 7 = 238
;; => 5

(expmod 4 3 7)
;; => 1

;; Note that using simplified base case from the book doesn't work when the mod is 1,
;; but that is really a trivial case, where the answer is always zero!
(expmod 4 0 1)
;; => 1
(mod 1 1)
;; => 0


;; use expmod to implement `fermat-test`
(defn fermat-prime? [n]
  )

;;; HO fns

;; generalized accumulate (ex. 1.33)
;; consider also implementating Simpson's rule with it.
(defn accmulate [])


;;; General methods - section 1.3.3

;; half-interval method (p. 67)
;; what does it do?
(defn half-interval-method [])

;; fixed-point (p. 69)
;; how is fixed-point useful?
(defn fixed-point [f]
  )


;; what arguments it takes?
;; what exactly it does?
;; how can I use it?
;; how can I derive it?
(defn newton-method [f])

;; average dumping?

;; iterative improvement?
