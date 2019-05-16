(ns clojure-experiments.linters.clj-kondo
  "Experiments with the `clj-kondo` linter: https://github.com/borkdude/clj-kondo")

;;; Doesn't work properly with namespaced maps
(defn my-fn [db-spec id config]
  (println "Saving into db")
  (println "DONE."))
;; => reports 'Wrong number of args (4) ...'
(my-fn
 {}
 1
 #:my-app{:timeout-ms 10000})
