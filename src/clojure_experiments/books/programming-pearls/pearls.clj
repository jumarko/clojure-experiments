(ns clojure-experiments.books.programming-pearls.pearls
  (:require [clj-memory-meter.core :as mm]))


;;;; Book Programming Pearls

;;; Chapter 1
;;; Problem input file which contains at most n integers from 1 to n, where n = 1e7.
;;; No number can repeat -> fatal error.
(def input-data
  (shuffle (range 1000)))

(take 10 input-data)

(defn sort-input [input]
  (let [input-bits (java.util.BitSet. (count input))]
    (doseq [x input]
      (.set input-bits x))
    (println "Memory: " (mm/measure input-bits))
    #_(dotimes [i (count input)]
      (when (.get input-bits i)
        (println i)))))

(sort-input input-data)
