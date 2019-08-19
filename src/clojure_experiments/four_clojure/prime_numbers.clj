(ns four-clojure.prime-numbers)

;;; http://www.4clojure.com/problem/67
;;; Write a function which returns first x number of prime numbers

(defn prime-numbers [x]
  (letfn [(is-prime [n] (every? #(pos? (mod n %))  (range 2 n)))]
    (take x ( filter is-prime (drop 2 (range))))))

(= (prime-numbers 2)
   [2 3])

(= (prime-numbers 5)
   [2 3 5 7 11])

(= (last ( prime-numbers 100))
   541)
