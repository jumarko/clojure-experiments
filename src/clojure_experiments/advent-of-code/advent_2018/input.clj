(ns advent-of-clojure.2018.input
  "Helper functions for dealing with input"
  (:require [clojure.java.io :as io]))

(def common-file-prefix "src/advent_of_clojure/2018/")

(defn with-input
  "Reads data from given input file (pass only the name, not the full path!)
  and calls `f` with a sequence of lines from that file.

  Accepts third optional argument which can be used to transform each line
  before `f` is called."
  ([input-file-name f]
   (with-input input-file-name f identity))
  ([input-file-name f line-transform-fn]
   (with-open [input-reader (io/reader (str common-file-prefix input-file-name))]
     (let [lines (map line-transform-fn (line-seq input-reader))]
       (f lines)))))
