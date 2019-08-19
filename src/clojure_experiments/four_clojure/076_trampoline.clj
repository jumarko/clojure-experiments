(ns four-clojure.076-trampoline
  "http://www.4clojure.com/problem/76.")

(letfn
 [(foo [x y] #(bar (conj x y) y))
  (bar [x y] (if (> (last x) 10)
               x
               #(foo x (+ 2 y))))]
  (trampoline foo [] 1))
;; => [1 3 5 7 9 11]


