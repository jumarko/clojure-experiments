(ns clojure-experiments.purely-functional.puzzles.0382-uniques
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-382-express-implicit-argument/"
  (:require [clojure.test :refer [are deftest]]))

(defn uniques
  "Returns only the unique values from the given seqeunce while preserving their order."
  [vals]
  (let [freqs (frequencies vals)]
    (filter (fn [val] (= 1 (freqs val)))
            vals)))

(deftest uniques-test
  (are [x y] (= x (uniques y))
    [4] [1 2 3 4 5 6 1 2 3 5 6]
    [:a :b] [:a :b :c :c]
    [] [1 2 3 1 2 3]))

