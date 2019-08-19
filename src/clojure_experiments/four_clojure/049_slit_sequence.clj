(ns four-clojure.49-slit-sequence)

;;; http://www.4clojure.com/problem/49
;;; Write a function which will split a sequence into two parts.

;; let's cheat by using `split-at`
(defn split-sequence
  "Splits a sequence into two parts"
  [split-idx coll]
  (split-at split-idx coll))

;; now do the real work
(defn split-sequence
  "Splits a sequence into two parts"
  [split-idx coll]
  [(take split-idx coll) (drop split-idx coll)])


(= (split-sequence 3 [1 2 3 4 5 6])
   [[1 2 3] [4 5 6]])
(= (split-sequence 1 [:a :b :c :d])
   [[:a] [:b :c :d]])
(= (split-sequence 2 [[1 2] [3 4] [5 6]])
   [[[1 2] [3 4]] [[5 6]]])
