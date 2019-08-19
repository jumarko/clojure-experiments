(ns four-clojure.intro-to-destructuring)

;;; http://www.4clojure.com/problem/52
;;;

(= [2 4] (let [[a b c d e f g] (range)] [c e]))
