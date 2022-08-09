(ns clojure-experiments.graphs.loom
  "Experiments with loom graph library: https://github.com/aysylu/loom
   From lisp-in-small-pieces slack https://lisp2022.slack.com/archives/C03C3NMCM7T/p1659860280125529?thread_ts=1659808272.992099&cid=C03C3NMCM7T
   - For graphs there is a mature library “Loom” which defines protocols and provides implementations for many common graphs and common graph algorithms.
   - It is an excellent, well tested and well thought out library that works well.
   - For most use cases it will make your life much easier to use it."
  (:require [loom.graph :as lg]
            [loom.io :as lio]))


(def g (lg/graph [1 2] [2 3] {3 [4] 5 [6 7]} 7 8 9))
(def dg (lg/digraph g))

(println g)
(comment
  ;; view graphviz representation
  (lio/view g)
  (lio/view dg)
  .)
