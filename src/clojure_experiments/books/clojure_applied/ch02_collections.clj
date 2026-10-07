(ns clojure-experiments.books.clojure-applied.ch02-collections
  "Chapter 2 is called 'Collection and Organize Your Data'.
  It's about choosing, updating and accessing collections.
  It also shows how to build a custom collection when it's justified.")


;;;; I. Choosing the right collection
(def my-list (cons 0 '(1 2 3)))
;; => (0 1 2 3)
(peek '(1 2 3))
;; => 1

;; Gotcha: these throw!!
(comment
  (pop my-list)
  (peek my-list)
  ;; => 1. Unhandled java.lang.ClassCastException
  ;; class clojure.lang.Cons cannot be cast to class clojure.lang.IPersistentStack
  :-)

(def my-list (conj '(1 2 3) 0))
(pop my-list)
;; => (1 2 3)
(peek my-list);; => 0
;; pop will throw when applied to an empty list
#_(pop (pop (pop (pop (pop my-list)))))
;; 1. Unhandled java.lang.IllegalStateException
;; Can't pop empty list

;;; Compare collections
(defn- compare-author
  [s1 s2]
  (let [p1 ((juxt :lname :fname) s1)
        p2 ((juxt :lname :fname) s2)]
    (doto (compare p1 p2)
      println)))

(compare-author {:fname "Juraj" :lname "Martinka"}
                {:fname "Emanuel" :lname "Martinka"})

;; Using juxt directly doesn't work)
#_(sorted-set-by (juxt :lname :fname)
               {:fname "Juraj" :lname "Martinka"}
               {:fname "Emanuel" :lname "Martinka"})
;; => 1. Unhandled java.lang.ClassCastException
;;     class clojure.lang.PersistentVector cannot be cast to class java.lang.Number


(sorted-set-by compare-author
               {:fname "Juraj" :lname "Martinka"}
               {:fname "Emanuel" :lname "Martinka"})
;; => #{{:fname "Emanuel", :lname "Martinka"} {:fname "Juraj", :lname "Martinka"}}

;; But simple juxt works with `sort-by` because it uses `compare` under the hood,
;; if a custom comparator isn't specified
(sort-by
 (juxt :lname :fname)
 [{:fname "Juraj" :lname "Martinka"}
  {:fname "Emanuel" :lname "Martinka"}])
;; => ({:fname "Emanuel", :lname "Martinka"} {:fname "Juraj", :lname "Martinka"})

;; This is crazy! (from Essential Reference book)
(sort [3 2 Double/NaN 0])
;; => (0 2 3 ##NaN)
(sort [2 3 Double/NaN 0])
;; => (2 3 ##NaN 0)

