(ns clojure-experiments.purely-functional.puzzles.0363-moving-average
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-363-learn-to-build-and-deploy-a-single-page-application/"
  (:require [clojure.test :refer [deftest is testing]]))

;;; https://en.wikipedia.org/wiki/Moving_average
;;; Very often, your dataset represents real measurements over time.
;;; There’s a clear trend in the data, but it’s muddied by the noise in each data point.
;;; One way to focus on the trend is to perform a moving average.
;;; Each data point is the average of the n points around it (the window).
;;; 
;;; Your task is to write a function that takes a sequence of numbers and
;;; returns a sequence of weighted averages.
;;; Make n (the window size) an argument.

;; interesting discussion might be whether `n` should be the first or second arg
;; I decided to make it the first one since it seems to me that one could use this
;; with partial application more easily.
(defn moving-avg
  [n xs]
  (let [windows (partition n 1 xs)
        ]
    windows))

(moving-avg 3 [4 3 7])

(moving-avg 3 [4 3 7 10 2 -1])
;; => ((4 3 7) (3 7 10) (7 10 2) (10 2 -1))

(reduce
 (fn [[prev-window-first-num prev-window-avg] [fst :as current-window]]
   [fst (/ 3)])
 '((4 3 7) (3 7 10) (7 10 2) (10 2 -1)))

(deftest moving-avg-test
  (testing "single average"
    (is (= 14/3
           (moving-avg 3 [4 3 7]))))
  (testing "two averages"
    (is (= 20/3
           (moving-avg 3 [4 3 7 10]))))
  (testing "all averages"
    (is (= 140/3
           (moving-avg 3 [4 3 7 10 2 -1]))))
  )
