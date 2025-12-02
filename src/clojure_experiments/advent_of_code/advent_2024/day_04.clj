(ns clojure-experiments.advent-of-code.advent-2024.day-04
  "Elf word search.
  https://adventofcode.com/2024/day/4"
  (:require
   [clojure-experiments.advent-of-code.advent-2024.utils :as u]
   [clojure.string :as str]))

(defn read-input []
  (u/read-input "04"))
(def full-input (read-input))

(def sample-input
  "MMMSXXMASM
MSAMXMSMSA
AMXSXMAAMM
MSAMASMSMX
XMASAMXAMM
XXAMMXXAMA
SMSMSASXSS
SAXAMASAAA
MAMMMXMMMM
MXMXAXMASX")

(defn part1 []
  )

(defn parse-lines [input]
  (let [rows (str/split-lines input)
        cols (->> rows (u/transpose) (mapv #(apply str %)))
        _ (assert (= (count rows) (count cols)) "Expected a square (same number of rows and cols)")
        diagonals []]
    (concat rows cols diagonals)))

(parse-lines sample-input)

(map)
