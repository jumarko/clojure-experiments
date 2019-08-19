(ns clojure-experiments.four-clojure.069-merge-with-fn
  "Implement `merge-with` yourself.
  See http://www.4clojure.com/problem/69.
  Solutions: http://www.4clojure.com/problem/solutions/69")

(defn my-merge-with [f & ms]
  (reduce
   (fn [result m]
     (reduce-kv
      (fn [result-m m-k m-v]
        (if-let [[_result-k result-v] (find result-m m-k)]
          (assoc result-m m-k (f result-v m-v))
          (assoc result-m m-k m-v)))
      result
      m))
   {}
   ms))

(my-merge-with * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})
;; => should be: {:a 4, :b 6, :c 20}

(my-merge-with - {1 10, 2 20} {1 3, 2 10, 3 15})
;; => should be: {1 7, 2 10, 3 15}

(my-merge-with  concat {:a [3], :b [6]} {:a [4 5], :c [8 9]} {:b [7]})
;; => should be: {:a (3 4 5), :b (6 7), :c [8 9]}

;; this should throw NPE!
#_(my-merge-with + {:a nil} {:a 2})

