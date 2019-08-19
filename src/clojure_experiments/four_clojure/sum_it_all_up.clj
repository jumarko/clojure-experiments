(ns four-clojure.sum-it-all-up)

;;; http://www.4clojure.com/problem/24
;;; Write the function which returns the sujm of sequence of numbers.
(= (reduce + [1 2 3])  6)
(= (reduce + (list 0 -2 5 5))  8)
(= (reduce + #{4 2 1})  7)
(= (reduce + '(0 0 -1))  -1)
(= (reduce + '(1 10 3))  14)
