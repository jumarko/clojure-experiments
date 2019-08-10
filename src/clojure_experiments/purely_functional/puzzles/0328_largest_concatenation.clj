(ns clojure-experiments.purely-functional.puzzles.0328-largest-concatenation
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-328-tip-don-t-use-def-inside-a-defn/
  Tests: https://github.com/lispcast/maxcat/blob/master/test/maxcat/core_test.clj"
  (:require [clojure-experiments.purely-functional.puzzles.util :as u]))

(defn- prefix>
  "Compares two integers in a similar way as `clojure.core/>` does
  but does the comparison digit-by-digit; that is:
  - if first digit in x1 is greater than the first digit in x2 then return true, otherwise false,
    unless the digits are equal in which case continue as follows
  - if second digit in x2 is greater than the second digit in x2 then return true, ...
  - ... and so on"
  [x1 x2]
  (let [x1-digits (u/digits x1)
        x2-digits (u/digits x2)
        digits> (map #(- %1 %2) x1-digits x2-digits)]
    ;; ???
    ))

(prefix> 192 224)

(defn maxcat [integers]
  (sort > integers))

(maxcat [12 34 56 199])
