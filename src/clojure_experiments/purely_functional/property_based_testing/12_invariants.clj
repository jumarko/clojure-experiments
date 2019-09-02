(ns clojure-experiments.purely-functional.property-based-testing.12-invariants
  "One strategy to develop properties - invariants:
     https://purelyfunctional.tv/lesson/strategies-for-properties-invariants/
  This is the most intuitive strategy for people that already did unit testing."
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.generators :as gen]
   [clojure.spec.alpha :as s]
   [clojure-experiments.purely-functional.property-based-testing.mergesort :refer [mergesort]]))


;;;; Testing invariants (= basically things that won't change regardless of the input)

;;; What doesn't change?

;; always return list
(defspec sort-always-list 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
                (seq? (mergesort numbers))))

;; lenght of output should be the same as input
(defspec sort-same-length 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
                (= (count (mergesort numbers))
                   (count numbers))))

;; you can't change the elements!
;; ... we could use a set but that doesn't take care of duplicates...
;; => we could use `frequencies`
(defspec sort-same-elements 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
                (= (frequencies numbers)
                   (frequencies (mergesort numbers)))))
