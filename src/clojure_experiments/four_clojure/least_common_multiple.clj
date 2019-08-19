(ns four-clojure.least-common-multiple)

;;; http://www.4clojure.com/problem/100
;;; Write a function which computes a least common multiple (smallest positive integer divisible by both numbers).
;;; Your function should accept a variable number of positive integers or ratios.

(defn lcm [& args]
  (loop [min-arg (apply min args)
         min-arg-multiple min-arg]
    (if (every? #(zero? (mod min-arg-multiple %)) args)
      min-arg-multiple
      (recur min-arg (+ min-arg-multiple min-arg)))))

(== (lcm 2 3) 6)

(== (lcm 5 3 7) 105)

(== (lcm 1/3 2/5) 2)

(== (lcm 3/4 1/6) 3/2)

(== (lcm 7 5/7 2 3/5) 210)
