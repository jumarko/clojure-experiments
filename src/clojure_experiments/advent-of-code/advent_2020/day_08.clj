(ns clojure-experiments.advent-of-code.advent-2020.day-08
  "https://adventofcode.com/2020/day/8
  Input: https://adventofcode.com/2020/day/8/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(s/def ::op #{"acc" "jmp" "nop"})
(s/def ::arg int?)
(s/def ::instruction (s/keys :req-un [::op ::arg]))
(s/exercise `::instruction)
;; => ([{:op "nop", :arg -1} {:op "nop", :arg -1}]
;;     [{:op "acc", :arg -1} {:op "acc", :arg -1}]
;;     [{:op "nop", :arg -1} {:op "nop", :arg -1}]
;;     [{:op "acc", :arg 1} {:op "acc", :arg 1}]
;;     [{:op "acc", :arg -3} {:op "acc", :arg -3}]
;;     [{:op "acc", :arg 0} {:op "acc", :arg 0}]
;;     [{:op "acc", :arg 0} {:op "acc", :arg 0}]
;;     [{:op "nop", :arg -2} {:op "nop", :arg -2}]
;;     [{:op "acc", :arg 1} {:op "acc", :arg 1}]
;;     [{:op "jmp", :arg -6} {:op "jmp", :arg -6}])

(def sample-input
  "nop +0
acc +1
jmp +4
acc +3
jmp -3
acc -99
acc +1
jmp -4
acc +6"
  )

(defn parse-instruction [instruction-line]
  (let [[_ op arg] (re-matches #"^(nop|acc|jmp) (.+)$" instruction-line)]
    {:op op
     :arg (Integer/parseInt arg)}))
(def sample-instructions (mapv parse-instruction (str/split-lines sample-input)))
;; => [{:op "nop", :arg 0}
;;     {:op "acc", :arg 1}
;;     {:op "jmp", :arg 4}
;;     {:op "acc", :arg 3}
;;     {:op "jmp", :arg -3}
;;     {:op "acc", :arg -99}
;;     {:op "acc", :arg 1}
;;     {:op "jmp", :arg -4}
;;     {:op "acc", :arg 6}]

(def test-instructions (read-input 8 parse-instruction))

(defn compute
  "Follows the list of instructions until one of them is executed the second time.
  At that point, returns a value of the global accumulator."
  [instructions]
  (loop [acc 0
         current-index 0
         executed-instructions #{}]
    (let [{:keys [op arg] :as _current-instruction} (nth instructions current-index)
          executed (conj executed-instructions current-index)]
      (if (contains? executed-instructions current-index)
        acc
        (case op
          "nop" (recur acc
                       (inc current-index)
                       executed)
          "acc" (recur (+ acc arg)
                       (inc current-index)
                       executed)
          "jmp" (recur acc
                       (+ current-index arg)
                       executed))))))

(compute sample-instructions)
;; => 5

(time (compute test-instructions))
"Elapsed time: 0.282456 msecs"
;; => 1749


