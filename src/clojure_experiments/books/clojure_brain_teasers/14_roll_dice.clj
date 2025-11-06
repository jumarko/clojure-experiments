(ns clojure-experiments.books.clojure-brain-teasers.14-roll-dice
  "Sets check for duplicates at both reading AND evaluation phases.
  NOTE: in this case, set is not the right data structure - better to use a vector.")

(= #{(rand-int 6) (rand-int 6)}
   #{(rand-int 6) (rand-int 6)})
;; 1. Caused by java.lang.IllegalArgumentException
;; Duplicate key: (rand-int 6)

;; this works
(= (hash-set (rand-int 6) (rand-int 6))
   (hash-set (rand-int 6) (rand-int 6)))
;; => false

;; you can also use conj
(conj #{} (rand-int 6) (rand-int 6))
;; => #{3 2}
