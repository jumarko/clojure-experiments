(ns clojure-experiments.purely-functional.property-based-testing.mergesort
  "This is implementation for tests done in lessons 11 and later."
  )

;;; Implementation to test
(defn merge* [l1 l2]
  (lazy-seq
   (cond
     (empty? l1) l2
     (empty? l2) l1

     (< (first l1) (first l2))
     (cons (first l1) (merge* (rest l1) l2))

     :else
     (cons (first l2) (merge* l1 (rest l2))))))

(defn mergesort* [v]
  (case (count v)
    0 ()
    1 (seq v)
    (let [half (quot (count v) 2)]
      (merge*
       (mergesort* (subvec v 0 half))
       (mergesort* (subvec v half))))))

(defn mergesort [ls]
  (seq (mergesort* (vec ls))))

;; => doesn't work with empty list/vector
;; => fix the implementation (remove `seq` call in `mergesort`)
(defn mergesort [ls]
  (mergesort* (vec ls)))

(mergesort (reverse (range 10)))
;; => (0 1 2 3 4 5 6 7 8 9)


