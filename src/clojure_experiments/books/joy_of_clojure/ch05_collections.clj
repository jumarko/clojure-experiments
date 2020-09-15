(ns clojure-experiments.books.joy-of-clojure.ch05-collections)

;;; Persistence, sequences, and complexity (p. 85)

;; arrays are mutable => no historical versions are preserved

(def d [:willie :barnabas :adam])
(def ds (into-array d))
(seq ds)
;; => (:willie :barnabas :adam)

(aset ds 1 :quentin)
;; => :quentin

(seq ds)
;; => (:willie :quentin :adam)

;; original vector is still unchanged
d
;; => [:willie :barnabas :adam]

;; even if we replace an item
(replace {:barnabas :quentin} d)
;; => [:willie :quentin :adam]
d
;; => [:willie :barnabas :adam]


;;; Sequential, sequence, seq

;; list, vector and java.util.List are sequential
(= '(1 2 3) [1 2 3])
;; => true

(= '(1 2 3) (java.util.ArrayList. [1 2 3]))
;; => true

;; but set is not
(= '(1 2 3) #{1 2 3})
;; => false
