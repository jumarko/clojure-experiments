(ns four-clojure.interleave-two-seqs)

;;; Interleave Two Seqs: http://www.4clojure.com/problem/39
;;;
;;; Write a function which takes two sequences and returns the first item from each,
;;; then the second item from each, then the third, etc.
;;;
;;; Special Restrictions: interleave

(defn my-interleave [s1 s2]
  (mapcat #(vector %1 %2) s1 s2))

(= (my-interleave [1 2 3] [:a :b :c]) '(1 :a 2 :b 3 :c))

(= (my-interleave [1 2] [3 4 5 6]) '(1 3 2 4))

(= (my-interleave [1 2 3 4] [5]) [1 5])

(= (my-interleave [30 20] [25 15]) [30 25 20 15])


;; Other solutions:
(mapcat list [1 2 3] [:a :b :c])

(flatten (map list [1 2 3] [:a :b :c]))
