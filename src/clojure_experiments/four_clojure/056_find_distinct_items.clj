(ns four-clojure.056-find-distinct-items
  "http://www.4clojure.com/problem/56.
  Remove duplicates from a sequence, preserve order.")


(defn distinct-items
  [xs]
  (first (reduce
          (fn [[distincts visited :as acc] x]
            (if (visited x)
              acc
              [(conj distincts x) (conj visited x)]))
          [[] #{}]
          xs)))

;; of course we can also just use LinkedHashSet
(defn distinct-items
  [xs]
  (vec (java.util.LinkedHashSet. xs)))

(= (distinct-items [1 2 1 3 1 2 4]) [1 2 3 4])

(= (distinct-items [:a :a :b :b :c :c]) [:a :b :c])

(= (distinct-items '([2 4] [1 2] [1 3] [1 3])) '([2 4] [1 2] [1 3]))

(= (distinct-items (range 50)) (range 50))
