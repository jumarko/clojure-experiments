(ns four-clojure.081-set-intersection
  "http://www.4clojure.com/problem/81
  Write a function which returns the intersection of two sets
  Special restrictions: `clojure.set/intersection`
  Other users' solutions: http://www.4clojure.com/problem/solutions/81")

;; this is the easiest implementation I can think of
;; for more clever one, see implementation of `clojure.set/intersection`
(defn set-intersection [s1 s2]
  (set (filter s2 s1)))


(= (set-intersection #{0 1 2 3} #{2 3 4 5}) #{2 3})

(= (set-intersection #{0 1 2} #{3 4 5}) #{})

(= (set-intersection #{:a :b :c :d} #{:c :e :a :f :d}) #{:a :c :d})
