(ns clojure-experiments.advent-of-code.advent-2025.day-03
  "Input: https://adventofcode.com/2025/day/3/input.

  Elevators are fried so you need to take the escalators to get further down.
  Howevor, escalators don't work either - but, you can use the emergency power supplies (batteries).
  The batteries are each labeled with their joltage rating, a value from 1 to 9:
      987654321111111
      811111111111119
      234234234234278
      818181911112111
  The batteries are arranged into banks; each line of digits in your input corresponds
  to a single bank of batteries. Within each bank you need to turn on *exactly two batteries*.
  You'll need to find the largest possible joltage each bank can produce

  "
  (:require
   [clojure-experiments.advent-of-code.utils :as utils]
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]))

;;; Specs
(s/def ::joltage pos-int?)
(s/def ::bank (s/coll-of ::joltage))


;;; Input
(def sample-input ["987654321111111" ; 98
                   "811111111111119" ; 89
                   "234234234234278" ; 78
                   "818181911112111"]) ; 92

(defn parse-banks [input-lines]
  (mapv (fn [line] (mapv #(parse-long (str %)) line))
        input-lines))
(def sample-banks (parse-banks sample-input))
;; => [[9 8 7 6 5 4 3 2 1 1 1 1 1 1 1]
;;     [8 1 1 1 1 1 1 1 1 1 1 1 1 1 9]
;;     [2 3 4 2 3 4 2 3 4 2 3 4 2 7 8]
;;     [8 1 8 1 8 1 9 1 1 1 1 2 1 1 1]]

(def parsed-banks (parse-banks (utils/read-input 2025 3)))
;; preview input:
(take 5 parsed-banks)
;; => ([2 2 1 5 4 5 2 6 8 9 9 2 5 2 4 4 2 7 3 2 4 4 3 3 3 4 3 6 1 8 9 3 1 7 4 4 6 3 8 4 8 3 8 4 7 8 5 2 5 4 7 8 8 2 4 4 3 5 2 3 3 3 4 2 3 5 2 2 3 6 2 5 5 6 2 4 3 2 6 7 6 7 3 5 5 4 3 8 7 5 3 4 9 3 2 2 2 4 2 3]
;;     [1 2 2 2 2 3 2 3 2 3 2 2 2 2 3 2 1 3 2 2 2 2 3 2 3 2 2 1 2 2 6 2 2 2 2 2 5 2 1 2 2 2 1 2 1 3 2 3 2 1 2 2 2 3 2 3 1 1 1 5 2 2 3 2 2 2 3 2 1 2 1 2 3 6 2 2 1 1 1 2 1 2 2 2 3 1 6 2 3 2 2 2 2 1 3 2 3 2 1 1]
;;     [3 7 8 6 6 4 5 7 3 7 4 4 6 3 6 3 5 5 4 4 6 3 6 5 6 5 4 4 6 6 7 3 7 2 8 6 4 4 6 5 5 4 5 4 3 4 5 4 5 4 3 5 7 4 4 3 4 5 7 6 6 5 5 3 3 4 3 4 4 6 9 4 3 5 3 1 5 3 7 6 2 7 7 4 6 2 5 3 5 5 6 2 3 3 6 3 4 4 6 3]
;;     [8 4 5 3 3 3 2 3 5 2 5 6 5 1 4 2 1 4 3 5 2 4 5 3 1 4 4 6 3 3 5 4 4 3 4 3 7 3 2 5 3 2 5 1 5 2 2 4 3 2 3 4 6 2 3 5 5 4 5 3 3 5 3 2 3 1 4 5 1 3 8 5 4 5 2 4 5 3 4 5 3 2 5 3 4 2 2 4 2 4 2 3 4 4 4 3 4 5 2 5]
;;     [3 5 3 3 2 4 3 4 4 9 2 6 4 4 2 7 6 4 3 5 4 2 4 6 8 4 5 2 2 4 4 7 1 8 2 4 8 3 1 5 3 4 5 7 3 3 6 3 4 1 3 3 3 5 2 1 3 6 4 4 1 2 7 5 6 2 6 2 1 3 5 5 3 3 4 7 8 6 6 3 3 4 2 2 2 4 5 4 3 2 3 7 5 4 3 3 5 4 5 3])

;; #Attempt 1: sorting
(defn bank-joltage [bank]
  ;; first native attempt
  (take-last 2 (sort bank)))
(bank-joltage (first sample-banks))
;; => (8 9) ; notice it's in the wrong order!

;; Attempt #2: maybe I can simply find max (+ its index), a couple of times?
(comment
  (apply max [1 9 1 8 7 6 1])
  ;; => 9
  (let [xs [1 9 1 8 7 6 1]]
    (.indexOf xs (apply max xs)))
  ;; => 1
  ;; get the max element and remove it from the vector
  (let [xs [1 9 1 8 7 6 1]
        max-index (.indexOf xs (apply max xs))
        xs-count (count xs)]
    (into (subvec xs 0 max-index)
          (when (< max-index xs-count) (subvec xs (inc max-index) xs-count))))
  ;; => [1 1 8 7 6 1]

  ;; now find the max of the vector with removed original max
  (let [xs [1 9 1 8 7 6 1]
        max1 (apply max xs)
        max-index (.indexOf xs max1)
        xs-count (count xs)
        xs-without-max (into (subvec xs 0 max-index)
                             (when (< max-index xs-count) (subvec xs (inc max-index) xs-count)))
        max2 (apply max xs-without-max)]
    [max1 max2])
  ;; => [9 8]

  :-)

(defn- remove-index
  "Removing an element from vector isn't an efficient operation but `subvec` is supposed to be fast"
  [avec idx]
  (let [length (count avec)]
    (into (subvec avec 0 idx)
          ;; append the rest of the vector, but only if there are any items left
          (when (< idx length) (subvec avec (inc idx) length)))))
(remove-index [1 2 3 4 5] 4)
;; => [1 2 3 4]
(remove-index [1 2 3 4 5] 2)
;; => [1 2 4 5]


(s/fdef bank-joltage
  :args (s/cat :bank ::bank)
  :ret (s/every ::joltage :kind vector? :count 2))
(defn bank-joltage [bank]
  (let [max1 (apply max bank)
        max-index (.indexOf bank max1)
        xs-without-max (remove-index bank max-index)
        max2 (apply max xs-without-max)]
    [max1 max2]))

(assert [9 8] (= (bank-joltage (first sample-banks))))
;; => [9 8]
(mapv bank-joltage sample-banks)
;; => [[9 8] [9 8] [8 7] [9 8]]
;;=> This still isn't right - the order isn't preserved, rather it's always [max, second-max]




;; #Attempt 2b: let's put the max elements in orders
(defn bank-joltage [bank]
  (let [max1 (apply max bank)
        max1-index (.indexOf bank max1)
        xs-without-max (remove-index bank max1-index)
        max2 (apply max xs-without-max)
        max2-index (.indexOf bank max2)
        top-joltages (if (< max1-index max2-index)
                       [max1 max2]
                       [max2 max1])]
    top-joltages))
(mapv bank-joltage sample-banks)
;; => [[9 8] [8 9] [7 8] [8 9]]
;; >???
;; Why is the last number 89 but the instructions say 92??

;; AH, I misunderstood it.
;; So it really needs to start with the max digit and then append the largest one
;; among the _rest_ (after the max) of the digits.
;; ... except that the max can be the _last_ digit in which case we need to take the max from before
;; Attempt #3:
(defn- top-batteries [bank]
  (let [max1 (apply max bank)
        max1-index (.indexOf bank max1)]
    (if (< max1-index (dec (count bank)))
      ;; if not last elem then pick the largest value of the rest of the bank
      [max1 (apply max (subvec bank (inc max1-index) (count bank)))]
      ;; if it's the last elem, then find the max among all the preceeding digits
      ;; and put that one first
      [(apply max (subvec bank 0 max1-index)) max1])))

(mapv top-batteries sample-banks)
;; => [[9 8] [8 9] [7 8] [9 2]]

(defn bank-joltage [bank]
  (let [[a b](top-batteries bank)]
    (+ (* 10 a) b)))

(mapv bank-joltage sample-banks)
;; => [98 89 78 92]

(defn part1 [banks]
  (apply + (mapv bank-joltage banks)))

(assert (= 357 (part1 sample-banks)))
;; => 357

(assert (= 17346 (part1 parsed-banks)))
