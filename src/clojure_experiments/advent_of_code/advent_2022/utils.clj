(ns clojure-experiments.advent-of-code.advent-2022.utils
  "See also `clojure-experiments.advent-of-code.advent-2020.utils`."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn read-input [day-num]
  (->> (slurp (io/reader (format "src/clojure_experiments/advent_of_code/advent_2022/day_%s.txt"
                                 day-num)))
       str/split-lines))


(defmacro assert=
  "checks whether all of `args` are equal to `x` and throws an exception if it not.
  The exception message contains the values for all the arguments."
  ([x y & args]
   (when *assert*
     `(let [x# ~x, y# ~y, args# ~(vec args)]
        (when-not (apply = x# y# args#)
          (throw (new AssertionError (format "Assert failed: %s differs from at least one of %s" (pr-str x#) (pr-str (conj args# y#))))))))))
(comment
  (assert= 1 1)
  ;; => nil

  (assert= 1 (inc 0))
  ;; => nil

  (assert= (inc 1) 2 (dec 3) 2)
  ;; => nil

  (assert= (inc 1) 2 (dec 3) 3)
  ;; 1. Unhandled java.lang.AssertionError
  ;; Assert failed: 2 differs from at least one of [2 3 2]
  .)


