(ns clojure-experiments.four-clojure.078-trampoline-reimplement
  "Reimplement trampoline.
  http://www.4clojure.com/problem/78")

(defn my-trampoline [f & args]
  (let [fval (apply f args)]
    (loop [g fval]
      (if (fn? g)
        (recur (g))
        g))))

;;; From Problem 76:
(letfn
    [(foo [x y] #(bar (conj x y) y))
     (bar [x y] (if (> (last x) 10)
                  x
                  #(foo x (+ 2 y))))]
  (my-trampoline foo [] 1))
;; => [1 3 5 7 9 11]

