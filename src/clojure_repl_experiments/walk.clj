(ns clojure-repl-experiments.walk
  (:require [clojure.walk :as w]))

(w/walk prn #(println "RESULT: " %) [[1 2] {:a 1 :b 20} [{:aa 1000}]])
