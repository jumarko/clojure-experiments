(ns clojure-experiments.flatland.useful
  "https://github.com/clj-commons/useful/tree/master/src/flatland/useful"
  (:require [flatland.useful.seq :refer :all]))

;;; flatland.useful.seq: https://github.com/clj-commons/useful/blob/master/src/flatland/useful/seq.clj

;; unfold: https://github.com/clj-commons/useful/blob/master/src/flatland/useful/seq.clj#L129
;; - useful for iterating over APIs?
;; - referenced here: https://clojure.atlassian.net/browse/CLJ-1906?focusedCommentId=48526
;; test-unfold: https://github.com/clj-commons/useful/blob/69bfcf3a1530bf4960bc9b36df2e73f80c0b6856/test/flatland/useful/seq_test.clj#L77
(take 10
      (unfold (fn [[a b]] [a [b (+ a b)]])
              [0 1]))
;; => (0 1 1 2 3 5 8 13 21 34)

