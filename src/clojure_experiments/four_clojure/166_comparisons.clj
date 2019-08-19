(ns four-clojure.166-comparisons
  "http://www.4clojure.com/problem/166.
  Write a function that takes three arguments, a less than operator for the data and two items to compare. The function should return a keyword describing the relationship between the two items.
  The keywords for the relationship between x and y are as follows:
  x = y → :eq
  x > y → :gt
  x < y → :lt")

(defn comparison [comp-fn v1 v2]
  (cond
    (comp-fn v1 v2)
    :lt

    (comp-fn v2 v1)
    :gt

    :else
    :eq))

(= :gt (comparison < 5 1))

(= :eq (comparison (fn [x y] (< (count x) (count y))) "pear" "plum"))

(= :lt (comparison (fn [x y] (< (mod x 5) (mod y 5))) 21 3))

(= :gt (comparison > 0 2))

