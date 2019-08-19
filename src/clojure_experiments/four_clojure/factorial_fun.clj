(ns four-clojure.factorial-fun)

;;; http://www.4clojure.com/problem/42
;;; Write a function which calculates factorials.

(defn factorial [n]
  (loop [n n
         acc 1]
    (if (zero? n)
      acc
      (recur (dec n) (* acc n))
      )))

;; much simpler solution!
(defn factorial [n]
  (apply * (range 1 (inc n))))

(= (factorial 1) 1)

(= (factorial 3) 6)

(= (factorial 5) 120)

(= (factorial 8) 40320)
