(ns clojure-experiments.regex
  "Various experiments with regular expressions.
  See also https://clojure.org/reference/other_functions#regex")


;; re-find returns the first match within the string
(re-find #"abc" "abcdabc")
;; => "abc"

;; re-seq returns all the matches as a sequence
(re-seq #"abc" "abcdabc")
;; => ("abc" "abc")

;; with re-matches, the whole string must match the regex
(re-matches #"abc" "abcdabc")
;; => nil
(re-matches #"abc" "abc")
;; => "abc"

