(ns four-clojure.flipping-out)

;;; http://www.4clojure.com/problem/46
;;; Write a higher-order function which flips the order of the arguments of an input function

(defn flip-arguments [f]
  (fn [a b] (f b a)))


(= 3 ((flip-arguments nth) 2 [1 2 3 4 5]))

(= true ((flip-arguments >) 7 8))

(= 4 ((flip-arguments quot) 2 8))

(= [1 2 3] ((flip-arguments take) [1 2 3 4 5] 3))
