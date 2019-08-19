(ns four-clojure.duplicate-sequence)

;;; http://www.4clojure.com/problem/32
;;; Write a functionn which duplicates each element of a sequence

(defn duplicate [s]
  (mapcat (fn [x] [x x]) s))


(= (duplicate [1 2 3]) '(1 1 2 2 3 3))
(= (duplicate [:a :a :b :b]) '(:a :a :a :a :b :b :b :b))
(= (duplicate [[1 2] [3 4]]) '([1 2] [1 2] [3 4] [3 4]))
