(ns clojure-experiments.reducers
  (:require [clojure.core.reducers :as r]))

;;;; https://functional.works-hub.com/learn/reducers-and-transducers-introductory-d0cff

;; this works:
(nth (map inc [1 2 3]) 2)

;; but reducers returns "reducible"
#_(nth (r/map inc [1 2 3]) 2)
;;=> UnsupportedOperationException

(->> [1 2 3]
     (r/map inc)
     (into []))


