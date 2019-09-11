(ns clojure-experiments.recursion)

;;; When is `recur` in "tail position"??

;; `cond` doesn't work?
;; => Yes it does!
(loop [i 10]
  (println i)
  (cond
    (= i 7) (recur (- i 4))
    (= i 4) (recur (- i 2))
    (= i 3) (recur (inc i))
    (> i 0) (recur (dec i))
    :else :done))
