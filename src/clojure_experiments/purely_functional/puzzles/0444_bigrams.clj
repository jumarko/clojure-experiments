(ns clojure-experiments.purely-functional.puzzles.0444-bigrams
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-444-humane-models-of-errors/

  Bigram is a sequence of two letters.

  Solutions: https://gist.github.com/ericnormand/724c98a2ff399c82469ce2dd1ea3e23c"
  (:require [clojure.string :as str]))

(defn- all-present? [bigrams string]
  (let [words (str/split string #"\s")]
    (every? (fn [bigram]
              (some #(str/includes? % bigram) words))
            bigrams)))

;; in fact, we can eliminate splitting to words completely?
;; - it doesn't match bigrams crossing words because there's always a space between words
(defn- all-present?-2 [bigrams string]
  (every? #(str/includes? string %) bigrams))

(all-present? ["st" "tr"] "street") ;=> true
(all-present? ["ea" "ng" "kt"] "eating at a restaurant") ;=> false
(all-present? ["ea" "ng" "ar"] "eating at a restaurant") ;=> false
(all-present? [] "hello!") ;=> true
