(ns clojure-experiments.advent-of-code.utils
  (:require
   [clojure.string :as str]))

(defn read-input
  "Reads data for given puzzle.
  Applies the `parse-line-fn` to every line if not nil;
  otherwise returns the whole input as string as is - don't even try to split it by newlines!
  YOu can provide alternative `split-regex` - e.g. splitting by two new lines #\"\\R\\R\"
  Data are expected to be stored in this directory as XY.txt, where XY is the number of the puzzle;
  e.g. 01.txt"
  ([year puzzle-number]
   (read-input year puzzle-number nil))
  ([year puzzle-number parse-line-fn]
   (read-input year puzzle-number parse-line-fn #"\R"))
  ([year puzzle-number parse-line-fn split-regex]
   (let [content (slurp (format "src/clojure_experiments/advent_of_code/advent_%d/day_%02d.txt" year puzzle-number))
         lines (str/split content split-regex)]
     (if parse-line-fn
       (mapv parse-line-fn lines)
       lines))))

(comment
  (read-input 2025 1)
  ;;=>
  ;; ["R11"
  ;;  "R8"
  ;;  "L47"
  ;; ...
  :-)
