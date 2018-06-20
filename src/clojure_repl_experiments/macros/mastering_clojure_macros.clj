(ns clojure-repl-experiments.macros.mastering-clojure-macros
  "Examples from the book Mastering Clojure Macros.")

;;;; Chapter 1 - Build a Solid Foundation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(read-string "(+ 1 2 3 4 5)")
(class (read-string "(+ 1 2 3 4 5)"))
(eval (read-string "(+ 1 2 3 4 5)"))
(class (eval (read-string "(+ 1 2 3 4 5)")))
