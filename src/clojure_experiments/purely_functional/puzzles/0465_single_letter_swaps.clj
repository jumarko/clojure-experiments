(ns clojure-experiments.purely-functional.puzzles.0465-single-letter-swaps
  "https://ericnormand.me/issues/465
  Solutions: https://gist.github.com/ericnormand/d60a16f9e3e244aba3017e4f9af5533b"
  (:require [clojure.test :refer [deftest is testing]]))

;; To get unstuck, look at this solution https://gist.github.com/ericnormand/d60a16f9e3e244aba3017e4f9af5533b?permalink_comment_id=4135839#gistcomment-4135839
(defn letter-swap [string target-string]
  (when (= (count string) (count target-string))
    (let [[[a b] & other-swaps]
          ;; here's the trick: find only letters that are different!
          (remove #(apply = %) (map vector string target-string))]
      (when (= [[b a]] other-swaps)
        #{a b}))))


(defn letter-swaps [strings target-string]
  ;; interesting usage of (repeat target-string)
  (mapv letter-swap strings (repeat target-string)))

(deftest test-it
  (testing "Eric's example"
    (is (= [#{\a \b} #{\c \d} nil nil nil]
           (letter-swaps ["bacd" "abdc" "abcde" "abcc" "abcd"] "abcd"))))
  (testing "My example"
    (is (= [#{\a \d} #{\a \c} nil]
           (letter-swaps ["dbca" "cbad" "ccba"] "abcd")))))


;;; My attempt => failed miserably
(comment
;; https://stackoverflow.com/questions/56393412/how-to-replace-a-character-in-a-string-using-index-in-clojure
  (defn replace-char
    "Replaces the character at index `idx`in string `s` with character `c`.
  Returns a pair [original-char-at-idx updated-string]."
    [s idx c]
    [(nth s idx)
     (str (subs s 0 idx) c (subs s (inc idx)))])

  (replace-char "ahoj" 2 \h)
  ;; => [\o "ahhj"]

  (mapv  ["bacd" "abdc"] (repeat "abcd"))

  (defn swaps [fst & rst :as _word]
    (fst))

  (let [[fst & rst :as  word] "ahoj"]
    (map
     (fn [idx] (apply str (replace-char (apply str rst) idx fst)))
     (range (count rst))))
;; => ("haoj" "ohaj" "jhoa")

  (map-indexed
   (fn [i c]
     (map-indexed
      (fn [idx rest-c]
        (apply str (replace-char (subs "ahoj" (inc idx)) idx rest-c)))
      (subs "ahoj" (inc idx))))
   (rest "ahoj"))

  (defn letter-swaps
    "For each string in the sequence of strings determine
  whether it is equal to the target string after exactly one letter swap."
    [string-seq target-string]
    (mapv (fn [s]
            (= s target-string))
          string-seq))

  .)
