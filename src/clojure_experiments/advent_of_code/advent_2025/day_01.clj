(ns clojure-experiments.advent-of-code.advent-2025.day-01
  "https://adventofcode.com/2025/day/1
  Input: https://adventofcode.com/2025/day/1/input.
  "
  (:require
   [clojure-experiments.advent-of-code.utils :as utils]))

(defn parse-rotation [instruction]
  (let [direction (case (first instruction)
                    \R :right
                    \L :left)
        distance (parse-long (subs instruction 1))]
    [direction distance]))

(assert (= [:right 11] (parse-rotation "R11")))


(def parsed-input (mapv parse-rotation (utils/read-input 2025 1)))
;; => ["R11"
;;     "R8"
;;     "L47"
;;     "L20"
;;     "L25"
;;     "L40"
;;     "R50"
;;     "L44"
;;     "L38"

(defn- unbounded-rotation
  [position [direction distance]]
  (let [op (case direction :left - :right +)
        ;; the distance can be more traverse the whole circle and move beyond
        ;; therefore we adjust this unbounded-position with modulus below
        unbounded-position (op position distance)]
    unbounded-position))

(defn- apply-rotation
  [dial-size position rotation]
  [(mod (unbounded-rotation position rotation) dial-size)])

(assert (= [95] (apply-rotation 100 50 [:right 45])))
(assert (= [5] (apply-rotation 100 50 [:right 155])))


(defn apply-rotations
  "Starting in `init-position` performs rotations as given by `rotation-instructions`
  where each instruction is a tuple of [direction distance].
  Returns a sequence of positions of the dial's arrow after each rotation,
  _including_ `init-position`."
  [rotation-fn dial-size init-position rotation-instructions]
  (reduce
   (fn [[positions zero-hits] instruction]
     (let [current-position (peek positions)
           [position-after-rotation hits] (rotation-fn dial-size current-position instruction)]
       [(conj positions position-after-rotation)
        (+ zero-hits (or hits 0))]))
   [[init-position] 0]
   rotation-instructions))

(assert (= [50] (first (apply-rotations apply-rotation 100 50 []))))
(assert (= [50 90]
           (first (apply-rotations apply-rotation 100 50 [[:right 40]]))))
;; this is the example from https://adventofcode.com/2025/day/1
(def sample-rotations [[:left 68]
                       [:left 30]
                       [:right 48]
                       [:left 5]
                       [:right 60]
                       [:left 55]
                       [:left 1]
                       [:left 99]
                       [:right 14]
                       [:left 82]])
(assert (= [50 82 52 0 95 55 0 99 0 14 32]
           (first (apply-rotations apply-rotation 100 50 sample-rotations))))

(defn password [parsed-rotations]
  (let [positions (first (apply-rotations apply-rotation 100 50 parsed-rotations))]
    (count (filter zero? positions))))

;;; Part 1 answer
(assert (= 964 (password parsed-input)))



;;; Part 2: modification: include number of times the arrow crosses zero,
;;; whether it ends up there or not.
;;; ...

;; I guess I will need to work with `unbounded-position` inside `rotation`
;; ... and also change the return value to return both final position (after applying the rotation)
;; as well as number of "0 hits" (all the crossings of 0, including where it ends up at 0)
(defn- zero-hits
  [dial-size position [direction _distance :as rotation]]
  (let [unbounded-position (unbounded-rotation position rotation)
        adjusted-position (case direction
                            :left (- unbounded-position dial-size)
                            :right unbounded-position)
        hits (abs (quot adjusted-position dial-size))
        ;; same as in `apply-rotation`
        final-position (mod (unbounded-rotation position rotation) dial-size)]
    [final-position hits]))

(assert (= [99 0] (zero-hits 100 50 [:right 49])))
(assert (= [0 1] (zero-hits 100 50 [:right 50])))
(assert (= [99 1] (zero-hits 100 50 [:right 149])))
(assert (= [0 2] (zero-hits 100 50 [:right 150])))
(assert (= [1 0] (zero-hits 100 1 [:left 0])))
(assert (= [0 1] (zero-hits 100 1 [:left 1])))
(assert (= [1 1] (zero-hits 100 1 [:left 100])))
(assert (= [0 2] (zero-hits 100 1 [:left 101])))

(defn password2 [parsed-rotations]
  (let [[positions zero-hits] (apply-rotations zero-hits 100 50 parsed-rotations)]
    (- zero-hits
       (dec (count (filter zero? positions))))))

(assert (= 6 (password2 sample-rotations)))

(assert (= 5872 (password2 parsed-input)))






;;; Cheating - using genmeblog solution: https://github.com/genmeblog/advent-of-code/blob/master/src/advent_of_code_2025/day01.clj

(defn parse-line [line] (-> line (clojure.string/escape {\R \+ \L \-}) parse-long))

(def data (map parse-line (utils/read-input 2025 1)))

(defn find-zeros
  [data method]
  (->> data (reduce method [50 0]) last))
;; => 1097

(defn second-method
  [[current zeros] rotation]
  (let [ncurrent (+ current rotation)
        rotations (abs (quot ncurrent 100))
        nzeros (if (or (zero? ncurrent)
                       (and (pos? current) (neg? ncurrent))) (inc rotations) rotations)]
    [(mod ncurrent 100) (+ nzeros zeros)]))

(def part-2 (find-zeros data second-method))
;; => 5872
