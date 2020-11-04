(ns clojure-experiments.purely-functional.puzzles.0402-most-frequent
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-402-up-front-vs-incremental-design/.
  Find the most frequent element in the collection but you cannot use `frequencies`.
  Solutions here: https://gist.github.com/ericnormand/7944c8806ba447a7bee6301a168ecdcb"
  (:require [clojure.test :refer [deftest is testing]]))

(defn most-frequent [coll]
  (when-not (empty? coll)
    (let [by-number (group-by identity coll)
          [num _occurences] (apply max-key (comp count val) by-number)]
      num)))

(deftest test-most-frequent
  (testing "empty collection"
    (is (nil? (most-frequent []))))
  (testing "non-empty collection"
    (is (= 2 (most-frequent [2 2 3 4 4 2 1 1 3 2])))
    (is (= 4 (most-frequent [1 1 4 4 5])))))
