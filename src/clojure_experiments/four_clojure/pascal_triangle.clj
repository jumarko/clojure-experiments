(ns four-clojure.pascal-triangle)

;;; http://www.4clojure.com/problem/97
;;; Write a function which returns the nth row of Pascal's Triangle

(defn pascal-triangle [row-num]
  (if (= 1 row-num)
    [1]
    (let [previous-row (pascal-triangle (dec row-num))
          current-row (repeat row-num 1)]
      (map-indexed
       (fn [index element]
         (if (< 0 index (dec row-num))
           ;; the elements in the middle are computed as a sum of two elements from previous row
           (+ (nth previous-row (dec index))
              (nth previous-row index))
           ;; the first and last element of new row is just one
           1))
       current-row))))

(= (pascal-triangle 1) [1])

(= (map pascal-triangle (range 1 6))
   [
        [1]
       [1 1]
      [1 2 1]
     [1 3 3 1]
    [1 4 6 4 1]
    ])

(= (pascal-triangle 11) [1 10 45 120 210 252 210 120 45 10 1])
