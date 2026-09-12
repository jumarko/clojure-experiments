(ns clojure-experiments.books.living-clojure.training.week2
  "Week 2 from the Clojure Training Plan at the end of the book.")


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


;;; Day : Get the caps
(defn caps [s]
  (apply str (filter Character/isUpperCase s)))
(caps "heLLo, WorLD");; => "LLWLD"
