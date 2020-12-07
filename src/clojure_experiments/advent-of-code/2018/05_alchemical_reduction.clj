(ns advent-of-clojure.2018.05-alchemical-reduction
  "https://adventofcode.com/2018/day/5"
  (:require [advent-of-clojure.2018.input :as io]
            [clojure.test :refer [deftest is testing]]))

;;;; Suit's material is composed from extremely long polymers.
;;;; Polymer consists of smaller subunits which, when triggered,
;;;; react with each other -> adjacent units of opposite polarity are destroyed;
;;;; e.g. `r` and `R` are units with the same type but opposite polarity.

(defn opposite-polarity?
  [a b]
  (and (not= a b)
       (= (Character/toLowerCase a) (Character/toLowerCase b))))

;; This is Mike Fikes' solution
;; Dramatic reduction of complexity comes from traversing the whole collection only once
;; and using `peek` and `pop`
(defn add-unit [polymer unit]
  (if (some-> (peek polymer) (opposite-polarity? unit))
    (pop polymer)
    (conj polymer unit)))

(defn reduce-polymer [polymer]
  (apply str (reduce
              add-unit
              []
              polymer)))

(defn read-input []
  (-> (str io/common-file-prefix "05_input.txt")
      slurp
      clojure.string/trim-newline))

(defn puzzle1 []
  (count (reduce-polymer (read-input))))

#_(time (puzzle1))
;; "Elapsed time: 700.105373 msecs"

(defn reduce-polymer-max
  [polymer]
  (let [unit-types (into {} (map #(Character/toLowerCase %) polymer))
        candidates (->> unit-types
                        (map (fn remove-unit
                               [unit-type]
                               (remove #(= (Character/toLowerCase %)
                                           unit-type)
                                       polymer))))
        reduced-candidates (mapv reduce-polymer candidates)]
    (apply min-key count reduced-candidates)))

(defn puzzle2 []
  (count (reduce-polymer-max (read-input))))

#_(time (puzzle2))
;; "Elapsed time: 28479.649817 msecs"

(deftest polymer-reduction-test
  (testing "units with same type but opposing polarity are destroyed"
    (is (empty?
         (reduce-polymer "aA")))
    (testing "destroy repeated"
      (is (empty?
           (reduce-polymer "abBA")))))
  (testing "nothing happens when adjacent units are of the same polarity"
    (is (= "aabAAB"
           (reduce-polymer "aabAAB"))))
  (testing "more complex example of reduction"
    (is (= "dabCBAcaDA"
           (reduce-polymer "dabAcCaCBAcCcaDA")))))

(deftest puzzle1-test
  (testing "real input"
    (is (= 9202
           (puzzle1)))))
