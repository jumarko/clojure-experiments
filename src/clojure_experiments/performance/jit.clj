(ns clojure-experiments.performance.jit)


;;; try this with `jstat -printcompilation <PID> 1000`
(defn hot-loop [n]
  (dotimes [i n]
    (when (zero? (mod i 1000))
      (println i))))
    
#_(hot-loop 10000	0)
