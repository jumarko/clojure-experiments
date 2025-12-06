(ns clojure-experiments.advent-of-code.advent-2025.day-06
  "Cephalopod math homework.
  Input: https://adventofcode.com/2025/day/6/input.
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
