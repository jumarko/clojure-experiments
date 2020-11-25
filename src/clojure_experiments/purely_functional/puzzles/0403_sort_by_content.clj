(ns clojure-experiments.purely-functional.puzzles.0403-sort-by-content
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-403-the-timeless-in-an-unstable-domain/
  Solutions: https://gist.github.com/ericnormand/48b0a933294639bb27902eda062772e2"
  (:require [clojure.test :refer [deftest is testing]]))

(defn to-vector [x] (if (vector? x) x (vector x)))
(defn sort-by-content [x]
  (sort
   (fn [x y] (let [xv (to-vector x) yv (to-vector y)
                   xs (count xv) ys (count yv)
                   ;; this is incorrect - see the last test
                   subvec-for-comp (fn [v] (subvec (if (vector? v) v (vector v))
                                                   0
                                                   (min xs ys)))
                   ;;... thus we use `pad` fn instead
                   pad (fn [v] (vec (take (max xs ys) (concat v (repeat nil)))))]
               (if (and (= 1 xs ys)
                        (= (first xv) (first yv)))
                 (cond
                   (number? x) -1
                   (number? y) 1
                   :else 0)
                 ;; can't use subvec-for-comp here - see the last test
                 (compare (pad xv) (pad yv)))))
   x))


(deftest sort-by-content-test
  (testing "sort numbers"
    (is (= [1 2 3 4 5]
           (sort-by-content [4 5 3 2 1]))))
  (testing "sort collections"
    (is (= [[-1 3 4] [0 3] [0 9] [2 3]]
           (sort-by-content [[2 3] [0 9] [-1 3 4] [0 3]]))))
  (testing "sort collections and numbers"
    (is (= [[0 2 3] 1 3 [4 5] 5]
           (sort-by-content [5 [4 5] 3 1 [0 2 3]]))))
  (testing "numbers have higher priority than collections"
    (is (= [1 [1]]
           (sort-by-content [[1] 1]))))
  (testing "tricky test: https://gist.github.com/ericnormand/48b0a933294639bb27902eda062772e2#gistcomment-3531059"
    (is (= (sort-by-content [[1 2] [1]])
           (sort-by-content [[1] [1 2]])))))

