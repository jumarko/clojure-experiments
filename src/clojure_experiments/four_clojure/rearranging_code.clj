(ns four-clojure.rearranging-code)

;;; thread-first macro: http://www.4clojure.com/problem/71

;; example
(.toUpperCase (str (first [:cat :dog :fish])))
(-> [:cat :dog :fish] first str .toUpperCase)

;; task
(=
 (last (sort (rest (reverse [2 5 4 1 3 6]))))
 (-> [2 5 4 1 3 6] (reverse) (rest) (sort) (last))
 5)



;;; thread-last macro: http://www.4clojure.com/problem/72
;;; especially useful if you want to use threading on collection functions like map, filter, and take where the collection is the last argument

;; example
(->> [1 2 3 4 5 6 7 8] (filter even?) (take 3))

;; task
(= (apply + (map inc (take 3 (drop 2 [2 5 4 1 3 6]))))
 (->> [2 5 4 1 3 6] (drop 2) (take 3) (map inc) (apply +))
 11)
