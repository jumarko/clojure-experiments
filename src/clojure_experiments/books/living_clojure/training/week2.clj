(ns clojure-experiments.books.living-clojure.training.week2
  "Week 2 from the Clojure Training Plan at the end of the book.")

;;;; Day 1
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Day 1: Fibonacci
;;; Write a function that returns Fibonacci sequence

;; 1,1,2,3,5,8,11, ...

(defn fibonacci [n]
  (loop [x n
         result [0 1]]
    (cond
      (zero? x) []
      (= 1 x) [0]
      (= 2 x) result
      :else (recur (dec x)
                   (conj result (+ (get result (max (- n 1)) 0 )
                                   (get result (max (- n 2)) 0)))))))

(fibonacci 3)
;; => [0 1 1]

(fibonacci 5)
;; => [0 1 0 0 0]

;;; Gosh... It's an iteration!
(defn fibonacci [n]
  (take n
        (map first
             (iterate (fn [[a b]]
                        [b (+ a b)])
                      [1 1]))))

(fibonacci 10)
;; => (1 1 2 3 5 8 13 21 34 55)



;;; Let's try to implement via loop-recur again
(defn fibonacci-loop [n]
  (cond
    (zero? n) []
    (= 1 n) [1]
    :else (loop [result [1 1]
                 a 1
                 b 1
                 i 2]
            (if (< i n)
              (let [new-item (+' a b)]
                (recur (conj result new-item)
                       b
                       new-item
                       (inc i)))
              result))))

(fibonacci-loop 0)
;; => []
(fibonacci-loop 1)
;; => [1]
(fibonacci-loop 2)
;; => [1 1]
(fibonacci-loop 3)
;; => [1 1 2]
(fibonacci-loop 5)
;; => [1 1 2 3 5]
(fibonacci-loop 10)
;; => [1 1 2 3 5 8 13 21 34 55]

#_(time (fibonacci-loop 10000))


;;; Day 1: Get the caps
(defn caps [s]
  (apply str (filter Character/isUpperCase s)))
(caps "heLLo, WorLD");; => "LLWLD"


;;; Day 1: Factorial
(defn factorial [n]
  (cond
    (neg? n) (throw (ex-info "Cannot compute a factorial of a negative number"
                             {:n n}))
    (zero? n) 1 ; special case, as per definition
    :else (reduce * (range 1 (inc n)))))

(assert (= 1 (factorial 0)))
(assert (= 1 (factorial 1)))
(assert (= 6 (factorial 3)))
(assert (= 120 (factorial 5)))
(assert (= 40320 (factorial 8)))
(factorial 20)
;; => 2432902008176640000

;; throws "long overflow"
#_(factorial 30)



;;; Day 2: Implement GCD.
;;; - see https://www.geeksforgeeks.org/dsa/euclidean-algorithms-basic-and-extended/
(defn gcd [a b]
  (if (zero? a)
    b
    (gcd (mod b a) a)))
(gcd 35 15)
;; => 5
(gcd 28 7)
;; => 7




;;; Day 3: cartesian product
(for [x [1 2 3]
      y [:a :b :c]]
  [x y])
;; => ([1 :a] [1 :b] [1 :c] [2 :a] [2 :b] [2 :c] [3 :a] [3 :b] [3 :c])


;;; Day 5: Pascal's triangle
(defn pascal [n]
  (if (zero? n)
    ;; the first row is 1 by definition
    [1]
    (let [previous-row (pascal (dec n))
          sums (mapv + previous-row (drop 1 previous-row))]
      (conj (into [1] sums)
            1))))

(pascal 0)
;; => [1]
(pascal 1)
;; => [1 1]
(pascal 2)
;; => [1 2 1]
(take 10 (map pascal (range 10) ))
;; => ([1]
;;     [1 1]
;;     [1 2 1]
;;     [1 3 3 1]
;;     [1 4 6 4 1]
;;     [1 5 10 10 5 1]
;;     [1 6 15 20 15 6 1]
;;     [1 7 21 35 35 21 7 1]
;;     [1 8 28 56 70 56 28 8 1]
;;     [1 9 36 84 126 126 84 36 9 1])
