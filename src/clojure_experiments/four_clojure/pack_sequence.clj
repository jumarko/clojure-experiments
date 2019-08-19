(ns four-clojure.pack-sequence)

;;; http://www.4clojure.com/problem/31
;;; Write a function which packs consecutive duplicates into sub-lists.

(defn pack-duplicates [coll]
  (partition-by identity coll))

(= (pack-duplicates [1 1 2 1 1 1 3 3]) '((1 1) (2) (1 1 1) (3 3)))

(= (pack-duplicates [:a :a :b :b :c]) '((:a :a) (:b :b) (:c)))

(= (pack-duplicates [[1 2] [1 2] [3 4]]) '(([1 2] [1 2]) ([3 4])))
