(ns clojure-experiments.purely-functional.property-based-testing.15-metamorphic-properties
  "https://purelyfunctional.tv/lesson/strategies-for-properties-metamorphic-properties/
  "
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clj-http.client :as http]
            [cheshire.core :as json]))


;;;; Sometimes you try to test a thing that doesn't have a correct answer
;;;; Example can be a search engine or AI system (e.g. vision system for object detection)
;;;; Basically anything, that has some kind of heuristic.
;;;; Even example-based tests would fail with those systems.
;;;; It's called "metamorphic" because you're modifying an input and expect modification in the output
;;;; (see wiki search example:      'adding negative query => less than or equal to results'
;;;;
;;;; Generic scheme:
;;;; - gen input
;;;; - run function
;;;; - modify input
;;;; - run function
;;;; - compare the two results


;;; Example: Wikipedia search

(defn search
  ([query]
   (search query 0))
  ([query offset]
   (let [res (-> "http://en.wikipedia.org/w/api.php"
                 (http/get {:query-params {:action "query"
                                           :format "json"
                                           :list "search"
                                           :srsearch query
                                           :sroffset offset}})
                 :body
                 ;; note: nice to be explicit with `keyword` instead of just `true`
                 (json/parse-string keyword)
                 #_(doto prn))
         hits (get-in res [:query :search])
         total-hits (get-in res [:query :searchinfo :totalhits])
         next-offset (get-in res [:continue :sroffset])]
     total-hits)))
(comment
  (search "Clojure")
  ;; => 311
;;
  )

;; one thing we can do is get search query results and compare them to the result of another queyr
(def fruit-terms ["fruit" "apple" "banana" "grape" "orange" "tree" "ripe" "sweet"])

(defspec wikipedia-search-not-operator 10
  (prop/for-all
   [pos-word (gen/elements fruit-terms)
    neg-word (gen/elements fruit-terms)]
   (let [pos-hits (search pos-word)
         neg-query (str pos-word " !" neg-word)
         neg-hits (search neg-query)]
     ;; now we can make an assertion about how the queries should be related
     ;; (at least, numver of positive hits should not be less than when searching for the same thing
     ;; but without )
     ;; => We don't need to worry about number of this themselves, just their relationship ("Metamorphic")
     ;;      adding negative query => less than or equal to results
     (println "Testing: " pos-hits neg-hits)
     (>= pos-hits neg-hits))))


;;; Example: Image detection
;;; Note: this won't compile
(comment
  (defspec fog-detection 100
    (prop/for-all 
     [image gen-image] ; pre-tending we already have `gen-image`
     (let [object (detect image)
           image-with-fog (add-fog image)
           object-with-fog (detect image-with-fog)]
       ;; this is to make sure our image detection system works under presence of fog
       (= object object-with-fog)))))


;;; Going back to wikipedia search example...
;;; We could use result of the "positive query" to modify the input of negative search query
;;; Example: google search
;;; - you do a query
;;; - you notice the domain name in the result
;;; - modify the search query by adding "site: <domain>" and check that result of such query
;;;   contains the result


;;; Another example: max
(defspec max-test 100
  (prop/for-all
   ;; if we didn't restrict the number of elements it will take a long time!
   ;; and also need to use `vector-distinct` to make sure we get at least two different numbers
   [numbers (gen/vector-distinct gen/small-integer {:min-elements 2 :max-elements 100})]
   (let [m (apply max numbers)
         ;; now I want to remove m from numbers (hard if it's not in the beginning of a list or end of a vector)
         ;; => use set but have to be careful to not generate same number twice in a two-element vector
         ;;    otherwise you'd get an exception because max cannot be applied to an empty coll of numbers
         numbers' (disj (set numbers) m)
         m' (apply max numbers')]
     (> m m'))))
