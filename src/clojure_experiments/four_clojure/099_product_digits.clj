(ns four-clojure.099-product-digits
  "http://www.4clojure.com/problem/99.
  Write a function which multiplies two numbers and returns the result as a sequenc of digits.")

(defn multiply [x y]
  (letfn [(number->digits [number]
            (map #(Integer/parseInt (str %)) (str number)))]
    (let [product (* x y)]
      (number->digits product))))

(= [1]
   (multiply 1 1))

(= [8 9 1]
   (multiply 99 9))

(= [9 8 9 0 1]
   (multiply 999 99))
