(ns clojure-experiments.purely-functional.property-based-testing.11-functionality
  "One strategy to develop properties - functionality.
  This is the most intuitive strategy for people that already did unit testing."
  (:require
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.generators :as gen]
   [clojure.spec.alpha :as s]
   [clojure-experiments.purely-functional.property-based-testing.mergesort :refer [mergesort]]))

;;;; Testing functionality = ensuring it does what is is supposed together
;;;; Caveat: you can't reimplement the function under the test (e.g. implementing mergesort to test mergesort)

;;; BUT we can "compare with a model"
;;; -> e.g. comparing using a different algorithm (e.g. using built-in function)

(defspec sort-with-model 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
                (= (sort numbers)
                   (mergesort numbers))))

;;; If you don't have a "model" already built-in in your language:
;;; you can use simpler model (like hashmap used for testing database)

;; let's say I wanna test `distinct`
(distinct [1 2 3 3 3 2 0])

;; => I can use a set to make sure elements are distinct
(defspec distinct-with-model 100
  (prop/for-all [numbers (gen/vector (gen/choose 0 4))]
                (= (count (set numbers)) ; here's our model
                   (count (distinct numbers)))))

;;; However, sometimes you doesn't have a model (imagine you don't have sorting algorithm in your language)
;;; What do you do then??
;;; => THINK ABOUT THE MEANING! (what does it mean to be "sorted"?)

;; think about the meaning of "sorted"
(defspec first-element-smaller 100
  (prop/for-all
   [numbers (gen/not-empty (gen/vector gen/large-integer))]
   (let [s (mergesort numbers)
         f (first s)]
     (every? #(<= f %) numbers))))

;; Better to use stronger property of "being in order"
(defspec elements-in-order 100
  (prop/for-all
   [numbers (gen/not-empty (gen/vector gen/large-integer))]
   (let [s (mergesort numbers)]
     (apply <= s))))
