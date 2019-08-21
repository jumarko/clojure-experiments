(ns clojure-experiments.books.sicp.01-abstractions-procedures.s3-higher-order-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure-experiments.books.sicp.ch1-abstractions-procedures.s3-higher-order :as sut]
            [clojure-experiments.books.sicp.ch1-abstractions-procedures.exercise :as e]))

(deftest half-interval-method
  (testing "pi aproximation using sin"
    (is (= 3.14111328125
           (sut/half-interval-method e/sine 2.0 4.0))))
  (testing "root of the equation"
    (is (= 1.89306640625
           (sut/half-interval-method (fn [x] (- (* x x x)
                                                (* 2 x)
                                                3))
                                     1.0
                                     2.0)))))

(deftest fixed-point
  (testing "cosine fixed point approximation"
    (is (= 0.7390822985224024
           (sut/fixed-point #(Math/cos %) 1.0))))
  (testing "sin(x) + cos(x) approximation"
    (is (= 1.2587315962971173
           (sut/fixed-point #(+  (Math/sin %) (Math/cos %)) 1.0))))
  (testing "square root approximation"
    (is (= 2.000000000000002
           (sut/sqrt 4)))))

(deftest golden-ratio
  (testing "Golden ratio returns correct value"
    (is (= 1.6180
           (->> (sut/golden-ratio)
                double
                (format "%.4f")
                (Double/parseDouble))))))

(deftest inverse-golden-ration
  (testing "Ex. 1.37 using recursive continued fraction"
    (is (= 0.6180344478216819
           (double (sut/cont-frac-rec (constantly 1)
                                      (constantly 1)
                                      15)))))
  (testing "Ex. 1.37 using iterative continued fraction"
    (is (= 0.6180344478216819
           (double (sut/cont-frac-iter (constantly 1)
                                       (constantly 1)
                                       15))))))
(deftest euler-approximation
  (testing "Ex. 1.38"
    ;; This isn't very precise but I don't know why :(
    (is (= 2.717162485326501
           (sut/e-approximation 100)))))

(deftest newtons-method
  (testing "finds square root which is a whole number"
    (is (= 9.0
           (sut/sqrt-newton 81))))
  (testing "finds square root which is a real number"
    (is (= 1.4142135623822438
           (sut/sqrt-newton 2)))))

