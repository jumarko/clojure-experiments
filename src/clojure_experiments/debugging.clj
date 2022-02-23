(ns clojure-experiments.debugging
  "Experiments with debugging code,
  perhaps using Cider debugger: https://docs.cider.mx/cider/debugging/debugger.html")


(defn baz [z]
  (let [zz (+ 10 z)
        zzs (repeat zz z)]
    (mapv inc zzs)))

(defn bar [y]
  (let [yy (+ 5 y)]
    (baz yy)))

(defn foo [x]
  (bar (inc x)))


(comment

  (foo 3)

  ,)
