(ns four-clojure.simple-closures)

;;; http://www.4clojure.com/problem/107
;;;
;;; Lexical scope and first-class functions are two of the most basic building blocks of functional language like Clojure
;;; With Lexical Closures you can get a great control overal lifetime of you local bindings
;;;
;;; Let's build a simple closure:
;;; Given a positive integer n, return a function (f x) which computes x^n.
;;; Observe that the effect of this is to preserve the value of n for use outside the scope in which it is defined.

(defn lexical-closure [n]
  (fn [x] (int ( Math/pow x n))))

(= 256
   ((lexical-closure 2) 16)
   ((lexical-closure 8) 2))

(= [1 8 27 64] (map (lexical-closure 3) [1 2 3 4]))

(= [1 2 4 8 16] (map #((lexical-closure %) 2) [0 1 2 3 4]))
