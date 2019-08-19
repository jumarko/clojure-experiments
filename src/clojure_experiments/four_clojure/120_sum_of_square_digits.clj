(ns four-clojure.120-sum-of-square-digits
  "http://www.4clojure.com/problem/120
  Given a collection, return the number of elements that are smaller than the sum of their
  squared component digits.
  E.g. 10 is larger than 1 squared plus 0 squared.")

(defn count-small-digits [coll]
  (letfn [(sum-of-squared-digits [n]
            (apply +
                   (map (fn square-sum [digit]
                          (let [digit-num (Character/getNumericValue digit)]
                            (* digit-num digit-num)))
                        (str n))))]
    (->> coll
         (filter #(< % (sum-of-squared-digits %)))
         count)))

(= 8 (count-small-digits (range 10)))

(= 19 (count-small-digits (range 30)))

(= 50 (count-small-digits (range 100)))

(= 50 (count-small-digits (range 1000)))
