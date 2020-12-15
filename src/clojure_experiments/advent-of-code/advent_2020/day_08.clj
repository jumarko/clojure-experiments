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
    (let [{:keys [op arg] :as current-instruction} (get instructions current-index)
          executed (conj executed-instructions current-index)]
      (cond
        ;; terminated
        (nil? current-instruction)
        [true acc]

        ;; also done but without termination
        (contains? executed-instructions current-index)
        [false acc]

        ;; process next instruction
        :else
        (case op
          "nop" (recur acc
                       (inc current-index)
                       executed)
          "acc" (recur (+ acc arg)
                       (inc current-index)
                       executed)
          "jmp" (recur acc
                       (+ current-index arg)
                       executed)
          (throw (ex-info (str "Unexpected operation: " op)
                          {:instruction current-instruction
                           :executed-instructions executed-instructions
                           :current-index current-index})))))))

(compute sample-instructions)
;; => [false 5]

(time (compute test-instructions))
"Elapsed time: 0.282456 msecs"
;; => [false 1749]


;;; Part 2
(defn- modify-instruction [{:keys [op arg]:as inst}]
  (if (#{0 1} arg) ; jump with 0 creates infinite loop, jump 1 same as noop
    inst
    (case op
      "nop" (assoc inst :op "jmp")
      "jmp" (assoc inst :op "nop")
     inst)))

(defn- modifications
  "Returns a lazy seq of all possible modifications of given list of instructions."
  [instructions index]
  (lazy-seq (when-let [inst (get instructions index)]
              ;; TODO: this could be optimized to skip modifications where the instruction isn't changed
              ;; that is they are the same as the previous modification
              (cons (assoc instructions index (modify-instruction inst))
                    (modifications instructions (inc index))))))

#_(modifications sample-instructions 0)

(defn modify-to-terminate
  "Try to modify the list of instructions by changing jmp to nop or vice versa
  to make them terminate."
  [instructions]
  (let [modified-instructions (modifications instructions 0)]
    (->> (map compute modified-instructions)
         (filter (fn [[terminated? acc]] terminated?))
         first
         ;; return accumulator
         second)))

(time (modify-to-terminate sample-instructions))
;; "Elapsed time: 0.493497 msecs"
;; => 8

(time (modify-to-terminate test-instructions))
;; => 515

