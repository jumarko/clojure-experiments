(ns advent-of-clojure.2017.01
  "Day 1 of Advent of Clojure 2017: http://adventofcode.com/2017/day/1

  Part I:
  ------------------------------------------------------------------------------
  The night before Christmas, one of Santa's Elves calls you in a panic.
  \"The printer's broken! We can't print the Naughty or Nice List!\"
  By the time you make it to sub-basement 17, there are only a few minutes until midnight.
  \"We have a big problem,\" she says; \"there must be almost fifty bugs in this system,
  but nothing else can print The List.
  Stand in this square, quick! There's no time to explain; if you can convince them to pay you in stars, you'll be able to--\"
  She pulls a lever and the world goes blurry.

  When your eyes can focus again, everything seems a lot more pixelated than before.
  She must have sent you inside the computer! You check the system clock: 25 milliseconds until midnight.
  With that much time, you should be able to collect all fifty stars by December 25th.

  Collect stars by solving puzzles. Two puzzles will be made available on each day millisecond in the advent calendar;
  the second puzzle is unlocked when you complete the first. Each puzzle grants one star. Good luck!

  You're standing in a room with \"digitization quarantine\" written in LEDs along one wall.
  The only door is locked, but it includes a small interface. \"Restricted Area - Strictly No Digitized Users Allowed.\"

  It goes on to explain that you may only leave by solving a captcha to prove you're not a human.
  Apparently, you only get one millisecond to solve the captcha: too fast for a normal human, but it feels like hours to you.

  ----------------------------------------------------------------------------------------
  The captcha requires you to review a sequence of digits (your puzzle input)
  and find the sum of all digits that match the next digit in the list.
  The list is circular, so the digit after the last digit is the first digit in the list.
  ----------------------------------------------------------------------------------------

  For example:
    1122 produces a sum of 3 (1 + 2) because the first digit (1) matches the second digit and the third digit (2) matches the fourth digit.
    1111 produces 4 because each digit (all 1) matches the next.
    1234 produces 0 because no digit matches the next.
    91212129 produces 9 because the only digit that matches the next one is the last digit, 9.

  What is the solution to your captcha?

  To begin, bet your puzzle input: http://adventofcode.com/2017/day/1/input


  Part II:
  ------------------------------------------------------------------------------
  You notice a progress bar that jumps to 50% completion.
  Apparently, the door isn't yet satisfied, but it did emit a star as encouragement.
  The instructions change:
    Now, instead of considering the next digit, it wants you to consider
    the digit halfway around the circular list.
    That is, if your list contains 10 items, only include a digit in your sum
    if the digit 10/2 = 5 steps forward matches it.
    Fortunately, your list has an even number of elements.

  For example:
    1212 produces 6: the list contains 4 items, and all four digits match the digit 2 items ahead.
    1221 produces 0, because every comparison is between a 1 and a 2.
    123425 produces 4, because both 2s match each other, but no other digit has a match.
    123123 produces 12.
    12131415 produces 4.
  ")

(defn- read-input
  "Reads input from given file and returns it as a sequence of digits (numbers 0-9)."
  [file-path]
  (let [content (slurp file-path)]
    (prn content)
    (->> content
         (filter #(Character/isDigit %))
         (map #(Character/digit % 10)))))

(def input (read-input "src/advent_of_clojure/2017/01_input.txt"))

;;; generic solution
(defn generic-sum
  [digits paired-digits]
  (->> (map vector digits paired-digits)
       (filter (fn [[digit next-digit]] (= digit next-digit)))
       (map first)
       (apply +)))

;;; PART I:
(defn sum-of-repeated-digits
  "Returns the sum of all digits as required for this task."
  [digits]
  (let [queue (into (clojure.lang.PersistentQueue/EMPTY) digits)]
    (generic-sum queue
                 (conj (pop queue) (peek queue)))))

(sum-of-repeated-digits [1 1 2 2])

(sum-of-repeated-digits input)
;;=> 1136


;;; PART II:
(defn- circular-nth [digits-vector index]
  (nth digits-vector (mod index (count digits-vector))))

;; see https://stackoverflow.com/questions/20805978/circularly-shifting-nested-vectors
;; for shifting vectors
(defn sum-of-repeated-digits-2
  "Returns the sum of all digits as required for this task."
  [digits]
  (let [digits-vector (vec digits)
        halfway-next (/ (count digits) 2)
        digits-shifted-halfway (vec (concat (subvec digits-vector halfway-next)
                                            (subvec digits-vector 0 halfway-next)))]
    (generic-sum digits digits-shifted-halfway)))


(sum-of-repeated-digits-2 [1 2 1 2])
;;=> 6
(sum-of-repeated-digits-2 [1 2 2 1])
;;=> 0
(sum-of-repeated-digits-2 [1 2 3 4 2 5])
;;=> 4
(sum-of-repeated-digits-2 [1 2 3 1 2 3])
;;=> 12
(sum-of-repeated-digits-2 [1 2 1 3 1 4 1 5])
;;=> 4

(sum-of-repeated-digits-2 input)
;;=> 1092
