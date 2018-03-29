(ns clojure-repl-experiments.puzzles)

;;; What are the problems that beginners face often?
;;; 1. Laziness
;;; 2. 
(def x (map println (range 100)))
;; what does this print?
(take 10 x)
;; what does this print?
(take 10 x)
