(ns four-clojure.143-dot-product
  "http://www.4clojure.com/problem/143
  Create a function that computes dot product of two sequences.
  You may assume that vectors will have the same length.")


(defn dot-product [v1 v2]
  (reduce + (map * v1 v2)))

(= 0 (dot-product [0 1 0] [1 0 0]))

(= 3 (dot-product [1 1 1] [1 1 1]))

(= 32 (dot-product [1 2 3] [4 5 6]))

(= 256 (dot-product [2 5 6] [100 10 1]))



