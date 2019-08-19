(ns four-clojure.126-through-looking-glass
  "http://www.4clojure.com/problem/126.")

(let [x java.lang.Class]
  (and (= (class x) x) x))
