(ns four-clojure.070-word-sorting
  "Write a function that splits a sentence up into a sorted list of words.
  Capitalization should not affect sort order and punctuation should be ignored.
  See http://www.4clojure.com/problem/70")

(defn sorted-words [sentence]
  (->> (clojure.string/split sentence #"[^A-Za-z]+")
       (sort-by clojure.string/lower-case)))

(sorted-words "Have a nice day.")

(sorted-words "Clojure is a fun language!")

(sorted-words "Fools fall for foolish follies.")
