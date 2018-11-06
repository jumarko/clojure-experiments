(ns clojure-repl-experiments.skyline
  "Skyline problem - motivated by Riso's interview."
  (:require [clojure.spec.alpha :as s]))

;;; See https://www.geeksforgeeks.org/the-skyline-problem-using-divide-and-conquer-algorithm/

;; Array of buildings
(def input
  [[1,11,5], [2,6,7], [3,13,9], [12,7,16], [14,3,25],
   [19,18,22], [23,13,29], [24,4,28]])

;; A strip has x coordinate of left side and height 
(def expected-output
  [[1 11] [3 13] [9 0] [12 7] [16 3] [19 18] [22 3] [25 0]])


(s/def ::x-start int?)
(s/def ::x-end int?)
(s/def ::height int?)
(s/def ::building (s/tuple ::x-start ::x-end ::height))
(s/def ::buildings
  (s/coll-of ::building))

(s/def ::strip (s/tuple ::x-start ::height))
(s/def ::output (s/coll-of ::strip))

(s/fdef skyline
  :args (s/cat :buildings ::buildings)
  :ret ::output)
(defn skyline [buildings]
  (mapv (comp vec pop) buildings))


(comment

  (skyline input)

  )
