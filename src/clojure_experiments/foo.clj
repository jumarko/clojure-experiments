(ns clojure-experiments.foo)

(defn bar
  "Dummy function used from within `clojure-experiments.idioms` via `requiring-resolve`.
  See https://clojurians.slack.com/archives/C03M5U2LLAC/p1658364375721519"
  [opts]
  (println "options: " opts)
  opts)
