(ns clojure-experiments.equality)

;;; See https://clojure.org/guides/equality
;;; - summary and https://clojure.org/guides/equality#equality_and_hash

(= 1 1.0)
;; => false

(== 1 1.0)
;; => true

(= (float 1.0) 1.0)
;; => true

(= [1 2 3] '(1 2 3) (conj clojure.lang.PersistentQueue/EMPTY 1 2 3))
;; => true

(hash ["a" 5 :c])
;; => 1698166287

(hash (conj clojure.lang.PersistentQueue/EMPTY "a" 5 :c))
;; => 1698166287

;; But hash isn't consistent whe mixing Clojure colls with java counterparts
(= [1 2 3] (java.util.ArrayList. [1 2 3]))
;; => true
(hash [1 2 3])
;; => 736442005
(hash (java.util.ArrayList. [1 2 3]))
;; => 30817

;; ... inconsistent hash means that java vs clojure collections put in a hashmap won't be equal!
(= (hash-map [1 2 3] 5)
   (hash-map [1 2 3] 5))
;; => true
(= (hash-map [1 2 3] 5)
   (hash-map (java.util.ArrayList. [1 2 3]) 5))
;; => false
