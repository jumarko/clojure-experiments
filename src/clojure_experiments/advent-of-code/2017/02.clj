(ns advent-of-clojure.2017.02
  "Day 2 of Advent of Clojure 2017 - Corruption Checksum: http://adventofcode.com/2017/day/2

  Part I:
  ------------------------------------------------------------------------------
  As you walk through the door, a glowing humanoid shape yells in your direction.
  \"You there! Your state appears to be idle. Come help us repair the corruption in this spreadsheet
  - if we take another millisecond, we'll have to display an hourglass cursor!\"

  The spreadsheet consists of rows of apparently-random numbers.
  To make sure the recovery process is on the right track,
  they need you to calculate the spreadsheet's checksum.
  For each row, determine the difference between the largest value and the smallest value;
  the checksum is the sum of all of these differences.

  For example, given the following spreadsheet:
    5 1 9 5
    7 5 3
    2 4 6 8
  The first row's largest and smallest values are 9 and 1, and their difference is 8.
  The second row's largest and smallest values are 7 and 3, and their difference is 4.
  The third row's difference is 6.
  In this example, the spreadsheet's checksum would be 8 + 4 + 6 = 18.

  What is the checksum for the spreadsheet in your puzzle input? (http://adventofcode.com/2017/day/2/input)

  Part II:
  ------------------------------------------------------------------------------
  \"Great work; looks like we're on the right track after all. Here's a star for your effort.\"
  However, the program seems a little worried. Can programs be worried?
  \"Based on what we're seeing, it looks like all the User wanted is some information
  about the evenly divisible values in the spreadsheet. Unfortunately, none of us are equipped
  for that kind of calculation - most of us specialize in bitwise operations.\"

  It sounds like the goal is to find the only two numbers in each row where one evenly divides the other
  - that is, where the result of the division operation is a whole number.
  They would like you to find those numbers on each line, divide them, and add up each line's result.

  For example, given the following spreadsheet:
    5 9 2 8
    9 4 7 3
    3 8 6 5
  In the first row, the only two numbers that evenly divide are 8 and 2; the result of this division is 4.
  In the second row, the two numbers are 9 and 3; the result is 3.
  In the third row, the result is 2.
  In this example, the sum of the results would be 4 + 3 + 2 = 9.

  What is the sum of each row's result in your puzzle input?")


(defn- read-input
  "Reads input from given file and returns it as a sequence of digits (numbers 0-9)."
  [file-path]
  (with-open [rdr (clojure.java.io/reader file-path)]
    ;; force sequence eval with `mapv`
    (mapv (fn [row]
            (mapv (fn [num-string] (Long/parseLong num-string))
                  (clojure.string/split row #"\s+")))
     (line-seq rdr))))

(def input-rows (read-input "src/advent_of_clojure/2017/02_input.txt"))

;;; PART I:
;;;
(defn- checksum
  "Computes the checksum of all given rows represented as a sequence of number sequences."
  [spreadsheet-rows]

  (apply + 
         (for [row spreadsheet-rows]
           (let [max-num (apply max row)
                 min-num (apply min row)]
             (- max-num min-num)))))
  
(checksum [[5 1 9 5]
           [7 5 3]
           [2 4 6 8]])
;;=> 18
(checksum input-rows)
;;=> 51833


;;; PART II:
;;;

(defn- find-factors [n candidate-factors]
  (some
   #(cond
      (zero? (rem n %)) [n %]
      (zero? (rem % n)) [% n])
   candidate-factors))

(find-factors 9 [7 4 3])
;;=> [9 3]
(find-factors 3 [7 4 9])
;;=> [9 3]

(defn- find-divisibles
  "Returns a pair (vector) of two numbers in given sequence which are evenly divisible.
  The greater number is returned as the first element of vector, the smaller number as the second element."
  [numbers]
  (loop [num (first numbers)
         others (rest numbers)]
    (when (and num others)
      (if-let [factors (find-factors num others)]
        factors
        (recur (first others) (rest others))))))

(find-divisibles [5 9 2 8])
;;=> [8 2]
(find-divisibles [9 4 7 3])
;;=> [9 3]
(find-divisibles [3 8 6 5])
;;=> [6 3]

(defn- checksum-2
  "Computes the checksum of all given rows represented as a sequence of number sequences."
  [spreadsheet-rows]
  (apply +
         (for [row spreadsheet-rows]
           (let [[x y] (find-divisibles row)]
             (/ x y)))))

(checksum-2 [[5 9 2 8]
           [9 4 7 3]
           [3 8 6 5]])
;;=> 9

(checksum-2 input-rows)
