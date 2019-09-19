(ns clojure-experiments.purely-functional.property-based-testing.17-shrinkage
  "https://purelyfunctional.tv/lesson/behind-the-scenes-shrinkage/"
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.test.check :as tc]
            [clojure.string :as str]))

;;;; Exploring shrinking process in test.check
;;;; Note, ultimately, it's the `clojure.test.check/shrink-loop` function that deals with shrinking
;;;; using RoseTree (which is returned from generators?)
;;;;
;;;; Moreover, typically you don't need to think about shrinking much (you just trust generators)

(defn report-shrinkage [res]
  ;; we can be sure that shrinking happens only after a failure
  (when (= :shrink-step (:type res))
    (prn (get-in res [:shrinking :args])
         (get-in res [:shrinking :smallest]))))

(defn check-with-shrinkage-report [property]
  (tc/quick-check
   100
   property
   :reporter-fn report-shrinkage))

(check-with-shrinkage-report
 (prop/for-all [v (gen/vector gen/nat)]
               (not= 7 (count v))))

;;=> 
[[]] [[6 5 3 1 2 1 3]] ; just try empty
[[6 5 3]] [[6 5 3 1 2 1 3]] ; try first half
[[1 2 1 3]] [[6 5 3 1 2 1 3]] ; try second half
;; try dropping one element
[[5 3 1 2 1 3]] [[6 5 3 1 2 1 3]] 
[[6 3 1 2 1 3]] [[6 5 3 1 2 1 3]]
[[6 5 1 2 1 3]] [[6 5 3 1 2 1 3]]
[[6 5 3 2 1 3]] [[6 5 3 1 2 1 3]]
[[6 5 3 1 1 3]] [[6 5 3 1 2 1 3]]
[[6 5 3 1 2 3]] [[6 5 3 1 2 1 3]]
[[6 5 3 1 2 1]] [[6 5 3 1 2 1 3]]
;; then it trie to zero the first element and it failed!
;; => [0 5 3 1 2 1 3] becomes the new "smallest"
[[0 5 3 1 2 1 3]] [[0 5 3 1 2 1 3]]
[[5 3 1 2 1 3]] [[0 5 3 1 2 1 3]]
[[0 3 1 2 1 3]] [[0 5 3 1 2 1 3]]
[[0 5 1 2 1 3]] [[0 5 3 1 2 1 3]]
[[0 5 3 2 1 3]] [[0 5 3 1 2 1 3]]
[[0 5 3 1 1 3]] [[0 5 3 1 2 1 3]]
[[0 5 3 1 2 3]] [[0 5 3 1 2 1 3]]
[[0 5 3 1 2 1]] [[0 5 3 1 2 1 3]]
;; again trying to "zero" an element (the second one)
[[0 0 3 1 2 1 3]] [[0 0 3 1 2 1 3]]
[[0 3 1 2 1 3]] [[0 0 3 1 2 1 3]]
[[0 3 1 2 1 3]] [[0 0 3 1 2 1 3]]
[[0 0 1 2 1 3]] [[0 0 3 1 2 1 3]]
[[0 0 3 2 1 3]] [[0 0 3 1 2 1 3]]
[[0 0 3 1 1 3]] [[0 0 3 1 2 1 3]]
[[0 0 3 1 2 3]] [[0 0 3 1 2 1 3]]
[[0 0 3 1 2 1]] [[0 0 3 1 2 1 3]]
[[0 0 0 1 2 1 3]] [[0 0 0 1 2 1 3]]
[[0 0 1 2 1 3]] [[0 0 0 1 2 1 3]]
[[0 0 1 2 1 3]] [[0 0 0 1 2 1 3]]
[[0 0 1 2 1 3]] [[0 0 0 1 2 1 3]]
[[0 0 0 2 1 3]] [[0 0 0 1 2 1 3]]
[[0 0 0 1 1 3]] [[0 0 0 1 2 1 3]]
[[0 0 0 1 2 3]] [[0 0 0 1 2 1 3]]
[[0 0 0 1 2 1]] [[0 0 0 1 2 1 3]]
[[0 0 0 0 2 1 3]] [[0 0 0 0 2 1 3]]
[[0 0 0 2 1 3]] [[0 0 0 0 2 1 3]]
[[0 0 0 2 1 3]] [[0 0 0 0 2 1 3]]
[[0 0 0 2 1 3]] [[0 0 0 0 2 1 3]]
[[0 0 0 2 1 3]] [[0 0 0 0 2 1 3]]
[[0 0 0 0 1 3]] [[0 0 0 0 2 1 3]]
[[0 0 0 0 2 3]] [[0 0 0 0 2 1 3]]
[[0 0 0 0 2 1]] [[0 0 0 0 2 1 3]]
[[0 0 0 0 0 1 3]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 1 3]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 1 3]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 1 3]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 1 3]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 1 3]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 0 3]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 0 1]] [[0 0 0 0 0 1 3]]
[[0 0 0 0 0 0 3]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 3]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 3]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 3]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 3]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 3]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 3]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 3]]
[[0 0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]
[[0 0 0 0 0 0]] [[0 0 0 0 0 0 0]]

;;; Now try another operation 
(check-with-shrinkage-report
 (prop/for-all [n gen/nat]
               (not (zero? (mod (inc n) 13)))))

[0] [12]
[6] [12]
[9] [12]
[11] [12]


;;; Yet another - fail when string contains a colon:
(check-with-shrinkage-report
 (prop/for-all [s gen/string-ascii]
               (not (str/index-of s ":"))))
[""] ["gaf?:"]
["ga"] ["gaf?:"]
["f?:"] ["f?:"]
["?:"] ["?:"]
[":"] [":"]
[""] [":"]
[","] [":"]
["3"] [":"]
["7"] [":"]
["9"] [":"]


;;; Now let's try UUIDs
;;; we won't see any shrinking of UUIDs themselves
(check-with-shrinkage-report
 (prop/for-all [v (gen/vector gen/uuid)]
               (not= 3 (count v))))
[[]] [[#uuid "0599c18e-3298-49bf-a43f-e0dcffc2ba4e" #uuid "ffb922b9-9b9d-4525-804e-2b9502968c56" #uuid "da08dcbd-095c-476d-a82d-75ae14f757e8"]]
[[#uuid "ffb922b9-9b9d-4525-804e-2b9502968c56" #uuid "da08dcbd-095c-476d-a82d-75ae14f757e8"]] [[#uuid "0599c18e-3298-49bf-a43f-e0dcffc2ba4e" #uuid "ffb922b9-9b9d-4525-804e-2b9502968c56" #uuid "da08dcbd-095c-476d-a82d-75ae14f757e8"]]
[[#uuid "0599c18e-3298-49bf-a43f-e0dcffc2ba4e" #uuid "da08dcbd-095c-476d-a82d-75ae14f757e8"]] [[#uuid "0599c18e-3298-49bf-a43f-e0dcffc2ba4e" #uuid "ffb922b9-9b9d-4525-804e-2b9502968c56" #uuid "da08dcbd-095c-476d-a82d-75ae14f757e8"]]
[[#uuid "0599c18e-3298-49bf-a43f-e0dcffc2ba4e" #uuid "ffb922b9-9b9d-4525-804e-2b9502968c56"]] [[#uuid "0599c18e-3298-49bf-a43f-e0dcffc2ba4e" #uuid "ffb922b9-9b9d-4525-804e-2b9502968c56" #uuid "da08dcbd-095c-476d-a82d-75ae14f757e8"]]


;;; And try gen/elements;
;;; notice that it "shrinks to left"  (in our case all the values shrink to :a)
(check-with-shrinkage-report
 (prop/for-all [v (gen/vector (gen/elements [:a :b :c :d]))]
               (not= 3 (count v))))
[[]] [[:c :b :b]]
[[:b :b]] [[:c :b :b]]
[[:c :b]] [[:c :b :b]]
[[:c :b]] [[:c :b :b]]
;; shrinking to two element vector didn't work -> try changing an element
[[:a :b :b]] [[:a :b :b]]
[[:b :b]] [[:a :b :b]]
[[:a :b]] [[:a :b :b]]
[[:a :b]] [[:a :b :b]]
[[:a :a :b]] [[:a :a :b]]
[[:a :b]] [[:a :a :b]]
[[:a :b]] [[:a :a :b]]
[[:a :a]] [[:a :a :b]]
[[:a :a :a]] [[:a :a :a]]
[[:a :a]] [[:a :a :a]]
[[:a :a]] [[:a :a :a]]
[[:a :a]] [[:a :a :a]]

;;; similarly, gen/one-of shrinks toward choosing an earlier generator


;;; When it comes to generators, we have basically two options:
;;; - gen/fmap 
;;; - gen/bind (gen/let)
;;; => ***prefer gen/fmap because gen/bind will prevent the value from shrinking properly***
(check-with-shrinkage-report
 (prop/for-all [v (gen/let [size gen/nat]
                    (gen/vector gen/nat size))]
               (not= 3 (count v))))
[[]] [[11 11 6]]
[[11 7]] [[11 11 6]]
[[0 11 6]] [[0 11 6]]
[[0 0 6]] [[0 0 6]]
[[0 0 0]] [[0 0 0]]
[[0 0 3]] [[0 0 3]]
[[0 0 0]] [[0 0 0]]
[[0 0 2]] [[0 0 2]]
[[0 0 0]] [[0 0 0]]
[[0 0 1]] [[0 0 1]]
;; manage to get to zero zero zero but that's not always possible
[[0 0 0]] [[0 0 0]]


(check-with-shrinkage-report
 (prop/for-all [v (gen/let [size gen/nat]
                    (gen/vector gen/nat size))]
               (not (contains? (set v) 17))))
;; it cannot shrink it by systematically removing values from the original failed input
;; because the only way to shrink the vector is to change the `size` and that means
;; the new generator is returned by inner gen/vector and thus lost the original failed input
[[]] [[11 17 17 19 16 14 2 19 18 9 19 6 0 19 16 8 16 11]]
[[11 17 17 19 16 14 2 19 9]] [[11 17 17 19 16 14 2 19 9]]
[[]] [[11 17 17 19 16 14 2 19 9]]
[[11 17 17 19 3]] [[11 17 17 19 3]]
[[]] [[11 17 17 19 3]]
[[11 17 13]] [[11 17 13]]
[[]] [[11 17 13]]
[[11 21]] [[11 17 13]]
[[0 17 13]] [[0 17 13]]
[[0 0 13]] [[0 17 13]]
[[0 9 13]] [[0 17 13]]
[[0 13 13]] [[0 17 13]]
[[0 15 13]] [[0 17 13]]
[[0 16 13]] [[0 17 13]]
[[0 17 0]] [[0 17 0]]
[[0 0 0]] [[0 17 0]]
[[0 9 0]] [[0 17 0]]
[[0 13 0]] [[0 17 0]]
[[0 15 0]] [[0 17 0]]
[[0 16 0]] [[0 17 0]]


;; now try the same with fmap
;; although we could replace it with just `(gen/vector gen/nat)`
(check-with-shrinkage-report
 (prop/for-all [v (gen/fmap (fn [[size v]]
                              (subvec v 0 size))
                            (gen/tuple gen/nat
                                       (gen/vector gen/nat 1000)))]
               (not (contains? (set v) 17))))
