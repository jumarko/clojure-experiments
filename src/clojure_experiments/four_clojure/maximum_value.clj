(ns four-clojure.maximum-value)

;;; Maximum value: http://www.4clojure.com/problem/38

(defn maximum-value [& args]
  (reduce #(if (> %1 %2) %1 %2) args))

(= (maximum-value 1 8 3 4) 8)

(= (maximum-value 30 20) 30)

(= (maximum-value 45 67 11) 67)


;; Other solutions
((comp last sort list) 45 67 11)

(#(last (sort %&)) 45 67 11)
