(ns clojure-experiments.core-match
  "Examples of core.match useage.
  See https://github.com/clojure/core.match/wiki/Overview"
  (:require [clojure.core.match :refer [match]]))

(let [x true
      y true
      z true]
  (match [x y z]
         [_ false true] 1
         [false true _ ] 2
         [_ _ false] 3
         [_ _ true] 4))
                                        ;=> 4

(let [x {:a 1 :b 1}]
  (match [x]
         [{:a _ :b 2}] :a0
         [{:a 1 :b _}] :a1
         [{:c 3 :d _ :e 4}] :a2))

(let [x {:a 1 :b 1}]
  (match [x]
         [{:a _}] :a0
         [{:a 1 :b _}] :a1
         [{:c 3 :d _ :e 4}] :a2))
;;=> :a0
