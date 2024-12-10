(ns clojure-experiments.advent-of-code.advent-2024.day-03
  (:require
   [clojure-experiments.advent-of-code.advent-2024.utils :as u]
   [clojure.string :as str]))

(defn read-input []
  (u/read-input "03"))

(def mul-regex #"mul\((\d{1,3}),(\d{1,3})\)")

(defn parse-muls
  "Parses a string (one line of the input) into a sequence of multiplications, if any.
  Returns a seq of 2-element vectors, each representing a multiplication of two numbers.
  See the example below."
  [line]
  (mapv (fn [[_ x y]] (mapv parse-long [x y]))
        (re-seq mul-regex line)))

(parse-muls "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))")
;; => [[2 4] [5 5] [11 8] [8 5]]

(defn calculate [muls]
  (->> muls
   (map #(apply * %))
   (apply +)))

(defn part1
  "Scan input for valid 'mul' instructions which have the form `mul(x,y)`,
  where x and y are 1-3 digit integers.
  Then calculate results and all such multiplications and sum them up."
  []
  (->> (read-input)
       (mapcat parse-muls)
       calculate))

(assert (= 183380722
           (part1)))

;;; Part2
;;; Can I solve this using regex again? Efficiently?

(def do-dont-regex #"(?=(do|don't)\(\))")

(def my-line "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))")
(str/split my-line do-dont-regex)
;; => ["xmul(2,4)&mul[3,7]!^"
;;     "don't()_mul(5,5)+mul(32,64](mul(11,8)un"
;;     "do()?mul(8,5))"]

(defn parse-muls2
  "Parses all multiplications in given string, ignoring those those preceded by don't."
  [program]
  ;; each line is split into segments delimited by "do" or "don't";
  ;; and we ignore segments starting with "don't"
  (->> (str/split program do-dont-regex)
       (remove #(str/starts-with? % "don't()"))
       (mapcat parse-muls)))
(parse-muls2 my-line)
;; => ([2 4] [8 5])

(defn part2
  "Like part1 but with do and don't instructions.
  E.g.
  xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))"
  []
  (->> (read-input)
       ;; combine the whole program into a single line - this is also why there's no `mapcat` below (when calling parse-muls2)
       ;; this is necessary to make parse-muls2 work properly - otherwise it would automatically forget don't instructions
       ;; activated on the previous line and thus would erroneously include more `mul()` instructions than desired.
       (apply str)
       (parse-muls2)
       calculate))

(assert (= 82733683
           (part2)))
