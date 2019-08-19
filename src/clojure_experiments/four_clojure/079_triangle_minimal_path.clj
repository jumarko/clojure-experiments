(ns four-clojure.079-triangle-minimal-path
  "http://www.4clojure.com/problem/79.
  Computes the minimal path through the triangle represented as a collection of vectors."
  (:require [clojure.test :refer [deftest testing is]]))

(defn grow-row [row n]
  (let [inners (-> row rest butlast)
        inners-grown (mapcat (fn [x] (repeat (bit-shift-left 1 n) x))
         inners)]
    (concat (first grow) inners-gronw (last grow))
    ))
(grow-row [1 3 5] 2)

(defn all-paths [triangle]
  (->> triangle
       ;; reverse to be able to use row indices directly
       reverse
       (map-indexed (fn [row-num row]
                      (grow-row row row-num)))))

(all-paths '([1]
            [2 4]
           [5 1 4]
          [2 3 4 5]))

(defn min-path
  [triangle]
  (let [paths (all-paths triangle)]
    (prn "DEBUG:: " paths)
    (->> paths
         (apply min-key #(apply + %))
         (apply +))))

(deftest official-samples
  (testing "simple"
    (is (= 7
           (min-path '([1]
                      [2 4]
                     [5 1 4]
                    [2 3 4 5])))))
  (testing "complex"
    (is (= 20
           (min-path '([3]
                      [2 4]
                     [1 9 3]
                    [9 9 2 4]
                   [4 6 6 7 8]
                  [5 7 3 5 1 4]))))))

'([1] [2 4] [5 1 4])
