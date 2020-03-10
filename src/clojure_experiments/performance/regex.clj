(ns clojure-experiments.performance.regex
  (:require [clj-java-decompiler.core :refer [decompile disassemble] :as decompiler]))


;;; Experiment with regex performance

#_(decompile (re-find #"ahoj" "mystring"))

(defn regex-test []
  (first (mapv
    (fn [x] (re-find #"ah" x))
    (repeat 1000000 "ahoj"))))
#_(decompile regex-test)
(time (regex-test ))

(def my-regex #"ah")
(defn regex-test2 []
  (first (mapv
    (fn [x] (re-find my-regex x))
    (repeat 1000000 "ahoj"))))
(time (regex-test2 ))
