(ns four-clojure.map-defaults)

;;; http://www.4clojure.com/problem/156
;;; Solutions: http://www.4clojure.com/problem/solutions/156

;; you can specify default value when retrieving value from map
(= 2 (:foo {:bar 0 :baz 1} 2))

;; what if you want to create a map with default values for all given keys?
(defn map-with-defaults [default-value keys]
  (into {} (map
            (fn [key] [key default-value])
            keys)))

(= (map-with-defaults 0 [:a :b :c])
   {:a 0 :b 0 :c 0})

(= (map-with-defaults "x" [1 2 3])
   {1 "x" 2 "x" 3 "x"})

(= (map-with-defaults [:a :b] [:foo :bar])
   {:foo [:a :b] :bar [:a :b]})


(into {} (map (fn [x] [x "x"]) [:a :b :c]))


;;; Other solutions
(#(zipmap %2 (repeat %1)) "default" [:a :b :c])

(#(into {} (map vector %2 (repeat %))) "default" [:a :b :c])
