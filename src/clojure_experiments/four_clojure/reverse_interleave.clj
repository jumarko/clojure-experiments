(ns four-clojure.reverse-interleave)

;;; http://www.4clojure.com/problem/43
;;; Write a function which reverses the interleave process into x number of subsequences

(defn reverse-interleave [xs count]
  (for [x (range count)]
    (take-nth count (drop x xs))))

;;; other solutions
(defn reverse-interleave [xs count]
  (vals (group-by #(rem % count) xs)))

(defn reverse-interleave [xs count]
  (apply map list (partition count xs)))


(= (reverse-interleave [1 2 3 4 5 6] 2) '((1 3 5) (2 4 6)))

(= (reverse-interleave (range 9) 3) '((0 3 6) (1 4 7) (2 5 8)))

(= (reverse-interleave (range 10) 5) '((0 5) (1 6) (2 7) (3 8) (4 9)))
