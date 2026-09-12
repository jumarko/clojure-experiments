(ns clojure-experiments.books.living-clojure.training.week3
  (:require [clojure.string :as str]))

;;; Day 1: binary tree predicate
(defn binary-tree?
  "Returns true if given data structure represents a binary tree,
  that is each node is either a simple value or a sequence of 3 items."
  [btree]
  (if (= 3 (count btree))
    (every? (fn [x]
              (if (coll? x)
                (binary-tree? x)
                ;; assuming simple value
                true))
            btree)
    false))

(binary-tree? [])
;; => false
(binary-tree? [:a :b :c])
;; => true
(binary-tree? [:a [:b 1 100] :c])
;; => true
(binary-tree? [:a [:b 1] :c])
;; => false


;;; Day 5: Anagrams

(defn anagrams
  "Returns a set of sets, where each set is a collection of anagrams.

  Idea: If I sort the words that are anagrams I get the same thing"
  [xs]
  (let [canonical (fn [w] (sort (str/lower-case w)))]
    (->> xs
         (group-by canonical)
         vals
         (map set)
         (filter #(< 1 (count %)))
         (into #{}))))


(anagrams ["meat" "mat" "team" "mate" "eat"])
;; => #{#{"meat" "mate" "team"}}
(anagrams ["veer" "lake" "item" "kale" "mite" "ever"])
;; => #{#{"kale" "lake"} #{"item" "mite"} #{"ever" "veer"}}
