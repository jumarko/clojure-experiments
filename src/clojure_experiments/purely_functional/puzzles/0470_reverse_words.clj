(ns clojure-experiments.purely-functional.puzzles.0470-reverse-words
  "https://ericnormand.me/issues/470
  Solutions: https://gist.github.com/ericnormand/66a3ddaafc7c61dcb0a857aa88239142"
  (:require [clojure.string :as str]))

(defn reverse-words [words]
  (->> (str/split words #"\s+")
       reverse ; possible to use `rseq` too
       (str/join " ")))


(assert (= "you. love I"
           (reverse-words "I love you.")))
(assert (= "hello"
           (reverse-words "hello")))
