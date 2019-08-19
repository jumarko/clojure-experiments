(ns four-clojure.vectors-conj)

(= [1 2 3 4] (conj [1 2 3] 4))

(= [1 2 3 4] (conj [1 2 ] 3 4))
