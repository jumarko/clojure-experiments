(ns clojure-experiments.purely-functional.puzzles.0359-duplicate-letters
  "See https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-359-tip-reduce-as-universal-recursion-over-a-list/"
  (:require [clojure.test :refer [deftest is testing]]))


;;; Unrelated
;; this is an interesting alternative by using `reduce` to define `map`
;; see https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-359-tip-reduce-as-universal-recursion-over-a-list/
(defn rmap [f coll]
  (reduce
   (fn [x y]
     (conj x (f y)))
   []
   coll))
(rmap inc [1 2 3])
;; => [2 3 4]

;;; Quiz
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
