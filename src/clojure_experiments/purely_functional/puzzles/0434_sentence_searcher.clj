(ns clojure-experiments.purely-functional.puzzles.0434-sentence-searcher
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-434-re-combination-of-parts/
  Solutions: https://gist.github.com/ericnormand/25e53eea786708b948d0c666c790580b"
  (:require [clojure.string :as str]))


(defn sentences [document]
  (mapv str/trim
        (str/split document #"[.?!]")))

(defn search [document word]
  ;; TODO: take word boundaries into account - e.g. 'no' shouldn't match 'not'
  (filterv #(str/includes? (str/lower-case %) (str/lower-case word))
           (sentences document)))

(search "I like to write. Do you like to write?" "like")
;; => ["I like to write" "Do you like to write"]

(search "This is my document. It has two sentences." "sentences")
;; => ["It has two sentences"]
