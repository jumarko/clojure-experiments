(ns clojure-experiments.purely-functional.puzzles.util)


(defn digits [n]
  "Returns all digits of given number"
  (assert (pos? n) "Can only work with positive numbers.")
  (loop [n n
         digits '()]
    (if (> n 9)
      (recur (quot n 10) (conj digits (rem n 10)))
      (conj digits n))))

#_(digits 135)
;; => (1 3 5)
