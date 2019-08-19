(ns four-clojure.intro-to-iterate)

;;; http://www.4clojure.com/problem/45
;;; iterate function can be used to produce an infinite lazy sequence
;; Be careful to use take with infinite sequences!

(= '(1 4 7 10 13) (take 5 (iterate #(+ 3 %) 1)))
