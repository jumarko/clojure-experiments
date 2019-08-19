(ns four-clojure.double-down)

(= ((fn double [x] (* x 2)) 2) 4)

(= ((fn [x] (* x 2)) 3) 6)

(= (#(* % 2) 11) 22)

(= ((partial * 2) 7) 14)
