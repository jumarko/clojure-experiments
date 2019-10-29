(ns clojure-experiments.spectrum
  "Experiments with spectrum - static analyzer using clojure spec: https://github.com/arohner/spectrum
  See also conj 2016 talk: https://www.youtube.com/watch?v=hzV7dFYmbAs"
  (:require [spectrum.flow :as f]
            [spectrum.types :as t]))


(defn foo [i]
  (inc i))

(f/infer-var #'foo)
;;=> couldn't find analysis for #'clojure-experiments.spectrum/foo

;; throws AssertionError
#_(f/infer-form '(foo 3))
