(ns clojure-experiments.purely-functional.puzzles.0463-word-search
  "https://ericnormand.me/issues/purelyfunctional-tv-newsletter-463-what-is-beautiful-code
  Solutions: https://gist.github.com/ericnormand/6eda605a72b169d62bde12a740eb5bb9"
  (:require [clojure.string :as str]))


;; I didn't get very far...
(defn found? 
  "Returns true if the needel is found in they haystack with a few forgiving features:
  - if some letters are missing in the needle still returns true
  - but don't match across whitespaces"
  [needle haystack]
  (let [words (str/split haystack #"\s+")]
    words))

(found? "abc" "aaabc abc")


;; This is a really nice solution!
;; https://gist.github.com/ericnormand/6eda605a72b169d62bde12a740eb5bb9?permalink_comment_id=4083589#gistcomment-4083589
(defn found? [needle haystack]
  (let [needle-pattern (re-pattern (str/join "\\p{Alnum}*" needle))]
    (boolean (re-find needle-pattern haystack))))

(found? "abc" "dddabcfff") ;=> true (direct match)
;; => true
(found? "abc" "xyzannbffooc") ;=> true (add missing "nn" and "ffoo")
;; => true
(found? "abc" "a bc") ;=> false (don't match across whitespace)
;; => false
(found? "xxx" "cxccx") ;=> false (not enough x's)
;; => false
(found? "" "") ;=> true (trivially so)
;; => true
(found? "" "aaa") ;=> true (also trivial);; => 
;; => true

