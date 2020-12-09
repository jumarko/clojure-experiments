(ns clojure-experiments.advent-of-code.advent-2020.utils
  (:require [clojure.string :as str]))

(defn read-input
  "Reads data for given puzzle.
  Data are expected to be stored in this directory as XY.txt, where XY is the number of the puzzle;
  e.g. 01.txt"
  [puzzle-number parse-fn]
  (-> (slurp (format "src/clojure_experiments/advent-of-code/advent_2020/%02d.txt" puzzle-number))
      (str/split #"\n")
      (as-> $ (mapv parse-fn $))
      vec))

