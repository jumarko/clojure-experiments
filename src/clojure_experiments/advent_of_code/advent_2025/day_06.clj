(ns clojure-experiments.advent-of-code.advent-2025.day-06
  "Cephalopod math homework.
  Input: https://adventofcode.com/2025/day/6/input.

  Notice my program is quite long.
  Compare to genmeblog: https://github.com/genmeblog/advent-of-code/blob/master/src/advent_of_code_2025/day06.clj
  "
  (:require
   [clojure-experiments.advent-of-code.utils :as utils]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

(defn transpose [matrix]
  (apply map list matrix))

;;; Specs
(s/def ::fresh-ranges (s/coll-of (s/tuple int? int?)))
(s/def ::available-ingredients (s/coll-of int?))

;;; Input

(s/fdef parse-input
  :args (s/cat :input-lines (s/coll-of string?))
  :ret (s/coll-of list?))
(defn parse-input
  "Parses the list of math problems into a sequence of forms such as
  ['(* x y z) '(+ a b c) ...] where x, y, z, a, b, c, are numbers.
  These forms should be directly evaluable."
  [input-lines]
  (let [nums (mapv (fn [line] (->> (str/split line #"\s+") (keep parse-long)))
                   (butlast input-lines))
        ops (mapv (fn [op]
                    (case op
                      "*" *
                      "+" +))
                  (str/split (last input-lines) #"\s+"))]
    (->> (conj (list* nums) ops)
         transpose)))


(def sample-input (str/split-lines "123 328  51 64 
 45 64  387 23 
  6 98  215 314
*   +   *   +  "))

(def sample-parsed (parse-input sample-input))
;;=>
;; ((#function[clojure.core/*] 123 45 6)
;;  (#function[clojure.core/+] 328 64 98)
;;  (#function[clojure.core/*] 51 387 215)
;;  (#function[clojure.core/+] 64 23 314))

(def full-input (utils/read-input 2025 6))
(def full-parsed (parse-input full-input))


(defn part1 [parsed-input]
  (->> parsed-input
       (map (fn [[op args :as _form]]
              (eval _form)
              #_(apply op args)))
       (apply +)))

(assert (= 4277556 (part1 sample-parsed)))

(part1 full-parsed)
;; => 5060053676136


;;; part2: reading numbers in columns, right to left

;; e.g. (123 45 6) will become (356 24 1)

(->> [123 45 6]
     (map #(-> % str reverse) )
     #_transpose)
;; not quite right...
;; => ((\3 \5 \6))

(defn pad [avec n]
  (let [length (count avec)]
    (if (< length n)
      (apply conj avec (repeat (- n length) nil))
      avec))
  )
(pad [4 5] 3)
;; => [4 5 nil]
(pad [4 5 6] 3)
;; => [4 5 6]

(->> [123 45 6]
     (map #(-> % str reverse vec (pad 3)) )
     transpose
     (map #(->> % (remove nil?) (apply str) parse-long))
     )
;; => (356 24 1)

(defn- fix-line [[op & nums]]
  (let [length (count (str (apply max-key #(-> % str count) nums)))
        proper-nums (->> nums
                         (map #(-> % str reverse vec (pad length)))
                         transpose
                         (map #(->> % (remove nil?) (apply str) parse-long)))]
    (conj proper-nums op)))

(fix-line (first sample-parsed))

(defn part2
  [parsed-input]
  (part1 (mapv fix-line parsed-input)))

(part2 sample-parsed)
;; => 3264322
;; BUT should be 3263827 ?

(part2 full-parsed)
;; => 9648030945893


;;; let's try again...
;;; I think, I need to find the longest number first,
;;; then I need to align all the numbers in the same column properly.
;;; Hmm... but the problem is that the alignment of numbers in the column
;;; is really significant - so I cannot ignore whitespaces!
;;; Therefore, it's too late to try to fix the parsed input  at this point.
;;; => I need to re-do the parsing...

(defn- parse-num [digits-seq]
  (reduce (fn [acc digit] (+ (* 10 acc)
                             (parse-long (str digit))))
          0
          digits-seq))
(parse-num '(\4 \3 \1))
;; => 431

(defn- parse-nums [nums-lines]
  (->> nums-lines
       (map reverse)
       ;; read the numbers in columns
       transpose
       (keep (fn [digits]
               (filter #(not= \space %) digits)))
       ;; group digits belonging to the same logical group (to which the operation in the last row will be applied)
       ;; we know that each such group is followed by a column that has only spaces
       (partition-by empty?)
       ;; remove the spaces
       (remove #(empty? (first %)))
       ;; combine sequences of digits into numbers
       (mapv #(map parse-num %))))
(parse-nums (butlast sample-input))
;; => [(4 431 623) (175 581 32) (8 248 369) (356 24 1)]

(defn- parse-ops [op-line]
  (->> (str/split op-line #"\s+")
       (map (fn [op]
              (case op
                "*" *
                "+" +)))
       reverse))

(defn parse-input2
  "Parses the list of math problems into a sequence of forms such as
  ['(* x y z) '(+ a b c) ...] where x, y, z, a, b, c, are numbers.
  These forms should be directly evaluable."
  [input-lines]
  (let [nums (parse-nums (butlast input-lines))
        ops (parse-ops (last input-lines))]
    (mapv conj nums ops)))

(parse-input2 sample-input)
;; => [(#function[clojure.core/+] 4 431 623)
;;     (#function[clojure.core/*] 175 581 32)
;;     (#function[clojure.core/+] 8 248 369)
;;    (#function[clojure.core/*] 356 24 1)]

;; NOTE: we can reuse `part1` implementation for part2 here!
(assert (= 3263827 (part1 (parse-input2 sample-input))))

(assert (= 9695042567249 (part1 (parse-input2 full-input))))

