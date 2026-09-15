(ns clojure-experiments.reduce
  "Play with `reduce`")


;;; Implement my own `map` with reduce.
(defn my-map [f coll]
  ;; NOTE: reduce docstring is kinda confusing...
  (into (empty coll) ; preserve the type/category of the input collection?
        (reduce
         (fn
           ([] coll)
           ([result item]
            (conj result (f item))))
         []
         (seq coll))))

(my-map #(* % 2) (range 10))
;; => (18 16 14 12 10 8 6 4 2 0)

(my-map #(* % 2) (vec (range 10)))
;; => [0 2 4 6 8 10 12 14 16 18]
