(ns four-clojure.replicate-sequence)

;;; http://www.4clojure.com/problem/33
;;; Write a function which replicates each element of a sequence a variable number of times

(defn replicates [coll replicates-count]
  (apply concat
   (map (fn [x] (repeat replicates-count x))
        coll)))

;; simpler solution using mapcat
(defn replicates [coll replicates-count]
  (mapcat (fn [x] (repeat replicates-count x)) coll))

;; or even
(defn replicates [coll replicates-count]
  (mapcat (partial repeat replicates-count) coll))


(= (replicates [1 2 3] 2) '(1 1 2 2 3 3))

(= (replicates [:a :b] 4) '(:a :a :a :a :b :b :b :b))

(= (replicates [4 5 6] 1) '(4 5 6))

(= (replicates [[1 2] [3 4]] 2) '([1 2] [1 2] [3 4] [3 4]))

(= (replicates [44 33] 2) '(44 44 33 33))
