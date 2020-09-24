(ns clojure-experiments.books.joy-of-clojure.ch06
  (:require [cljol.dig9 :as cljol]))

;;; lazy quick sort (p. 133) - This is a gem!
(defn sort-parts [work]
  (lazy-seq
   (loop [[part & parts] work]
     (if-let [[pivot & xs] (seq part)]
       (let [smaller? #(< % pivot)]
         (recur (list*
                 (filter smaller? xs)
                 pivot
                 (remove smaller? xs)
                 parts)))
       (when-let [[x & parts] parts]
         (cons x (sort-parts parts)))))))

(defn lazy-qsort [xs]
  (sort-parts (list xs)))

(lazy-qsort [2 1 4 3])

(defn rand-ints [n]
  (take n (repeatedly #(rand-int n))))

#_(lazy-qsort (rand-ints 20))

(def numbers (rand-ints 1000000))

#_(time (sort numbers))
;;=> "Elapsed time: 1650.853321 msecs"

#_(time (lazy-qsort numbers))
;;=> "Elapsed time: 0.062903 msecs"
#_(time (doall (take 1000 (lazy-qsort numbers))))
;;=> "Elapsed time: 417.165721 msecs"
#_(time (doall (take 10000 (lazy-qsort numbers))))
;;=> "Elapsed time: 861.250399 msecs"
;; approaching 100,000 numbers we can see we have similar running time to native sort with full 10^6 numbers
#_(time (doall (take 100000 (lazy-qsort numbers))))
;;=> "Elapsed time: 1988.791992 msecs"
;; finally, the whole sequence is significantly slower than than native sort
#_(time (doall (lazy-qsort numbers)))
;; "Elapsed time: 14839.150604 msecs"
 


;;; Structural sharing

;; list
(def baselist (list :barnabas :adam))
(def lst1 (cons :willie baselist))
(def lst2 (cons :phoenix baselist))

;; the next parts of both lists are identical
(= (next lst1) (next lst2))
;; => true
(identical? (next lst1) (next lst2))
;; => true
#_(cljol/view lst1)


;; Let's try to build a simple tree to demonstrate
;; how structural sharing works
{:val 5 :L nil :R nil} ; use this when a single item is added to an empty list

(defn xconj
  "Builds up the tree by adding given value to the tree."
  [t v]
  (cond
    (nil? t) {:val v :L nil :R nil}))

(xconj nil 5)
;; => {:val 5, :L nil, :R nil}

;; that's fine but we want more than just a single item
(defn xconj
  "Builds up the tree by adding given value to the tree."
  [t v]
  (cond
    (nil? t) {:val v :L nil :R nil}
    (< v (:val t)) {:val (:val t)
                    :L (xconj (:L t) v)
                    :R (:R t)}
    :else {:val (:val t)
           :L (:L t)
           :R (xconj (:R t) v)}))

;; we'll need a better way to print the tree
(defn xseq [t]
  (when t
    (concat (xseq (:L t))
            [(:val t)]
            (xseq (:R t)))))

(def tree1 (xconj (xconj (xconj nil 5) 3)
                  2))
(xseq tree1)
;; => (2 3 5)
(xseq (xconj tree1 7))
;; => (2 3 5 7)
