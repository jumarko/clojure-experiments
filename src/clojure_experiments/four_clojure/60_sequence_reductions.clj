(ns four-clojure.60-sequence-reductions
  "http://www.4clojure.com/problem/60
  Write reduce-like function which returns each intermediate value of the reduction.
  It must accept 2 or 3 args and return a lazy sequence."
  (:require [clojure.test :refer [deftest testing is]]))

;;; copied from `reductions` just removed calling `reduced?`
;;; then copied from pcl's solution: http://www.4clojure.com/problem/solutions/60
(defn my-reductions
  ([f coll]
   (my-reductions f (first coll) (rest coll)))
  ([f init coll]
   (cons init
         (lazy-seq
          (when-let [s (seq coll)]
            (my-reductions f (f init (first s)) (rest s)))))))

;; try if it's lazy
#_(take 5 (my-reductions + (range)))

(deftest official-tests
  (testing "lazy"
    (is (= [0 1 3 6 10]
           (take 5 (my-reductions + (range))))))
  (testing "init value"
    (is (= [[1] [1 2] [1 2 3] [1 2 3 4]]
           (my-reductions conj [1] [2 3 4]))))
  (testing "the result is same as from reduce"
    (is (= 120
           (reduce * 2 [3 4 5])
           (last (my-reductions * 2 [3 4 5]))))))

