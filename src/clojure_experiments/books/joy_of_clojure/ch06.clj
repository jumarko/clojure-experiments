(ns clojure-experiments.books.joy-of-clojure.ch06)

;;; lazy quick sort (p. 133) - This is a gem!
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

(def numbers (rand-ints 1000000))

#_(time (sort numbers))
;;=> "Elapsed time: 1650.853321 msecs"

#_(time (lazy-qsort numbers))
;;=> "Elapsed time: 0.062903 msecs"
#_(time (doall (take 1000 (lazy-qsort numbers))))
;;=> "Elapsed time: 417.165721 msecs"
#_(time (doall (take 10000 (lazy-qsort numbers))))
;;=> "Elapsed time: 861.250399 msecs"
;; approaching 100,000 numbers we can see we have similar running time to native sort with full 10^6 numbers
#_(time (doall (take 100000 (lazy-qsort numbers))))
;;=> "Elapsed time: 1988.791992 msecs"
;; finally, the whole sequence is significantly slower than than native sort
#_(time (doall (lazy-qsort numbers)))
;; "Elapsed time: 14839.150604 msecs"
 
