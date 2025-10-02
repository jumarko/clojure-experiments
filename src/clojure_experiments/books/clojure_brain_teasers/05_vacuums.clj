(ns clojure-experiments.books.clojure-brain-teasers.05-vacuums
  "NOTE: Other programming languages, such as JavaScript and Python, also have
  examples of “vacuous truth” logical operations.
  See https://en.wikipedia.org/wiki/Vacuous_truthhttps://en.wikipedia.org/wiki/Vacuous_truth")

;; If there are no examples, there are no false examples and the statement is true
(and)
;; => true

;; this is conceptually the same case as above (`and`)
(every? odd? [])
;; => true !?!

(or)
;; => nil


;; returns last logically true value
(and 1 2)
;;=> 2
;; returns first logically true value
(or nil false 1 2)
;;=> 1
