(ns clojure-experiments.advent-of-code.advent-2024.day-03
  (:require
   [clojure-experiments.advent-of-code.advent-2024.utils :as u]))

(defn read-input []
  (u/read-input "03"))

(defn parse-muls
  "Parses a string (one line of the input) into a sequence of multiplications, if any.
  Returns a seq of 2-element vectors, each representing a multiplication of two numbers.
  See the example below."
  [mul-regex line]
  (mapv (fn [[_ x y]] (mapv parse-long [x y]))
        (re-seq mul-regex line)))

(def mul-regex #"mul\((\d{1,3}),(\d{1,3})\)")

(parse-muls mul-regex "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))")
;; => [[2 4] [5 5] [11 8] [8 5]]

(defn part1
  "Scan input for valid 'mul' instructions which have the form `mul(x,y)`,
  where x and y are 1-3 digit integers.
  Then calculate results and all such multiplications and sum them up."
  []
  (->> (read-input)
       (mapcat #(parse-muls mul-regex %))
       (map #(apply * %))
       (apply +)))

(part1)
;; => 183380722
