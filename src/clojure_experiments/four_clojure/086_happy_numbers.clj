(ns clojure-experiments.four-clojure.086-happy-numbers
  "http://www.4clojure.com/problem/86.
  Write a function that determines whether given number is happy.
  Is this a 'halting problem'?"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure-experiments.purely-functional.puzzles.util :refer [digits]]))

;;; TODO: after how many iterations are we going to give up?
;;; - if we get to the same number we can give up
;;; - anything else??
;;; `digits` copied from `clojure-experiments.purely-functional.puzzles.util`
(defn happy-number? [x]
  
  (let [digits (fn [y] (map #(- (int %) (int \0)) (str y)))
        digits-squares-sum (fn [y] (->> (digits y)
                                        (map #(* % %))
                                        (apply +)))
        ;; after how many tries we're going to decide it's not worth trying anymore
        threshold 100]
    (->> (iterate digits-squares-sum x)
         (drop threshold)
         first
         (= 1))))

(deftest happy-numbers
  (testing "happy numbers"
    (is (happy-number? 7))
    (is (happy-number? 986543210)))
  (testing "sad numbers"
    (is (not (happy-number? 2)))
    (is (not (happy-number? 3)))))
