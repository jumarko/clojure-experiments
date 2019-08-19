(ns four-clojure.rotate-a-sequence)

;;; http://www.4clojure.com/problem/44
;;; Write a function which can rotate a sequence in either direction
;;;
;;; Besides the drop and take I could also use split-at function

(defn rotate-seq [index coll]
  (let [normalized-index (mod index (count coll))]
    (concat
     (drop normalized-index coll)
     (take normalized-index coll))))


(= (rotate-seq 2 [1 2 3 4 5])
   '(3 4 5 1 2))

(= (rotate-seq -2 [1 2 3 4 5]) '(4 5 1 2 3))

(= (rotate-seq 6 [1 2 3 4 5]) '(2 3 4 5 1))

(= (rotate-seq 1 '(:a :b :c)) '(:b :c :a))

(= (rotate-seq -4 '(:a :b :c)) '(:c :a :b))
