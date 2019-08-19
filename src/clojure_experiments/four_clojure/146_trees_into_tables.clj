(ns four-clojure.146-trees-into-tables
  "http://www.4clojure.com/problem/146.
  for is great to traversing in a nested fashion")

(defn my-flatten [map-of-maps]
  (into {} 
        (for [[parent-key m] map-of-maps
              [child-key v] m]
          [[parent-key child-key] v])))
;; this is the version submitted
#_#(into {} 
         (for [[parent-key m] %
               [child-key v] m]
           [[parent-key child-key] v]))




(= (my-flatten
    '{a {p 1, q 2}
      b {m 3, n 4}})
   '{[a p] 1, [a q] 2
     [b m] 3, [b n] 4})


(= (my-flatten
    '{[1] {a b c d}
      [2] {q r s t u v w x}})
   '{[[1] a] b, [[1] c] d,
     [[2] q] r, [[2] s] t,
     [[2] u] v, [[2] w] x})


(= (my-flatten
    '{m {1 [a b c] 3 nil}})
   '{[m 1] [a b c], [m 3] nil})

