(ns clojure-experiments.stats.commons-math
  "Examples using apache commons math library:
  Check https://generateme.github.io/fastmath/fastmath.stats.html too."
  (:import (org.apache.commons.math3.stat.inference TTest)))


;;; t-test using apache-commons math library: https://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/stat/inference/TTest.html
;;; https://en.wikipedia.org/wiki/Student%27s_t-test
;;; https://www.researchgate.net/post/Is_it_rational_to_use_students_t-test_for_larger_sample
;;; See also Java Performance: The Definitive Guide - chapter 2

;;; https://commons.apache.org/proper/commons-math/javadocs/api-3.6/org/apache/commons/math3/stat/inference/TTest.html#tTest(double[],%20double[])
(defn t-test [sample1 sample2]
  (-> (TTest.)
      (.tTest (double-array sample1) ;; avg 1.0 second
              (double-array sample2))))

(t-test
 [1.0 0.8 1.2]
 [0.5 1.25 0.5])
 ;; avg 0.75 second
;; => 0.43483185200434427 (p-value)
;; => 57% of time the performance of the second observation (specimen) is 25% better (0.75 seconds vs 1. seccond)


