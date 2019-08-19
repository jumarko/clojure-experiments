(ns four-clojure.157-indexing-sequences
  "http://www.4clojure.com/problem/157.
  Transform a sequence into a sequence of pairs containing the original elements along with their index.")

(defn with-index [xs]
  (map-indexed (fn [idx val] [val idx])
               xs))

(= (with-index [:a :b :c]) [[:a 0] [:b 1] [:c 2]])

(= (with-index [0 1 3]) '((0 0) (1 1) (3 2)))

(= (with-index [[:foo] {:bar :baz}]) [[[:foo] 0] [{:bar :baz} 1]])
