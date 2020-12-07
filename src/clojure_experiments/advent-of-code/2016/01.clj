(ns advent-of-clojure.2016.01
  "Day 1: No time for a taxicab: http://adventofcode.com/2016/day/1"
  (:require [clojure.spec.alpha :as s]))

;;; https://en.wikipedia.org/wiki/Taxicab_geometry


(defn parse-steps
  "Parses steps represented as a single string into the collection of steps (strings)."
  [steps-string]
  (clojure.string/split steps-string #", "))

;; Puzzle 1 input: http://adventofcode.com/2016/day/1/input
(def steps-1
  (parse-steps "L5, R1, R3, L4, R3, R1, L3, L2, R3, L5, L1, L2, R5, L1, R5, R1, L4, R1, R3, L4, L1, R2, R5, R3, R1, R1, L1, R1, L1, L2, L1, R2, L5, L188, L4, R1, R4, L3, R47, R1, L1, R77, R5, L2, R1, L2, R4, L5, L1, R3, R187, L4, L3, L3, R2, L3, L5, L4, L4, R1, R5, L4, L3, L3, L3, L2, L5, R1, L2, R5, L3, L4, R4, L5, R3, R4, L2, L1, L4, R1, L3, R1, R3, L2, R1, R4, R5, L3, R5, R3, L3, R4, L2, L5, L1, L1, R3, R1, L4, R3, R3, L2, R5, R4, R1, R3, L4, R3, R3, L2, L4, L5, R1, L4, L5, R4, L2, L1, L3, L3, L5, R3, L4, L3, R5, R4, R2, L4, R2, R3, L3, R4, L1, L3, R2, R1, R5, L4, L5, L5, R4, L5, L2, L4, R4, R4, R1, L3, L2, L4, R3"))

(defn- valid-step
  "Checks whether given step represented as a string is valid
  and if so returns its two components:
  direction - either \"L\" or \"R\"
  number of steps - integer"
  [step]
  (when-let [[_ direction step-count] (re-find #"(^[L|R])(\d+$)" step)]
    [direction
     (Integer/parseInt step-count)]))
#_(valid-step "L5")
#_(valid-step "L51")
#_(valid-step "D5")

(s/def ::step valid-step)
(s/def ::steps (s/coll-of ::step))

(def faces {:N [:W :E]
            :E [:N :S]
            :S [:E :W]
            :W [:S :N]})

(defn- next-locations
  "Similar to `next-location` but returns the whole path from the beginning to the end
  as a vector (instead of just returning the final location.
  That means it's more generic than `next-location`- the final point can be retrieved
  simply by calling `last` on the second part of the result.)"
  [[face-direction x y] [step-direction step-count]]
  (let [new-face-direction
        (case step-direction
          "L" (first (faces face-direction))
          "R" (second (faces face-direction)))]
    (case new-face-direction
      :N [:N (for [y (range y (+ y step-count 1))]
               [x y])]
      :E [:E (for [x (range x (+ x step-count 1))]
               [x y])]
      :S [:S (for [y (range y (- y step-count 1) -1)]
               [x y])]
      :W [:W (for [x (range x (- x step-count 1) -1)]
               [x y])])))


(defn- next-location
  ;; face-direction is one of `faces`
  [current-position steps]
  (let [[face path] (next-locations current-position steps)
        [x y] (last path)]
    [face x y]))

(next-locations [:W 0 0] ["L" 10])
(next-location [:N 0 0] ["L" 10])

(defn- distance [[x y]]
  (+ (Math/abs x) (Math/abs y)))

(defn- final-location [steps]
  (subvec
   (reduce next-location
           [:N 0 0]
           (map valid-step steps))
   1))

(s/fdef block-distance
        :args (s/cat :steps ::steps))
(defn block-distance
  "Given the sequence of steps of form `Ln` (e.g. `L4`), i.e. n blocks to the left
  and `Rn` (e.g. `R7`), i.e. (n blocks to the right)
  compute the shortest block distance from the beginning.
  See https://en.wikipedia.org/wiki/Taxicab_geometry."
  [steps]
  (distance (final-location steps)))

(block-distance steps-1)

;;; Second part:
;;; The headquarter of Easter Bunny is at the first location you visit twice
(defn- next-hq-location [{:keys [visited current-face-and-loc] :as acc}
                         current-step]
  (let [[new-face points-to-location] (next-locations current-face-and-loc current-step)
        [new-x new-y] (last points-to-location)]
    (if (some (set points-to-location) visited)
      (reduced acc)
      ;; we've found the headquarter => terminate early
      (-> acc
          (update :visited conj points-to-location)
          (assoc :current-face-and-loc [new-face new-x new-y])))))

(next-hq-location {:visited #{}
                   :current-face-and-loc [:N 0 0]}
                  ["L" 10])

(defn- headquarter-location [steps]
  (subvec
   (reduce next-hq-location
           {:visited #{}
            :current-face-and-loc [:N 0 0]}
           (map valid-step steps))
   1))

(s/fdef headquarter-distance
        :args (s/cat :steps ::steps))
(defn headquarter-distance [steps]
  (distance (headquarter-location steps)))

(headquarter-distance ["R8" "R4" "R4" "R8"])
