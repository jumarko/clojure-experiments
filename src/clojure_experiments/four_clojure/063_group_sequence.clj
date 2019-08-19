(ns four-clojure.063-group-sequence
  "http://www.4clojure.com/problem/63.
  Given a function f and a sequence s,
  write a function which returns a map.
  The keys should be the values of f applied to each item in s.
  The value at each key should be a vector of corresponding items
  in the order they appear in s.

  Special restrictions: group-by")

(defn my-group [f xs]
  (reduce (fn [m x]
            ;; cannot use `update` because it was added in 1.7
            (update m (f x) #(conj (if % % []) x)))
          {}
          xs))

(defn my-group2 [f xs]
  (reduce (fn [m x]
            (assoc m (f x) (conj (get m (f x) []) x)))
          {}
          xs))

(= (my-group2 #(> % 5) [1 3 6 8]) {false [1 3], true [6 8]})

(= (my-group2 #(apply / %) [[1 2] [2 4] [4 6] [3 6]])
   {1/2 [[1 2] [2 4] [3 6]], 2/3 [[4 6]]})

(= (my-group2 count [[1] [1 2] [3] [1 2 3] [2 3]])
   {1 [[1] [3]], 2 [[1 2] [2 3]], 3 [[1 2 3]]})
