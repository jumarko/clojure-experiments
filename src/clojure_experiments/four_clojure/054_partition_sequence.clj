(ns four-clojure.054-partition-sequence
  "Problem 54. Partition a Sequence: http://www.4clojure.com/problem/54
  Write a function which returns a seq of lists of x items each.
  Lists of less than x items should not be returned.")

(defn partition-seq [n xs]
  (loop [partitions []
         current-partition []
         [fst & rst] xs]
    (if fst
      (if (> n (count current-partition))
        (recur partitions (conj current-partition fst) rst)
        (recur (conj partitions current-partition) [fst] rst))
      (if (= n (count current-partition))
        (conj partitions current-partition)
        partitions))))

;; better?
(defn partition-seq [n xs]
  (->> xs
       (group-by #(quot % n))
       vals
       (filter #(= n (count %)))))

(= (partition-seq 3 (range 9)) '((0 1 2) (3 4 5) (6 7 8)))

(= (partition-seq 2 (range 8)) '((0 1) (2 3) (4 5) (6 7)))

(= (partition-seq 3 (range 8)) '((0 1 2) (3 4 5)))
