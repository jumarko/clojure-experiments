(ns clojure-experiments.four-clojure.084-transitive-closure
  "Write a function that computes the transitive clojure of a binary relation.
  http://www.4clojure.com/problem/84"
  (:require [clojure.test :refer [are deftest is testing]]))

(defn transitive-closure [binary-rel]
  (let [trans-clos-elem (fn tce [acc [x y]]
                          (or (some-> (filter (fn [[xx _yy]] (= x xx))
                                              binary-rel)
                                      first
                                      (recur (conj ))))
                          )]))


(for [x  #{[8 4] [9 3] [4 2] [27 9]}
      y #{[8 4] [9 3] [4 2] [27 9]}]
  [x y])
(transitive-closure #{[8 4] [9 3] [4 2] [27 9]})
;;=> #{[4 2] [8 4] [8 2] [9 3] [27 9] [27 3]}


(deftest transitive-closure-test
  (testing "divides"
    (is (= #{[4 2] [8 4] [8 2] [9 3] [27 9] [27 3]}
           (transitive-closure #{[8 4] [9 3] [4 2] [27 9]}))))
  (testing "more-legs"
    (is (= #{["cat" "man"] ["cat" "snake"] ["man" "snake"]
             ["spider" "cat"] ["spider" "man"] ["spider" "snake"]}
           (transitive-closure #{["cat" "man"] ["man" "snake"] ["spider" "cat"]}))))
  (testing "progeny"
    (is (= #{[4 2] [8 4] [8 2] [9 3] [27 9] [27 3]}
           (transitive-closure #{["father" "son"] ["uncle" "cousin"] ["son" "grandson"]}))))
  )
