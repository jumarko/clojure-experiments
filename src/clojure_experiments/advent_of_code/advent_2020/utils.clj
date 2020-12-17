(ns clojure-experiments.advent-of-code.advent-2020.utils
  (:require [clojure.string :as str]))

(defn read-input
  "Reads data for given puzzle.
  Applies the `parse-line-fn` to every line if not nil;
  otherwise returns the whole input as string as is - don't even try to split it by newlines!
  YOu can provide alternative `split-regex` - e.g. splitting by two new lines #\"\\R\\R\"
  Data are expected to be stored in this directory as XY.txt, where XY is the number of the puzzle;
  e.g. 01.txt"
  ([puzzle-number]
   (read-input puzzle-number nil))
  ([puzzle-number parse-line-fn]
   (read-input puzzle-number parse-line-fn #"\R"))
  ([puzzle-number parse-line-fn split-regex]
   (let [content (slurp (format "src/clojure_experiments/advent_of_code/advent_2020/%02d.txt" puzzle-number))]
     (if parse-line-fn
       (mapv parse-line-fn (str/split content split-regex))
       content))))

