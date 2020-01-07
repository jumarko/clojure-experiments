(ns clojure-experiments.purely-functional.puzzles.0359-duplicate-letters
  "See https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-359-tip-reduce-as-universal-recursion-over-a-list/"
  (:require [clojure.test :refer [deftest is testing]]))


(defn duplicate-letter? [s]
  (let [letter-freqs (frequencies s)]
    (->> letter-freqs
        vals
        ;; alternatively you can use `not-every?`
        #_(not-every? #(= % 1))
        ;; but `some` is more readable imho
        (some #(> % 1)))))

(deftest duplicate-letter-test
  (testing "contains duplicate letters"
    (is (duplicate-letter? "Hello, my friend!")))
  (testing "contains NO duplicate letters"
    (is (duplicate-letter? "Hey friend!")))
  )
