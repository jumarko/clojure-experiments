(ns four-clojure.02-recursion)

;;; Recursion is one of the fundamental techniques in functional programming

((fn foo [x] (when (> x 0) (conj (foo (dec x)) x))) 5)



