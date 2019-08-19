(ns four-clojure.advanced-destructuring)

;;; http://www.4clojure.com/problem/51

;; Example - destructuring vector
(let [[a b & c] ["cat" "dog" "bird" "fish"]]
  [a b])

(let [[a b & c] ["cat" "dog" "bird" "fish"]]
  c)

(let [[a b :as x] ["cat" "dog" "bird" "fish"]]
  x)


;; Problem
(= [1 2 [3 4 5] [1 2 3 4 5]] (let [[a b & c :as d] [1 2 3 4 5]] [a b c d]))
