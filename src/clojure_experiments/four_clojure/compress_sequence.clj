(ns four-clojure.compress-sequence)

;;; http://www.4clojure.com/problem/30
;;; Write a function which removes consecutive duplicates from sequence

;; using reduce
(defn remove-duplicates [s]
  (reduce (fn [coll x] (if (= (last coll) x)
                        coll
                        (conj coll x)))
          []
          s))

;; easier solution using partition-by
(defn remove-duplicates [s]
  (map first ( partition-by identity s)))

(= (apply str (remove-duplicates  "Leeeeerrroyyy")) "Leroy")

(= (remove-duplicates [1 1 2 3 3 2 2 3]) '(1 2 3 2 3))

(= (remove-duplicates [[1 2] [1 2] [3 4] [1 2]]) '([1 2] [3 4] [1 2]))
