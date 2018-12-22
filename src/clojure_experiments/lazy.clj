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


;;; lazy quick sort - see `clojure-experiments.joy-of-clojure.ch06`
(defn sort-parts [work]
  (lazy-seq
   (loop [[part & parts] work]
     (if-let [[pivot & xs] (seq part)]
       (let [smaller? #(< % pivot)]
         (recur (list*
                 (filter smaller? xs)
                 pivot
                 (remove smaller? xs)
                 parts)))
       (when-let [[x & parts] parts]
         (cons x (sort-parts parts)))))))

(defn lazy-qsort [xs]
  (sort-parts (list xs)))

(lazy-qsort [2 1 4 3])

(defn rand-ints [n]
  (take n (repeatedly #(rand-int n))))

#_(lazy-qsort (rand-ints 20))
