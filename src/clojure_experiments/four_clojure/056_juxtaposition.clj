(ns four-clojure.054-juxtaposition
  "Take a set of functions and return a new function that takes a variable number of arguments
  and returns a sequence containing the result of applying each function left-to-right to the argument list.
  See http://www.4clojure.com/problem/59")


(defn my-juxt
  [& fns]
  (fn [& args]
    (mapv
     (fn [afn] (apply afn args))
     fns)))

((my-juxt :a :b) {:a 1 :b 2 :c 3 :d 100})
