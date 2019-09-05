(ns clojure-experiments.four-clojure.085-power-set
  "http://www.4clojure.com/problem/85.
  Power set is a seb of all subsets of a set."
  (:require [clojure.test :refer [are deftest is testing]]))

;;; Idea: number of all sets if 2^n use binary num representation to pick proper elements
;;; in every iteration
;;; See https://www.geeksforgeeks.org/finding-all-subsets-of-a-given-set-in-java/ for inspiration

(Integer/toBinaryString 15)
;; => "1111"

(defn power-set [x]
  (let [subsets-count (int (Math/pow 2 (count x)))
        v (vec x)]
    (set (map
          (fn [n]
            (let [binary (drop 1 (Integer/toBinaryString n))]
              (->> v
                   (map-indexed
                    (fn [idx elem]
                      (when (= \1 (nth binary idx))
                        elem)))
                   (remove nil?)
                   set)))
          (range subsets-count (* subsets-count 2))))))

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
