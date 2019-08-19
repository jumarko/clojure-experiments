(ns four-clojure.let-it-be)

(= 10 (let [x 7 y 3] (+ x y)))

(= 4 (let [y 3 z 1] (+ y z)))

(= 1 (let [z 1] z))
