(ns clojure-experiments.four-clojure.098-equivalence-classes
  "http://www.4clojure.com/problem/98
  A function f on a domain D is 'equivalent' to b with resepect to f iff (f a) = (f b).
  Your function should compute equivalence classes for f and D: https://en.wikipedia.org/wiki/Equivalence_class
  "
  (:require [clojure.test :refer [deftest testing is]]))


;; pretty easy, pretty much everyone else did it the same way!
;; http://www.4clojure.com/problem/solutions/98
(defn equivalence-classes
  [f D]
  (->> D
       (group-by f)
       vals
       (map set)
       set))

(deftest test-equiv
  (testing "square function with sample data"
    (is (= #{ #{0} #{-1 1} #{-2 2}}
           (equivalence-classes #(* % %) #{-2 -1 0 1 2}))))
  (testing "testing `rem`"
    (is (= #{#{0 3} #{1 4} #{2 5}}
           (equivalence-classes #(rem % 3) #{0 1 2 3 4 5 }))))
  (testing "testing `identity`"
    (is (= #{#{0} #{1} #{2} #{3} #{4}}
           (equivalence-classes identity #{0 1 2 3 4}))))
  (testing "testing `constantly`"
    (is (= #{#{0 1 2 3 4}}
           (equivalence-classes (constantly true) #{0 1 2 3 4}))))
  
  )

