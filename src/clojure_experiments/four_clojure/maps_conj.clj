(ns four-clojure.maps-conj)

(= {:a 1 :b 2 :c 3} (conj {:a 1} [ :b 2] [:c 3]))
