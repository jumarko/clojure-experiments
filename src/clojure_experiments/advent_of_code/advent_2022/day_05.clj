(ns clojure-experiments.advent-of-code.advent-2022.day-05
  "https://adventofcode.com/2022/day/5
  Input: https://adventofcode.com/2022/day/5/input.

  An initial position of _crates_ is given - multiple stacks.
  Crates are then moved in the defined quantities from one stack to another.
  Each crate is moved one at a time.
  The Elves need to know which crate will end up on _top_ of each stack."
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure.string :as str])
  )


(def input (utils/read-input "05"))

(def sample-input (-> "    [D]    
[N] [C]    
[Z] [M] [P]
 1   2   3 

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2"
                      (str/split-lines)))

;;; Part 1

(defn parse-move [move-str]
  (map parse-long (re-seq #"\d+" move-str)))
(parse-move "move 1 from 2 to 1")
;; => (1 2 1)

(defn parse-init-positions [input]
  (take-while (complement str/blank?) input))
(parse-init-positions sample-input)
;; => ("    [D]    " "[N] [C]    " "[Z] [M] [P]" " 1   2   3 ")

(defn parse-init-positions [input]
  (let [levels (->> input
                    (take-while (complement str/blank?)))]
    (for [l (butlast levels)
          crate (partition-all 4 l)]
      (re-find #"[A-Z]+" (apply str crate)))))
(parse-init-positions sample-input)
;; => (nil "D" nil "N" "C" nil "Z" "M" "P")

;; that's not exactly what we need - so try to map them properly
(defn parse-init-positions [input]
  (let [levels (->> input
                    (take-while (complement str/blank?)))
        level-count (->> (last levels) (re-seq #"\d+") last parse-long)]
    (->> (for [l (butlast levels)
               crate (partition-all 4 l)]
           (re-find #"[A-Z]+" (apply str crate)))
         (partition level-count))))
(parse-init-positions sample-input)
;; => ((nil "D" nil)
;;     ("N" "C" nil)
;;     ("Z" "M" "P"))


;; still not enough - we need to transpose the 'matrix' !
(defn parse-init-positions [input]
  (let [levels (->> input
                    (take-while (complement str/blank?)))
        level-count (->> (last levels) (re-seq #"\d+") last parse-long)]
    (->> (for [l (butlast levels)
               crate (partition-all 4 l)]
           (re-find #"[A-Z]+" (apply str crate)))
         (partition level-count)
         ;; transpose the matrix
         (apply map vector)
         ;; and nils finally
         (map #(remove nil? %)))))
(parse-init-positions sample-input)
;; => (("N" "Z") ("D" "C" "M") ("P"))

;; notice how each list can be used like a stack
;; via `pop` and `peek`
(peek '("N" "Z"))
;; => "N"


;; Now we can finally try to go through the moves

(defn apply-move [stacks move]
  (let [[n from to] move
        [from-idx to-idx] (map dec [from to])
        from-stack (nth stacks from-idx)
        to-stack (nth stacks to-idx)]
    (assoc (vec stacks)
           from-idx (drop n from-stack)
           to-idx (apply conj to-stack (take n from-stack)))))

(parse-init-positions sample-input)
;; => (("N" "Z") ("D" "C" "M") ("P"))
(parse-move (nth sample-input 5))
;; => (1 2 1)
(apply-move (parse-init-positions sample-input)
            (parse-move (nth sample-input 5)))
;; => [("D" "N" "Z") ("C" "M") ("P")]

(defn apply-moves [input]
  (let [init-stacks (parse-init-positions input)
        max-stack-depth (apply max (map count init-stacks))
        ;; +1 line for stacks numbers and +1 empty line
        skip-lines (+ max-stack-depth 2)
        moves (map parse-move (drop skip-lines input))]
    (reduce apply-move init-stacks moves)))
(assert (= ['("C") '("M") '("Z" "N" "D" "P")]
           (apply-moves sample-input)))

(defn tops-of-stacks [input]
  (->> (apply-moves input)
       (map first)
       (apply str)))

(assert (= "CMZ" (tops-of-stacks sample-input)))

;; puzzle-1
(assert (= "TWSGQHNHL" (tops-of-stacks input)))



;;; Part 2 - moving multiple crates at once

(defn apply-move [stacks move]
  (let [[n from to] move
        [from-idx to-idx] (map dec [from to])
        from-stack (nth stacks from-idx)
        to-stack (nth stacks to-idx)]
    (assoc (vec stacks)
           from-idx (drop n from-stack)
           ;; the only change it to use `concat` instead of  `apply conj`
           to-idx (concat (take n from-stack) to-stack ))))
(assert (= "MCD" (tops-of-stacks sample-input)))


;; puzzle-2
(assert (= "JNRSCDWPP" (tops-of-stacks input)))



;;; All-in-one - all the code for the part 2 together
(defn all-in-one [input]
  (let [levels (->> input (take-while (complement str/blank?)))
        level-count (->> (last levels) (re-seq #"\d+") last parse-long)
        init-stacks  (->> (for [l (butlast levels)
                                crate (partition-all 4 l)]
                            (re-find #"[A-Z]+" (apply str crate)))
                          (partition level-count)
                          ;; transpose the matrix
                          (apply map vector)
                          (map #(remove nil? %)))
        skip-lines (+ (apply max (map count init-stacks)) 2)
        moves (map (fn [move-str] (map parse-long (re-seq #"\d+" move-str)))
                   (drop skip-lines input))]
    (reduce apply-move init-stacks moves)
    ;; get tops of the stacks
    (->> (apply-moves input) (map first) (apply str))))

(assert (= "JNRSCDWPP" (all-in-one input)))

