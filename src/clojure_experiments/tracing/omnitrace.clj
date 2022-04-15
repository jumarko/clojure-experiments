(ns clojure-experiments.tracing.omnitrace
  "https://github.com/Cyrik/omni-trace"
  ;; this leads to circular dependency error in aws logs?!
  #_(:require
   [cyrik.omni-trace :as o]))



;; try this in `clojure-experiments.aws.logs`
#_(o/rooted-flamegraph 'clojure-experiments.aws.logs/get-all-data)
