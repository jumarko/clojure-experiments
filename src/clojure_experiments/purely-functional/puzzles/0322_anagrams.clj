(ns clojure-experiments.purely-functional.puzzles.0322-anagrams
  "Two parts:
  - Write a function to determine if two words are anagrams.
  - Given a dictionary of words (such as this one: http://wiki.puzzlers.org/pub/wordlists/unixdict.txt),
    find all of the anagrams for a target word.
  Bonus points for efficiency."
  (:require [clojure.java.io :as io]))

;;; Save the dictionary for later processing
(def dictionary-file (io/file "src/clojure_experiments/purely-functional/puzzles/0322-dictionary.txt"))
(defn- save-dictionary
  "Helper function to save dictionary for fast offline access."
  []
  (spit dictionary-file
        (slurp "http://wiki.puzzlers.org/pub/wordlists/unixdict.txt" )))
#_(save-dictionary)

(def my-dictionary (->> dictionary-file
                     slurp
                     (clojure.string/split-lines)))

#_(take 10 my-dictionary)

(defn anagrams? [w1 w2]
  (and (= (count w1) (count w2))
       (= (set w1) (set w2))))

(anagrams? "ahoj" "helo")
;; => false
(anagrams? "helo" "ehlo")
;; => true
(anagrams? "helo" "hello")
;; => false

(defn dictionary-anagrams [word dictionary]
  (reduce
   (fn [anagrams candidate]
     (if (anagrams? word candidate)
       (conj anagrams candidate)
       anagrams))
   []
   dictionary))

(comment
  (dictionary-anagrams "helo" my-dictionary)
  ;; => [hole]

  (dictionary-anagrams "car" my-dictionary)
  ;; => ["arc" "car" "rca"]

  ;; end
  )
