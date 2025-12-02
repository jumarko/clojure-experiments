(ns clojure-experiments.advent-of-code.advent-2025.day-02
  (:require
   [clojure-experiments.advent-of-code.utils :as utils]
   [clojure.string :as str]))


(def sample-input-line "11-22,95-115,998-1012,1188511880-1188511890,222220-222224,1698522-1698528,446443-446449,38593856-38593862,565653-565659,824824821-824824827,2121212118-2121212124")

(defn parse-input [input-line]
  (mapv (fn [range-str]
          (mapv parse-long (str/split range-str #"-")))
        (str/split input-line #",")))

(def sample-input (parse-input sample-input-line))

(def parsed-input (parse-input (first (utils/read-input 2025 2))))
;; preview input:
(take 10 parsed-input)
;; => ([4077 5314]
;;     [527473787 527596071]
;;     [709 872]
;;     [2487 3128]
;;     [6522872 6618473]
;;     [69137 81535]
;;     [7276 8396]
;;     [93812865 93928569]
;;     [283900 352379]
;;     [72 83])

;; to get the sense of how many numbers are within the ranges
(apply + (mapv #(abs (apply - %)) parsed-input))
;; => 2489730 => I need to check about 2.5M numbers

;; copied from  .../purely_functional/puzzles/util.clj
(defn digits
  "Returns all digits of given number"
  [n]
  (assert (pos? n) "Can only work with positive numbers.")
  (loop [n n
         digits '()]
    (if (> n 9)
      (recur (quot n 10) (conj digits (rem n 10)))
      (conj digits n))))

(defn invalid-id? [num]
  (let [digits (digits num)
        n (count digits)]
    (boolean (and (even? n)
                  (apply = (split-at (quot n 2) digits))))))
(assert (true? (invalid-id? 7474)))
(assert (false? (invalid-id? 7475)))

(defn part [parsed-input invalid-id-fn]
  (->> parsed-input
       (mapcat (fn [[first-id last-id]]
                 (range first-id (inc last-id))))
       (filter invalid-id-fn)
       (apply +)))


(defn part-1 [parsed-input]
  (part parsed-input invalid-id?))

(assert (= 1227775554 (part-1 sample-input)))

(assert (= 13108371860 (time (part-1 parsed-input))))
;; "Elapsed time: 559.072833 msecs"



;;; Part 2:
;;; Now, an ID is invalid if it is made only of some sequence of digits repeated _at least_ twice.
;;; So, 12341234 (1234 two times), 123123123 (123 three times), 1212121212 (12 five times), and 1111111 (1 seven times) are all invalid IDs.

(defn invalid-id?
  [num]
  (let [digits (digits num)
        n (count digits)]
    (boolean (some #(apply = %) ; if any of the pattern size gives us a match
                   (map (fn [x] (partition-all x digits))
                        ;; check patterns of size 1 up to half of the digits count
                        (range 1 (inc (quot n 2))))))))
(assert (true? (invalid-id? 123123123)))
(assert (true? (invalid-id? 12341234)))
(assert (true? (invalid-id? 1111111)))
(assert (false? (invalid-id? 1231231234)))

(defn part-2 [parsed-input]
  (part parsed-input invalid-id?))

(assert (= 22471660255 (time (part-2 parsed-input))))
;; "Elapsed time: 6037.115584 msecs"
