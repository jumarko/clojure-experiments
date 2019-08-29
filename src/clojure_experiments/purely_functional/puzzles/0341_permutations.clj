(ns clojure-experiments.purely-functional.puzzles.0341-permutations
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-341-tip-tidy-up-your-ns-form/
  Write a function that returns all possible permutations of given sequence.
  Your function should return lazy sequence.
  See also `clojure.math.combinatorics/permutations`."
  (:require [clojure.test :refer [deftest is testing]]))

(defn permutations [aseq]
  #_(println "Computating permutation for " aseq)
  (cond
    (empty? aseq)
    []

    (= 1 (count aseq))
    [aseq]

    :else
    (for [x aseq
          xs (permutations (disj (set aseq)
                                 x))]
      (cons x xs))))

(take 2 (permutations [1 2 3]))
;; => ((1 3 2) (1 2 3) (2 1 3) (2 3 1) (3 1 2) (3 2 1))


(deftest permutations-test
  (testing "empty sequence"
    (is (= []
           (permutations []))))
  (testing "sequence of one"
    (is (= [[1]]
           (permutations [1]))))
  (testing "3-elements sequence have all 6 permutations"
    (let [permutations (permutations [2 1 3])]
      (is (= #{[1 2 3] [1 3 2]
               [2 1 3] [2 3 1]
               [3 1 2] [3 2 1]}
             (set permutations)))))
  (testing "many elements sequence have n! permutations"
      (let [permutations (permutations [1 2 3 4 5 10 9 8 7 6])]
        (is (= 3628800
               (count permutations))))))

