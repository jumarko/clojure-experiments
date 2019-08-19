(ns four-clojure.55-count-occurences
  "http://www.4clojure.com/problem/55.
  Count occurences of each distinct item in a sequence
  Don't use `frequencies`.")

(defn freqs
  "Like `frequencies` but my own."
  [xs]
  (loop [freqs-map {}
         xs xs]
    (if (seq xs)
      (recur
       (update freqs-map (first xs) (fnil inc 0))
       (rest xs))
      freqs-map)))

;; alternative implementation for 4clojure.com since it doesn't support Clojure 1.7 (where `update` was added)
(defn freqs
  [xs]
  (loop [freqs-map {}
         xs xs]
    (if (seq xs)
      (let [x (first xs)]
        (recur
         (assoc freqs-map (first xs) (inc (get freqs-map x 0)))
         (rest xs)))
      freqs-map)))

;; but I can use `update-in`!
(defn freqs [xs]
  (reduce
   (fn [freqs-map x]
     (update-in freqs-map [x] (fnil inc 0)))
   {}
   xs))

(= (freqs [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})

(= (freqs [:b :a :b :a :b]) {:a 2, :b 3})

(= (freqs '([1 2] [1 3] [1 3])) {[1 2] 1, [1 3] 2})

