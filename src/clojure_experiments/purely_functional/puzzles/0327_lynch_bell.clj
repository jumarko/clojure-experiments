(ns clojure-experiments.purely-functional.puzzles.0327-lynch-bell
  "Puzzle: https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-327-tip-always-be-decomplecting/
  Solution: https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-328-tip-don-t-use-def-inside-a-defn/
  Find the largest Lynch Bell number, that is number consisting of unique digits
  and divisible by all such digits/numbers.
  E.g. 135 is divisible by all 1, 3, 5.
  Note: The number can't contain 0 since division by zero leads to the arithmetic exception."
  (:require [clojure-experiments.purely-functional.puzzles.util :as u]))

;;; The largest one could be 123456789

(defn lynch-bell? [n]
  (let [digs (u/digits n)
        nonzero-digs (remove zero? digs)
        rems (map #(rem n %) nonzero-digs)]
    (every? zero? rems)))

(defn generate-lynch-bells [start]
  (let [max-possible-lynch-bell 987654321]
    (->> (range (inc max-possible-lynch-bell) start -1)
         (filter lynch-bell?))))

(defn max-lynch-bell []
  (first (generate-lynch-bells 1)))

(time (max-lynch-bell ))
;; => 987653520
;; 0.55736 msecs

(comment 
  (lynch-bell? 135)
  ;; => true
    
  (lynch-bell? 137)
  ;; => false

  ;; eoc
  )

