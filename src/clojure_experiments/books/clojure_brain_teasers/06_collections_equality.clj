(ns clojure-experiments.books.clojure-brain-teasers.06-collections-equality)

;; All these sequential collections are equivalent: vector, list, sequence
(= [0 1 2 3 4 5]
   '(0 1 2 3 4 5)
   (range 6))
;; => true


;; Comparing collections from different equality partitions will always compare false
(= [0 1 2 3 4 5] #{0 1 2 3 4 5})
;; => false


;; Also applies to Java collections
(= [0 1 2 3]
   (java.util.ArrayList. '(0 1 2 3)))
;; => true

;; ... but not Arrays -> they are compared by _identity_
(= [0 1 2 3]
   (int-array [0 1 2 3]))
;; => false
(= (int-array [0 1 2 3])
   (int-array [0 1 2 3]))
;; => false!!
(let [a (int-array [0 1 2 3])]
  (= a a))
;; => true
