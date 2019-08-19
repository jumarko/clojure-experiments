(ns four-clojure.fibonacci-sequence)

;;; http://www.4clojure.com/problem/26
;;; Write a function that returns first X numbers of Fibonacci sequence.

(defn fibonacci
  "Returns first n numbers from Fibonacci sequence"
  [n]
  (let [all-fibonacci-sequences
        (iterate
         (fn [previous-fibonacci]
           (if (= 1 (count previous-fibonacci))
             [1 1]
             (let [[last last-but-one] (rseq previous-fibonacci)]
               (conj previous-fibonacci (+ last last-but-one))))
           )
         [1])]
    (nth all-fibonacci-sequences (dec n))))

;; simpler solution
(defn fibonacci [n]
  (take n (map first (iterate (fn [[i1 i2]] [i2 (+ i1 i2)]) [1 1]))))

(= (fibonacci 3) '(1 1 2))

(= (fibonacci 6) '(1 1 2 3 5 8))

(= (fibonacci 8) '(1 1 2 3 5 8 13 21))
