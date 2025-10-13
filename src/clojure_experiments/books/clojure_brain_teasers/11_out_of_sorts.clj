(ns clojure-experiments.books.clojure-brain-teasers.11-out-of-sorts)

(contains? (sorted-set 1 2 3) :hello)
;; => 1. Unhandled java.lang.ClassCastException
;; class java.lang.Long cannot be cast to class clojure.lang.Keyword


;; with numbers it works fine
(contains? (sorted-set 1 2 3) 4)
;; => false


;;; Comparators - some really interesting discussion!
(defn num-comparator [i1 i2]
  (- i1 i2))
(num-comparator 1 1)
;; => 0
(num-comparator 1 10)
;; => -9
(num-comparator 10 1)
;; => 9


(sorted-set-by num-comparator 2 1 5 3 4)
;; => #{1 2 3 4 5}

;; Clojure also supports boolean comparetors
;; WARNING: boolean comparators require extra invocations to distinguish = and > cases.
(defn num-comparator-bool [i1 i2]
  (< i1 i2))
(sorted-set-by num-comparator-bool 2 1 5 3 4)
;; => #{1 2 3 4 5}

;; converting boolean style comparator to java style comparator
(defn to-java-comparator
  [bool-comparator]
  (fn [i1 i2]
    (if (bool-comparator i1 i2)
      -1
      (if (bool-comparator i2 i1)
        1
        0))))
(sorted-set-by (to-java-comparator num-comparator-bool) 2 1 5 3 4)
;; => #{1 2 3 4 5}

;; The default comparator is `compare` function.
;; This function ONLY supports comparison of values of the SAME TYPE.
(compare 1 2)
;; => -1
(compare 1 :b)
;; => 
;; 1. Unhandled java.lang.ClassCastException
;;    class clojure.lang.Keyword cannot be cast to class java.lang.Number


;; Possible solution: create custom compare function which uses string representation
(defn total-compare [x y]
  (compare (str x) (str y)))
(sorted-set-by total-compare :hello 2 "x" 1 10 false)
;; => #{1 10 2 :hello false "x"}
(contains? (sorted-set-by total-compare 1 2 3)
           :hello)
;; => false
