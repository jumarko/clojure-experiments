(ns clojure-experiments.four-clojure.078-trampoline-reimplement
  "Reimplement trampoline.
  http://www.4clojure.com/problem/78.
  Solutions: http://www.4clojure.com/problem/solutions/78")

(defn my-trampoline [f & args]
  (let [fval (apply f args)]
    (loop [g fval]
      (if (fn? g)
        (recur (g))
        g))))

(defn my-trampoline2 [f & args]
  (loop [f f
         args args]
    (let [fval (apply f args)]
      (if (fn? fval)
        (recur fval [])
        fval))))

;; no need for extra let to 'apply' args!
;; (leetwinski's solution)
(defn my-trampoline3 [f & args]
  (loop [fval (apply f args)]
    (if (fn? fval)
      (recur (fval))
      fval)))

;;; From Problem 76:
(letfn
    [(foo [x y] #(bar (conj x y) y))
     (bar [x y] (if (> (last x) 10)
                  x
                  #(foo x (+ 2 y))))]
  (my-trampoline foo [] 1))
;; => [1 3 5 7 9 11]


;;; Original `trampoline` implementation from clojure.core
(defn trampoline 
  ([f]
   (let [ret (f)]
     (if (fn? ret)
       (recur ret)
       ret)))
  ([f & args]
   (trampoline #(apply f args))))

