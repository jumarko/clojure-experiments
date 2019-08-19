(ns four-clojure.173-intro-to-destructuring2
  "http://www.4clojure.com/problem/173.")

(= 3
   (let [[sum nums] [+ (range 3)]] (apply sum nums))
   (let [[[sum a] b] [[+ 1] 2]] (sum a b))
   (let [[op-inc num] [inc 2]] (op-inc num)))

