(ns four-clojure.simple-recursion)

;;; https://www.4clojure.com/problem/57

(= '(5 4 3 2 1)
 ((fn foo [x] (when (> x 0)
               (conj (foo (dec x)) x)))
  5))
