(ns clojure-experiments.purely-functional.puzzles.util)


;;; Note: it's much simpler to just `str` the number to get its digits!
;;; -> http://www.4clojure.com/problem/solutions/86
(defn digits
  "Returns all digits of given number"
  [n]
  (assert (pos? n) "Can only work with positive numbers.")
  (loop [n n
         digits '()]
    (if (> n 9)
      (recur (quot n 10) (conj digits (rem n 10)))
      (conj digits n))))

(defn digits [n]
  (map #(- (int %) (int \0))
       (str n))
)

#_(digits 135)
;; => (1 3 5)
