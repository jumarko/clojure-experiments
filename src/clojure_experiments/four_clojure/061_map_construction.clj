(ns four-clojure.61-map-construction)

;;; http://www.4clojure.com/problem/61
;;; Write a function which takes a vector of keys and a vector of values and constructs a map from them.

(defn zip-map [keys vals]
  (into {} (map vector keys vals)))


(= (zip-map [:a :b :c] [1 2 3])
   {:a 1 :b 2 :c 3})

(= (zip-map [1 2 3 4] ["one" "two" "three"])
   {1 "one" 2 "two" 3 "three"})

(= (zip-map [:foo :bar] ["foo" "bar" "baz"])
   {:foo "foo" :bar "bar"})
