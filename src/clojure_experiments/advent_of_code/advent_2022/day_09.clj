(ns clojure-experiments.advent-of-code.advent-2022.day-09
  "Chttps://adventofcode.com/2022/day/9
  Input: https://adventofcode.com/2022/day/9/input"
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure-experiments.macros.macros  :refer [assert=]]
   [clojure.string :as str]))



(def full-input (utils/read-input "09"))

(def sample-input
  (str/split-lines
   "R 4
U 4
L 3
D 1
R 4
D 1
L 5
R 2"))

;;; Rough plan
;;; 1. transform the input into series of movements like R-R-R-R-L-L-L-...
;;; 2. Apply each move separately
;;; 3. After applying a move check if tail is too far from the head;
;;;    if so, the move the tail (horizontally, vertically, or diagonally)
;;;    to the appropriate position

;; 1. transform the input
(defn parse-directions [input]
  (mapcat (fn [repeated-move]
            (let [[direction cnt] (str/split repeated-move #" ")]
              (repeat (parse-long cnt) direction)))
          input))
(parse-directions sample-input)
;; => ("R" "R" "R" "R" "U" "U" "U" "U" "L" "L" "L" "D" "R" "R" "R" "R" "D" "L" "L" "L" "L" "L" "R" "R")

;; 2. apply the move
(def moves-map {"R" [1 0]
                "L" [-1 0]
                "U" [0 1]
                "D" [0 -1]})
(defn equal-or-diff-one [x y]
  (cond
    (= x y) 0
    (< (abs (- x y)) 3) (if (pos? (- x y)) 1 -1)
    :else (throw (ex-info "unexpected difference in coordinates" {:x x :y y}))))

(defn apply-move [{:keys [head tail] :as position} step]
  (let [[hr hc :as new-head] (mapv + head (moves-map step))
        [tr tc] tail
        distance (mapv - new-head tail)
        new-tail (if (every? #(<= -1 % 1) distance)
                   tail
                   ;; if tail is too far we need to move it too
                   (mapv +
                         [(equal-or-diff-one hr tr)
                          (equal-or-diff-one hc tc)]
                         tail))]
    (assoc position :head new-head :tail new-tail)))

(-> {:head [0 0] :tail [0 0]}
    (apply-move "R")
    ;; => {:head [1 0], :tail [0 0]}
    (apply-move "R")
    ;; => {:head [2 0], :tail [1 0]}
    (apply-move "U")
    ;; => {:head [2 1], :tail [1 0]}
    (apply-move "U")
    ;; => {:head [2 2], :tail [2 1]}
    )

(def sample-directions (parse-directions sample-input))
(defn positions [moves]
  (reductions apply-move
              {:head [0 0] :tail [0 0]}
              moves))
(positions sample-directions)
;; => ({:head [0 0], :tail [0 0]}
;;     {:head [1 0], :tail [0 0]}
;;     {:head [2 0], :tail [1 0]}
;;     {:head [3 0], :tail [2 0]}
;;     {:head [4 0], :tail [3 0]}
;;     {:head [4 1], :tail [3 0]}
;;     {:head [4 2], :tail [4 1]}
;;     {:head [4 3], :tail [4 2]}
;;     {:head [4 4], :tail [4 3]}
;;     {:head [3 4], :tail [4 3]}
;;     {:head [2 4], :tail [3 4]}
;;     {:head [1 4], :tail [2 4]}
;;     {:head [1 3], :tail [2 4]}
;;     {:head [2 3], :tail [2 4]}
;;     {:head [3 3], :tail [2 4]}
;;     {:head [4 3], :tail [3 3]}
;;     {:head [5 3], :tail [4 3]}
;;     {:head [5 2], :tail [4 3]}
;;     {:head [4 2], :tail [4 3]}
;;     {:head [3 2], :tail [4 3]}
;;     {:head [2 2], :tail [3 2]}
;;     {:head [1 2], :tail [2 2]}
;;     {:head [0 2], :tail [1 2]}
;;     {:head [1 2], :tail [1 2]}
;;     {:head [2 2], :tail [1 2]})

(defn unique-tail-positions [moves]
  (->> (positions moves)
       (map :tail)
       set))
(assert= 13 (count (unique-tail-positions sample-directions)))

(defn puzzle-1 []
  (->> full-input
       parse-directions
       unique-tail-positions
       count))
(assert= 6011 (puzzle-1))



;;; part 2
;;; Now the rope has 10 knots!

(-> {:head [0 0] :tail [0 0]}
    (apply-move "R"))
(-> {:head [0 0] :tail [0 0]}
    (apply-move "R"))

;;; ... OMG...


;;; Check this: https://gist.github.com/maacl/992c94a6000ae1ae206a926eccd34294#file-clj
;;; it's similar in spirit to the first part of my solution
;;; but I got stuck on the second part.
;;; Here they solve it very elegantly and concisely.
(comment

  (def pos {0 0 1 1 2 1 -1 -1 -2 -1})
  (def dirs {"U" [1  0] "R" [0  1] "D" [-1  0] "L" [0 -1]})
  (defn move [pos move]  (mapv + pos move))
  (defn diff [pos1 pos2] (mapv - pos2 pos1))
  (defn touching? [d] (#{[0 1] [1 0] [1 1] [0 0]} (mapv abs d)))
  (defn positions [f moves] (reductions f [0 0] moves))

  (defn moves->1-step-moves
    [moves]
    (mapcat (fn [[d n]] (repeat (parse-long n) (dirs d))) moves))

  (defn H-pos->T-pos
    [T-pos H-pos]
    (let [d (diff T-pos H-pos)]
      (if (touching? d)
        T-pos
        (move T-pos (mapv pos d)))))

  (def common
    (->>
     full-input
     (map #(str/split % #" "))
     moves->1-step-moves
     (positions move)))

  (def end (comp count set))
  
  ;; Part 1
  (->> common (positions H-pos->T-pos) end)
;; => 6011

  ;; Part 2
  (->> common (iterate (partial positions H-pos->T-pos)) (take 10) last end)
;; => 2419

  )


