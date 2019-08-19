(ns four-clojure.135-infix-calculator
  "http://www.4clojure.com/problem/135.
  Write a function that accepts a variable length mathematical expression
  consisting of numbers and the operations +, -, *, and /.
  Assume a simple calculator that does not do precedence
  and instead just calculates left to right.")

(defn calculate [x & more]
  (let [ops (partition 2 more)]
    (reduce
     (fn apply-op [acc [op arg]]
       (op acc arg))
     x
     ops)))

(= 7  (calculate 2 + 5))

(= 42 (calculate 38 + 48 - 2 / 2))

(= 8  (calculate 10 / 2 - 1 * 2))

(= 72 (calculate 20 / 2 + 2 + 4 + 8 - 6 - 10 * 9))
