(ns clojure-experiments.advent-of-code.advent-2022.day-02
  "https://adventofcode.com/2022/day/2
  Input: https://adventofcode.com/2022/day/2/input"
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure.string :as str]))

(def input (->> (utils/read-input "02")
                (map #(str/split % #" "))))

;;; Puzzle 1

(def mapping
  {"A" :rock
   "B" :paper
   "C" :scissors
   "X" :rock
   "Y" :paper
   "Z" :scissors})

(def rules
  {:rock {:paper :lost :scissors :win}
   :paper {:rock :win :scissors :lost}
   :scissors {:paper :win :rock :lost}})

(def points
  {:rock 1
   :paper 2
   :scissors 3
   :lost 0
   :draw 3
   :win 6})

;; first, try to get points for the outcome of a round
(->> (first input)
     (map mapping)
     ;; => (:scissors :rock)
     (get-in rules)
     ;; => :lost
     points)
;; => 0

;; ... but that isn't enough because we also need points for the shape we selected
(->> (first input)
     (map mapping)
     ;; => (:scissors :rock)
     ((juxt first #(get-in rules %)))
     ;; => [:scissors :lost]
     (map points)
     ;; => (3 0)
     )

;; That's good - now ecanpsulate the logic in a function and run it for all the rounds
(defn score-round [round]
  (->> round
       (map mapping)
       ((juxt first #(get-in rules %)))
       (map points)))
;; just sanity check
(assert (= [3 0] (score-round (first input))))

(take 10 (map score-round input ))
;; => ((3 0) (3 0) (3 0) (1 6) (3 0) (3 0) (1 0) (2 6) (2 nil) (2 0))

;; (2 nil) above is suspicious - let's debug it (just put a breakpoint or prn in the function)
(score-round (nth input 8))

;; ... Oh yeah, the input is "B" "Y" which means (:paper :paper)
;; I don't have such combination in `rules` => let's fix it!
(def rules
  {:rock {:rock :draw :paper :lost :scissors :win}
   :paper {:paper :draw :rock :win :scissors :lost}
   :scissors {:scissors :draw :paper :win :rock :lost}})
(score-round (nth input 8))
;; => (2 3)

;; that's better - let's look at more data
(take 20 (map score-round input ))
;; => ((3 0) (3 0) (3 0) (1 6) (3 0) (3 0) (1 0) (2 6) (2 3) (2 0) (3 6) (3 0) (3 0) (3 6) (2 3) (3 0) (3 6) (2 0) (3 6) (2 0))

;; looks reasonable.
;; how about computing the final score then?
(defn score-round [round]
  (->> round
       (map mapping)
       ((juxt first #(get-in rules %)))
       (map points)
       ;; this is the new thing
       (apply +)))
(score-round (nth input 8))
;; => 5

(defn total-score [input]
  (apply + (map score-round input)))
(def puzzle-1 (partial total-score input))
(puzzle-1)
;; => 11454

;; WHOOPS - the answer is too low... let's try to figure out what's wrong...
;; refactor the function to be able to debug more easily and separate concerns
(defn play [round]
  (->> round
       (map mapping)
       ((juxt first #(get-in rules %)))))
(play (nth input 8))
;; => [:paper :draw]
(defn score-round [round]
  (->> (play round)
       (map points)
       ;; this is the new thing
       (apply +)))
(assert (= 5 (score-round (nth input 8))))


;; first try on the sample input
(def sample-input [["A" "Y"]
                   ["B" "X"]
                   ["C" "Z"]])
(total-score sample-input)
;; => 15
;; that looks correct!

;; now try to spot some odd data - especially `nil`s
(->> input
     (map play)
     (filter #(some nil? %)))
;; => ()

;; ... hmmm, no nils - what else could be wrong?


;; Back to the basics - read the text!
;; => the first item is what _opponent_ is going to play, not me!!!
;; So we need to fix the `play` function
(defn play [round]
  (->> round
       (map mapping)
       ;; notice `second` and `reverse` to flip the order
       ((juxt second #(get-in rules (reverse %))))))
(map mapping (first input))
;; => (:scissors :rock)
(play (first input))
;; => [:scissors :lost] ; this is OLD result
;; => [:rock :win] ; this is NEW result

;; Let's try agin
(total-score sample-input)
;; => 15 ; still the same

(assert (= 14069 (puzzle-1)))
;; => 14069 ; CORRECT!!!



;;; Part 2:
;;; "Anyway, the second column says how the round needs to end:
;;; - X means you need to lose,
;;; - Y means you need to end the round in a draw,
;;; - and Z means you need to win. Good luck!"

(def mapping
  {"A" :rock
   "B" :paper
   "C" :scissors
   "X" :lost
   "Y" :draw
   "Z" :win})

(map mapping (first input))
;; => (:scissors :lost)

;; TODO: this duplicates the knowledge in `rules` to some extent
;; I found this to be the easiest approach but there should be an alternative
(def rules2
  {:rock {:lost :scissors :win :paper :draw :rock}
   :paper {:lost :rock :win :scissors :draw :paper}
   :scissors {:lost :paper :win :rock :draw :scissors}})
;; compare to rules:
(comment
  (def rules
    {:rock {:rock :draw :paper :lost :scissors :win}
     :paper {:paper :draw :rock :win :scissors :lost}
     ;; ... so I could actually flip it and simply the `play` (puzzle 1) function too!?
     :scissors {:scissors :draw :paper :win :rock :lost}})
)

(defn find-shape [[opponents-shape outcome]]
  (get-in rules2 [opponents-shape outcome]))
(find-shape (map mapping (first input)))
;; => :paper
(defn play2 [round]
  (->> round
       (map mapping)
       ;; `second` returns the outcome, the other function finds the shape to play based on that
       ((juxt second find-shape))))

;; these functions stay the same but need to use `play2`
(defn score-round2 [round] (->> (play2 round) (map points) (apply +)))
(defn total-score2 [input] (apply + (map score-round2 input)))

(play2 (first input))
;; => [:lost :paper]
(score-round2 (first input))
;; => 2
(map play2 sample-input)
;; => ([:draw :rock] [:lost :rock] [:win :rock])
(assert (= 12 (total-score2 sample-input)))

(assert (= 12411 (total-score2 input)))

