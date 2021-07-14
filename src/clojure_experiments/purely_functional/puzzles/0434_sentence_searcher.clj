(ns clojure-experiments.purely-functional.puzzles.0434-sentence-searcher
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-434-re-combination-of-parts/
  Solutions: https://gist.github.com/ericnormand/25e53eea786708b948d0c666c790580b"
  (:require [clojure.string :as str]))


(defn sentences [document]
  (mapv str/trim
        (str/split document #"[.?!]")))

(defn contains-word? [sentence word]
  (let [sentence-words (str/split sentence #"\s")]
    ((set sentence-words) word)))

(defn search [document word]
  (not-empty (filterv #(contains-word? % word)
                      (sentences document))))

(search "This is my document." "Hello")
;; => []

(search "I like to write. Do you like to write?" "like")
;; => ["I like to write" "Do you like to write"]

(search "This is not my document. It has no two sentences." "no")
;; => ["It has no two sentences"]
