(ns clojure-experiments.four-clojure.085-power-set
  "http://www.4clojure.com/problem/85.
  Power set is a seb of all subsets of a set.
  Solutions: http://www.4clojure.com/problem/solutions/85"
  (:require [clojure.test :refer [are deftest is testing]]))

;;; Idea: number of all sets if 2^n use binary num representation to pick proper elements
;;; in every iteration
;;; See https://www.geeksforgeeks.org/finding-all-subsets-of-a-given-set-in-java/ for inspiration

(Integer/toBinaryString 15)
;; => "1111"

(defn power-set [s]
  (let [subsets-count (int (Math/pow 2 (count s)))
        ;; we want a stable sequential represention of set's elements to map them over binary 0 and 1s
        ;; and filter only "1s" easily
        ;; the idea is to generate binary numbers from 0 to (n - 1) (e.g. from 000 to 111)
        ;; and them for each of such binary numbers selects matching elements from the set
        subset-seq (seq s)]
    ;; this was an unsuccessful attempt to re-implement with `for`
    #_(for [n (range subsets-count)
            :let [binary-number (drop 1 (Integer/toBinaryString (+ n subsets-count)))]
            [binary-digit set-elem] (map vector binary-number subset-seq)
            :when (not= \0 binary-digit)]
        set-elem)
    (->> (range subsets-count)
         (map (fn to-full-binary [n]
                ;; we add `subsets-count` to be able to use `Integer/toBinaryString`
                ;; otherwise we'd just get '0' instead of '000' for 0, etc.
                (drop 1 (Integer/toBinaryString (+ n subsets-count)))))
         (map (fn to-subset [binary-number]
                (->> (map (fn [binary-digit set-elem] (when (= \1 binary-digit) set-elem))
                          binary-number
                          subset-seq)
                     (remove nil?)
                     set)
                ;; alternative
                #_(->> subset-seq
                       (map-indexed (fn [idx elem] (when (= \1 (nth binary-number idx)) elem)))
                       (remove nil?)
                       set)))
         set)))

;; for submission
(fn power-set [s]
  (let [subsets-count (int (Math/pow 2 (count s)))
        ;; we want a stable sequential represention of set's elements to map them over binary 0 and 1s
        ;; and filter only "1s" easily
        ;; the idea is to generate binary numbers from 0 to (n - 1) (e.g. from 000 to 111)
        ;; and them for each of such binary numbers selects matching elements from the set
        subset-seq (seq s)]
    (->> (range subsets-count)
         (map (fn to-full-binary [n]
                ;; we add `subsets-count` to be able to use `Integer/toBinaryString`
                ;; otherwise we'd just get '0' instead of '000' for 0, etc.
                (drop 1 (Integer/toBinaryString (+ n subsets-count)))))
         (map (fn to-subset [binary-number]
                (->> (map (fn [binary-digit set-elem] (when (= \1 binary-digit) set-elem))
                          binary-number
                          subset-seq)
                     (remove nil?)
                     set)))
         set)))

(power-set #{1 2})
;; => #{#{} #{2} #{1} #{1 2}}

(deftest power-set-test
  (testing "both x and empty set are included"
    (is (= #{#{} #{1}}
           (power-set #{1}))))
  (testing "all subsets"
    (is (= #{#{}
             #{1} #{2} #{3} #{4}
             #{1 2} #{2 3} #{1 3} #{1 4} #{2 4} #{3 4}
             #{1 2 3} #{1 2 4} #{1 3 4} #{2 3 4}
             #{1 2 3 4}}
           (power-set #{1 2 3 4})))))
