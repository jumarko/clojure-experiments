(ns advent-of-clojure.2018.06-chronal-coordinates
  "https://adventofcode.com/2018/day/6"
  (:require
   [clojure.test :refer [deftest testing is]]
   [advent-of-clojure.2018.input :as io]))

;;;; You want to find the point that is farthest from all the others
;;;; using Manhattan (block) distance

(defn largest-area [points]
  )

(defn puzzle1 []
  (io/with-input "06_input.txt" largest-area))


(deftest largest-area-test
  (testing "two points"
    (is (= 4
           )))
  )

(deftest puzzle1-test
  (testing "real input"
    (is (= 0
           (puzzle1)))))
