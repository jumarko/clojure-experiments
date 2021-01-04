(ns clojure-experiments.advent-of-code.advent-2020.day-10
  "https://adventofcode.com/2020/day/10
  Input: https://adventofcode.com/2020/day/10/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]))


(def sample-input
  "16
10
15
5
1
11
7
19
6
12
4")

(def sample-input2
  "28
33
18
42
31
14
46
20
48
47
24
23
49
45
19
38
39
11
1
32
25
35
8
17
7
9
4
2
34
10
3")

(def sample-jolts (->> sample-input str/split-lines (mapv #(Long/parseLong %))))
(def sample-jolts2 (->> sample-input2 str/split-lines (mapv #(Long/parseLong %))))

(def test-jolts (read-input 10 #(Long/parseLong %)))

(defn- all-jolts-sorted [jolts]
  (let [charging-outlet-jolt 0
        device-jolt (+ 3 (apply max jolts))
        all-jolts (conj jolts charging-outlet-jolt device-jolt)]
    (sort all-jolts)))

(defn differences [jolts]
  (->> (all-jolts-sorted jolts)
       (partition 2 1)
       (map (fn [[v1 v2]] (- v2 v1)))))

(differences sample-jolts)
;; => (1 3 1 1 1 3 1 1 3 1 3 3)
(frequencies (differences sample-jolts2))
;; => {1 23, 3 8, 2 1}

(defn multiply-1-and-3-diffs
  [jolts]
  (let [diffs (differences jolts)
        {ones 1 threes 3} (frequencies diffs)]
    (* ones threes)))
(multiply-1-and-3-diffs sample-jolts2)
;; should be 22 * 10
;; => 220

(multiply-1-and-3-diffs test-jolts)
;; => 1998


;;; part 2 - count all the possible arragments
(defn valid-diff? [a b]
  (< 0  (- b a) 4))

(defn valid-index-for-removal
  "Returns index if the element from `numbers` at given index can be removed, nil otherwise
  while preserving the max acceptable distance of 3."
  [numbers index _]
  (let [numbersv (vec numbers)
        before (get numbersv (dec index) Integer/MIN_VALUE)
        after (get numbersv (inc index) Integer/MAX_VALUE)]
    (assert (< before after) "Elements are not sorted!")
    (when (< 0  (- after before) 4)
      index)))

(defn indexes-to-remove [numbers]
  (keep-indexed (partial valid-index-for-removal numbers)
                numbers))
;; => (3 4 7)

(defn- remove-index [numbers index]
  (let [numbersv (vec numbers)]
    (vec (concat (subvec numbersv 0 index)
                 (subvec numbersv (inc index) (count numbersv))))))

(defn remove-one [numbers]
  ;; TODO: why do we need dedupe?
  (dedupe (map
    (partial remove-index numbers)
    (indexes-to-remove numbers))))
(def memo-remove-one (memoize remove-one))
(remove-one (all-jolts-sorted sample-jolts))
;; => [[0 1 4 6 7 10 11 12 15 16 19 22]
;;     [0 1 4 5 7 10 11 12 15 16 19 22]
;;     [0 1 4 5 6 7 10 12 15 16 19 22]]

;; TODO

;; This takes a long time so don't use directlry for large samples!
;; (at least minutes for sample-jolts2)
(defn all-arrangements [jolts]
  (loop [arrangements [] ; TODO: check why we're getting duplicates
         todo #{(all-jolts-sorted jolts)}]
    (if (empty? todo)
      arrangements
      (recur (into arrangements todo)
             (set (mapcat remove-one todo))))))
(all-arrangements sample-jolts)
;; => #{[0 1 4 6 7 10 12 15 16 19 22] [0 1 4 5 7 10 11 12 15 16 19 22] [0 1 4 7 10 11 12 15 16 19 22] [0 1 4 5 6 7 10 12 15 16 19 22] (0 1 4 5 6 7 10 11 12 15 16 19 22) [0 1 4 6 7 10 11 12 15 16 19 22] [0 1 4 5 7 10 12 15 16 19 22] [0 1 4 7 10 12 15 16 19 22]}

(def cache (atom {}))


(defn count-arrangements [jolts]
  (loop [arrangements-count 0 ; TODO: check why we're getting duplicates
         todo #{(all-jolts-sorted jolts)}]
    (println "Current count " arrangements-count)
    (println "TODO count" (count todo))
    (if (empty? todo)
      (do
        (swap! cache assoc jolts arrangements-count)
        arrangements-count)
      (if-let [[_ ret] (find @cache jolts)]
        ret
        (recur (+ arrangements-count (count todo))
               (set (mapcat memo-remove-one todo)))))))

(def memo-count-arrangements (memoize count-arrangements))

#_(time (count-arrangements sample-jolts2))
"Elapsed time: 830.807147 msecs"
;; => 19208

;; This takes too long and ends up with OOM after many minutes :(
#_(time (count-arrangements test-jolts))

;; => Check reddit: https://www.reddit.com/r/adventofcode/comments/kcyj6r/2020_day_10_part_2_bad_at_math_am_i_supposed_to/
;; You've found one of the three sensible ways to solve 10 part 2:
;; - Recurse down from the maximum adapter value, caching your answers (called Memoization)
;; - Iterate up from the smallest adapter value, storing intermediate results in a table (called Dynamic Programming)
;; - Divide the problem up into subproblems which get separately counted then multiplied together (... called Maths, I guess!)


;;; Part 2- let's try again...
;;;- proper depth-first search? 
(defn count-arrangements-2 [numbers]
  (println "count: " (count numbers))
  (if (empty? numbers)
    0
    (+ 1
       (apply + (map #(count-arrangements-2 (remove-index numbers %))
                     (indexes-to-remove numbers))))))
(def memo-count-arrangements-2 (memoize count-arrangements-2))
(memo-count-arrangements-2 (all-jolts-sorted sample-jolts))
(memo-count-arrangements-2 (all-jolts-sorted sample-jolts2))
