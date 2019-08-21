(ns clojure-experiments.books.sicp.ch1-abstractions-procedures.s2-procedures-and-processes
  (:require
   [clojure-experiments.books.sicp.core :refer [get-stack get-stack-depth]]))

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
;; Notice it takes a while to compute fibonacci even for relatively small n
#_(time (fibr 35))
;; => "Elapsed time: 2837.268809 msecs"

;; Compare this to iterative approach
(defn fibi [n]
  (letfn [(fib-iter [a b counter]
            (if (zero? counter)
              b
              ;; for larger n-s you'd have to use `+'` here to avoid integer overflow
              (fib-iter (+' a b) a (dec counter))))]
    (fib-iter 1 0 n)))

(fibi 0)
(fibi 1)
(fibi 7)
;; and it's much faster too:
(time (fibi 35))
;; => "Elapsed time: 0.0512 msecs"
(fibi 100)


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
     (+ (change amount (dec kinds-of-coins))
        (change (- amount (first-denomination

                           kinds-of-coins)))))))

;;; WTF??
(change 10)



;;; 1.2.4 Exponentiation
;;; Fast exponentiation algorithm using succcessive squaring
;;; See also exercise.clj for iterative version `fast-exp-iter`

(defn exp [base n]
  (if (zero? n)
    1
    ;; notice `*'` for arbitrary precision
    (*' base (exp base (dec n)))))
(time (exp 2 10) )
;; be careful to avoid stackoverflow -> use loop-recur in `exp`
;; if you want to support exponents >> 1000
(time (exp 2 1000) )
;; => "Elapsed time: 0.62659 msecs"

(defn square' [x] (*' x x))

(defn fast-exp
  [base n]
  (cond
    (zero? n)
    1

    (even? n)
    (square' (fast-exp base (/ n 2)))

    :else
    (*' base (fast-exp base (dec n)))))

(fast-exp 2 10)
(time (fast-exp 2 1000))
;;=> "Elapsed time: 0.094528 msecs"

;; and we can support much larger exponents without loop-recur too
(time (fast-exp 2 100000))
;;=> "Elapsed time: 1.444502 msecs"



;;; 1.2.5 Greatest Common Divisor (GCD) - Euclid's algorithm
;;; Naive approach is to factor numbers and find common factors
;;; However, Euclid's algorithm is much more efficient

(defn gcd [a b]
  #_(println "stack depth: " (get-stack-depth "gcd"))
  (if (zero? b)
    a
    (gcd b (rem a b))))

(gcd 206 40)
(gcd 2793 1113) ;; 5 recursive calls
;; => 2



;;; 1.2.6 Probabilitistic algos, prime numbers, Fermat's test
;;;

;;; Classic prime number test
;; to check whether number is a prime we need to find the smallest divisor.
;; if that's n than we know it's a prime

(defn find-divisor
  "Finds the first divisor of number n starting from start-divisor."
  [n test-divisor]
  (cond
    ;; "trick" with square to avoid unnecessary computation
    (< n (square' test-divisor))
    n

    (zero? (rem n test-divisor))
    test-divisor

    :else
    (find-divisor n (inc test-divisor))))

(defn smallest-divisor [n]
  (find-divisor n 2))

(defn prime?
  "Performs classic prime test going from 2 up to sqrt(n)
  to determine whether given number has divisor less than n."
  [n]
  (= n (smallest-divisor n)))

(prime? 2)
;; => true
(prime? 4)
;; => false
(prime? 19)
;; => true
(prime? 41)
;; => true
(prime? 81)
;; => false


;;; Fermat's Test
;;; Based on the Fermat's Little Theorem stating that for every a < n (if n is a prime)
;;; holds following: a^n mod n = a mod n

;; we first need expmod procedure to compute module after exponentiation
;; this is my naive implementation...
;; This is in fact the question in the Ex. 1.25 (p. 55)
(defn expmod [a n m]
  (mod (fast-exp a n) m))
;; now we see that my naive implementation is really bad
;; following doesn't finish in any reasonable time (not within 1 minute!)
#_(time (expmod 123456 123456789 123456789))

;; and this is from the book:
(defn expmod [base exp m]
  (cond
    (= exp 0)
    1

    (even? exp)
    (rem (square' (expmod base (/ exp 2) m))
         m)

    :else
    (rem (* base (expmod base (dec exp) m))
         m)))
;; but the one from the book is really fast:
#_(time (expmod 123456 123456789 123456789))
;; "Elapsed time: 0.204019 msecs"

(expmod 4 7 7)
;; => 4

;; my try:
(defn fermat-prime? [n]
  (loop [i 0]
    ;; choose random number in <1, n-1> interval
    (let [a (inc (rand-int (dec n)))]
      (cond
        (not (= (mod a n) (expmod a n n)))
        false

        (> i 100)
        true

        :else
        (recur (inc i))))))

(fermat-prime? 2)
;; => true
(fermat-prime? 3)
;; => true
(fermat-prime? 4)
;; => false
(fermat-prime? 15)
;; => false
(fermat-prime? 19)
;; => true
(fermat-prime? 81)
;; => false

;; from the book
(defn fermat-test [n]
  (letfn [(try-it [a] (= a (expmod a n n)))]
    (try-it (inc (rand-int (dec n))))))

(defn fast-prime? [n times]
  (cond
    (zero? times) true

    (fermat-test n) (fast-prime? n (dec times))

    :else false))

(fast-prime? 2 10) 
;; => true
(fast-prime? 3 10)
;; => true
(fast-prime? 4 10)
;; => false
(fast-prime? 15 10)
;; => false
(fast-prime? 19 10)
;; => true
(fast-prime? 81 10)
;; => false

;;; Ex. 1.21 (p. 53)
;;; Use `smallest-divisor` to find the smalelst divisor of each of the following numbers:
(smallest-divisor 199)
;; => 199
(smallest-divisor 1999)
;; => 1999
(smallest-divisor 19999)
;; => 7


;;; Ex. 1.22 (p. 54)
;;; Use `timed-prime-procedure` (provided) to write `search-for-primes` procedure
;;; to find out three smallest primers larger than 1000; 10,000; 100,000; 1000,000
;;; Check the running times and compare with expected slow down which is sqrt(10),
;;; because classic testing algorithm has running time sqrt(n)

(defn report-prime [running-time]
  (println " *** ")
  (println running-time "ns"))

(defn start-prime-test [n start-time]
  (when (prime? n)
    (report-prime (- (System/nanoTime) start-time))
    ;; modification of the original implementation from the book
    ;; => returning n to be able to check if it's a prime
    n))

(defn timed-prime-test [n]
  (newline)
  (println n)
  (start-prime-test n (System/nanoTime)))

#_(timed-prime-test 1000)

(defn search-for-primes
  "Searches for primes at the specified interval;
  start inclusive, end exclusive.
  Returns an infinite lazy sequence (chunking!) which you can consume as needed."
  [start end]
  (->> (range start end)
       (map timed-prime-test)
       ;; return only primes
       (filter boolean)))

;; Note: make sure to execute it couple of times (10,000+) first to warm-up JIT

#_(take 3 (search-for-primes 1000 1020))
;; => (1009 1013 1019)
;; 12106 ns; 10916 ns; 10004 ns


#_(take 3 (search-for-primes 10000 10040))
;; => (10007 10009 10037)
;; 28208 ns; 36036 ns; 27110 ns 

#_(take 3 (search-for-primes 100000 100050))
;; => (100003 100019 100043)
;; 59366 ns; 54495 ns; 53917 ns

#_(take 3 (search-for-primes 1000000 1000050))
;; => (1000003 1000033 1000037)
;;  315656 ns; 154213 ns; 137821  ns

;; just for fun:
#_(take 3 (search-for-primes 10000000 10000104))
;; => (10000019 10000079 10000103)
;; ~800,000 ns

;; Summary
;; There's 2-3 increase in running time when going from one level to the next one
;; This roughly corresponds to the sqrt(10) ~= 3.16


;;; Ex. 1.23 (p. 54)
;;; `smallest-divisor` does lots of needles testing:
;;;   it's not necessary to check every even number once you checked the 2.
;;; => Define a procedure `next` which will return the next divisor (3 if the current is 2 and +2 otherwise)
;;; Then time this modified version of `smallest-divisor` using the same samples as in 1.22
;;; Is it two times faster? If not why?

;; we need to redefine `find-divisor` since that's what the `smallest-divisor` is using
(defn next-test-divisor [test-divisor]
  (if (= 2 test-divisor)
    3
    (+ 2 test-divisor)))

(defn find-divisor
  "Finds the first divisor of the number n starting from start-divisor."
  [n test-divisor]
  (cond
    (< n (square' test-divisor))
    n

    (zero? (rem n test-divisor))
    test-divisor

    :else
    (find-divisor n (next-test-divisor test-divisor))))

#_(take 3 #_(search-for-primes 1000 1020))
;; => (1009 1013 1019)
;; Very inconsistent results, but now I'm more often getting times between 5,000 and 8,000 ns
;; Original results:
;;  12106 ns; 10916 ns; 10004 ns


#_(take 3 (search-for-primes 10000 10040))
;; => (10007 10009 10037)
;; Very inconsistent results, but now I'm more often getting times between 12,000 and 20,000 ns
;; Original results:
;;   28208 ns; 36036 ns; 27110 ns 


#_(take 3 (search-for-primes 100000 100050))
;; => (100003 100019 100043)
;; Very inconsistent results, but now I'm more often getting times between 35,000 and 40,000 ns
;; Original results:
;;   59366 ns; 54495 ns; 53917 ns

#_(take 3 (search-for-primes 1000000 1000050))
;; => (1000003 1000033 1000037)
;; Very inconsistent results, but now I'm more often getting times around 100,000 ns
;; Original results:
;;  315656 ns; 154213 ns; 137821  ns

;;=> In general, I'm seeing some improvement and that's around 0.5 - 0.7 of original time
;;   To get more precise results I should have run it with something like criterium to see
;;    the real numbers (see below)
;;  The less than 2x improvement may be due the slower iteration (next-test-divisor vs inc call)
;;  and also some memory caching effects.

;; See http://community.schemewiki.org/?sicp-ex-1.23 :
;;   The observed ratio of the speed of the two algorithms is not 2, but roughly 1.5 (or 3:2) ***
;;   This is mainly due to the NEXT procedure's IF test. The input did halve indeed, but we need to do an extra IF test ***

(comment
  (require '[criterium.core :as c])
  (c/quick-bench (take 3 (search-for-primes 1000000 1000050)))
  ;; The improved implementation:
  ;; Evaluation count : 7406334 in 6 samples of 1234389 calls.
  ;; Execution time mean : 80.346378 ns ***
  ;; Execution time std-deviation : 12.241609 ns
  ;; Execution time lower quantile : 70.109303 ns ( 2.5%)
  ;; Execution time upper quantile : 99.796739 ns (97.5%) ***
  ;; Overhead used : 10.241829 ns

  ;; The old implementation:
  ;; Evaluation count : 4363632 in 6 samples of 727272 calls.
  ;; Execution time mean : 103.447246 ns ***
  ;; Execution time std-deviation : 33.591116 ns
  ;; Execution time lower quantile : 73.880126 ns ( 2.5%)
  ;; Execution time upper quantile : 151.937924 ns (97.5%)
  ;; Overhead used : 10.241829 ns

  )


;;; Ex. 1.24 (p. 55)
;;; Modify the `timed-prime-test` procedure to use `fast-prime?` instead
;;; and test each of the 12 primes you've found in the ex. 1.22.
;;; Since it has O(log n) growth, how would you expect the running time to increase
;;; when going from testing primes near 1,000 to testing primes near 1,000,000?
;;; => Theoretically we should expect running time to be only doubled: log 1000 = 3; log 1000000 = 6

(defn start-prime-test [n start-time]
  (when (fast-prime? n 10)
    (report-prime (- (System/nanoTime) start-time))
    ;; modification of the original implementation from the book
    ;; => returning n to be able to check if it's a prime
    n))

#_(timed-prime-test 1009)
;; 74170 ns
#_(timed-prime-test 1013)
;; 75194 ns
#_(timed-prime-test 1019)
;; 67691 ns

#_(timed-prime-test 10007)
;; 143297 ns
#_(timed-prime-test 10009)
;; 79403 ns
#_(timed-prime-test 10037)
;; 117173 ns

#_(timed-prime-test 100003)
;; 104602 ns
#_(timed-prime-test 100019)
;; 85833 ns
#_(timed-prime-test 100043)
;; 141564 ns

#_(timed-prime-test 1000003)
;; 92083 ns
#_(timed-prime-test 1000033)
;; 157861 ns
#_(timed-prime-test 1000037)
;; 110192 ns

;; => although results vary a lot we still see almost the expected improvement
;;    certainly not more than 2x increase
;; Note:  performing primitive operations on sufficiently large numbers is not constant time, but grows with the size of the number.


;;; Ex. 1.25 (p. 55) is naive `expmod` the same as the implementation from the book?
;;; => The remainder operation inside the original expmod implementation, keeps the numbers
;;;    being squared less than the number tested for primality m. fast-expt however squares huge numbers of a^m size.
(defn expmod [base exp m]
  (rem (fast-exp base exp) m))
;; now we see that my naive implementation is really bad
;; following doesn't finish in any reasonable time (not within 1 minute!)
#_(time (expmod 123456 123456789 123456789))

(defn expmod [base exp m]
  (cond
    (= exp 0)
    1

    (even? exp)
    (rem (square' (expmod base (/ exp 2) m))
         m)

    :else
    (rem (* base (expmod base (dec exp) m))
         m)))
;; but the one from the book is really fast:
#_(time (expmod 123456 123456789 123456789))
;; "Elapsed time: 0.204019 msecs"


;;; Ex. 1.26 (p. 55)
;;; Explain why writing expmod procedure like this make it O(n) instead of (O log n)
(defn expmod [base exp m]
  (cond
    (= exp 0)
    1

    (even? exp)
    (rem (* (expmod base (/ exp 2) m)
            (expmod base (/ exp 2) m))
         m)

    :else
    (rem (* base (expmod base (dec exp) m))
         m)))
;; suddenly, `fast-prime?` is slower than `prime?`
#_(time (prime? 123456789))
;; "Elapsed time: 0.508992 msecs"
#_(time (fast-prime? 123456789 10))
;; ... didn't finish in reasonable time ...

;; => This slow-down is because we turned ordinary recursion into a tree recursion
;;    which means that we halve number of operations via `(/ exp 2)` but immediatelly
;;    double number of operations by repeating the same computation twice

;; now rewrite it again and see the improvements
(defn expmod [base exp m]
  (cond
    (= exp 0)
    1

    (even? exp)
    (rem (square' (expmod base (/ exp 2) m))
         m)

    :else
    (rem (* base (expmod base (dec exp) m))
         m)))

#_(time (fast-prime? 123456789 10))
;; "Elapsed time: 0.074926 msecs"
 

;; Ex. 1.27 (p. 55) Carmichael numbers
(def c-numbers [561 1105 1729 2465 2821 6601])

(defn congruent? [a n]
  (= (expmod a n n)
     a))

(defn carmichael-test [n]
  (every? #(congruent? % n) (range n)))

(mapv carmichael-test [63 561 2709])
;; => [false true false]
(mapv carmichael-test c-numbers)
;; => [true true true true true true]



;; Ex. 1.28 (p. 56) Miller-Rabin test
;; Modify original expmod to check for non-trivial square root
;; and return early
(defn- non-trivial-sqrt? [x n modulus]
  (and (= 1 modulus)
       (not= 1 x)
       (not= (dec n) x)))

(defn mr-expmod
  "Like `expmode` but in every 'squaring' step checks whether we've found
  a 'non-trivial square root of 1 mod n` and returns 0 in that case to indicate
  that it can't be a prime number then.
  Useful for `miller-rabin-test`."
  [base exp m]
  (cond
    (= exp 0)
    1

    (even? exp)
    (let [res (mr-expmod base (/ exp 2) m)
          sq (square' res)
          modulus (rem sq m)]
      (if (non-trivial-sqrt? res m modulus)
        ;; non-trivial square root!
        0
        modulus))

    :else
    (rem (* base (mr-expmod base (dec exp) m))
         m)))

(defn miller-rabin-test [n]
  ;; Originally, didn't change params passed to expmod just modified the expmod itself
  ;; BUT it seemed to work => is 
  ;; `(= 1 (mr-expmod a (dec n) n)))`
  ;; AND `(= 0 (mr-expmod a n n)))`
  ;; EQUIVALENT??
  (letfn [(try-it [a] (= 1 (mr-expmod a (dec n) n)))]
    (try-it (inc (rand-int (dec n))))))


(defn miller-rabin-prime? [n times]
  (cond
    (zero? times) true

    (miller-rabin-test n) (miller-rabin-prime? n (dec times))

    :else false))

(mapv #(fast-prime? % 100) c-numbers)
;; => [true true true true true true]
(mapv #(miller-rabin-prime? % 100) c-numbers)
;; => [false false false false false false]
(miller-rabin-prime? 2 100) 
;; => true
(miller-rabin-prime? 3 100) 
;; => true
(miller-rabin-prime? 9 100) 
;; => false
(miller-rabin-prime? 19 100) 
;; => true
(miller-rabin-prime? 81 100) 
;; => false
(miller-rabin-prime? 1019 100) 
;; => true




