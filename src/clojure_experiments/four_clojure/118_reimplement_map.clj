(ns four-clojure.118-reimplement-map
  "http://www.4clojure.com/problem/118.
  Given a function f and an input sequence s, return a lazy sequence of (f x) for each element x in s.
  Special restrictions: map, map-indexed, mapcat, for")

;; non-lazy version
(defn my-map [f s]
  (reduce #(conj %1 (f %2))
          []
          s))

;; lazy version
(defn my-map [f xs]
  (when-let [s (seq xs)]
    (lazy-seq
     (cons (f (first s)) (my-map f (rest s))))))

(= [3 4 5 6 7]
   (my-map inc [2 3 4 5 6]))

(= (repeat 10 nil)
   (my-map (fn [_] nil) (range 10)))

(= [1000000 1000001]
   (->> (my-map inc (range))
        (drop (dec 1000000))
        (take 2)))


