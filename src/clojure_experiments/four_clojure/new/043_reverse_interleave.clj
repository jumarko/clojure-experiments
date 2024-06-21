(ns clojure-experiments.four-clojure.new.043-reverse-interleave
  "https://4clojure.oxal.org/#/problem/43"
  (:require [clojure.test :refer [deftest are testing]]))

(defn reverse-interleave
  "Splits the sequence into `n` subsequences,
  each sub-sequence containing every `n`-th element,
  starting from zero."
  [xs n]
  (apply map vector (partition n xs)))

(deftest reverse-interleave-test []
  (are [xs n res] (= res (reverse-interleave xs n))
    [1 2 3 4 5 6] 2 [[1 3 5] [2 4 6]]
    (range 9) 3 [[0 3 6] [1 4 7] [2 5 8]]
    (range 10) 5 [[0 5] [1 6] [2 7] [3 8] [4 9]]))
