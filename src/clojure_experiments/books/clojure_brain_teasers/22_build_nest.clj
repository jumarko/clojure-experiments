(ns clojure-experiments.books.clojure-brain-teasers.22-build-nest)

(def ex1
  (for [a [0 1 2]
        b [3 4 5]]
    (* a b)))
ex1
;; => (0 0 0 3 4 5 6 8 10)

(def ex2
  (for [a [0 1 2]]
    (for [b [3 4 5]]
      (* a b))))
ex2
;; => ((0 0 0) (3 4 5) (6 8 10))
