(ns clojure-experiments.lazy
  "Experiments with lazy sequences and related stuff.")

;;; how to properly construct lazy seq?
(defn lazy-range
  ([] (lazy-range 0))
  ([n]
   (lazy-seq 
    (cons n (lazy-range (inc n))))))

;; another approach - differences?
(defn lazy-range
  ([] (lazy-range 0))
  ([n]
   (cons n (lazy-seq (lazy-range (inc n))))))
#_(lazy-range 100)
