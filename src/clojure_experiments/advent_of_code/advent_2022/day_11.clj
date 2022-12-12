(ns clojure-experiments.advent-of-code.advent-2022.day-11
  "https://adventofcode.com/2022/day/11
  Input: https://adventofcode.com/2022/day/11/input"
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure-experiments.macros.macros  :refer [assert=]]
   [clojure.string :as str]
   [medley.core :as m]))

(def full-input (utils/read-input "11"))

(def sample-input
  (str/split-lines
   "Monkey 0:
  Starting items: 79, 98
  Operation: new = old * 19
  Test: divisible by 23
    If true: throw to monkey 2
    If false: throw to monkey 3

Monkey 1:
  Starting items: 54, 65, 75, 74
  Operation: new = old + 6
  Test: divisible by 19
    If true: throw to monkey 2
    If false: throw to monkey 0

Monkey 2:
  Starting items: 79, 60, 97
  Operation: new = old * old
  Test: divisible by 13
    If true: throw to monkey 1
    If false: throw to monkey 3

Monkey 3:
  Starting items: 74
  Operation: new = old + 3
  Test: divisible by 17
    If true: throw to monkey 0
    If false: throw to monkey 1"
))

(defn parse-monkeys [input]
  (->> input
       (partition-by empty?)
       (remove #(= [""] %))))
(first (parse-monkeys sample-input))
;; => ("Monkey 0:"
;;     "  Starting items: 79, 98"
;;     "  Operation: new = old * 19"
;;     "  Test: divisible by 23"
;;     "    If true: throw to monkey 2"
;;     "    If false: throw to monkey 3")

(defn parse-op [op-line]
  (let [[_ arg1 operator arg2] (re-find #"new = (\w+) ([+\-*/]) (\w+)" op-line)]
    {:operator (case operator "+" + "-" - "*" * "/" /)
     :args (mapv #(if (= % "old") :old (parse-long %))
                 [arg1 arg2])}))

(defn parse-monkey
  [[m itms op test t f :as _monkey-lines]]
  (let [monkey (parse-long (re-find #"\d+" m))
        items (mapv parse-long (re-seq #"\d+" itms))
        operation (parse-op op)
        [divisible-by true-target false-target] (map #(parse-long (re-find #"\d+" %))
                                                     [test t f])]
    {:monkey monkey
     :items items
     :operation operation
     :divisible-by divisible-by
     :targets {true true-target false false-target}}))

(parse-monkey (first (parse-monkeys sample-input)))
;; => {:monkey 0,
;;     :items [79 98],
;;     :operation {:operator #function[clojure.core/*], :args [:old 19]},
;;     :divisible-by 23,
;;     :targets {true 2, false 3}}

;; Now redefine `parse-monkeys` to use `parse-monkey`
(defn parse-monkeys [input]
  (->> input
       (partition-by empty?)
       (remove #(= [""] %))
       (mapv parse-monkey)))
(def sample-monkeys (parse-monkeys sample-input))
(take 2 sample-monkeys)
;; => ({:monkey 0,
;;      :items [79 98],
;;      :operation {:operator #function[clojure.core/*], :args [:old 19]},
;;      :divisible-by 23,
;;      :targets {true 2, false 3}}
;;     {:monkey 1,
;;      :items [54 65 75 74],
;;      :operation {:operator #function[clojure.core/+], :args [:old 6]},
;;      :divisible-by 19,
;;      :targets {true 2, false 0}})

;; now make the monkeys take turns...

(defn worry-level [item {:keys [operator args] :as _operation}]
  (quot (apply operator (map #(if (= % :old) item %) args))
        3))

(assert= 500
         (let [{:keys [items operation]} (first sample-monkeys)]
           (worry-level (first items) operation)))

;; debug print 
(defn- prn-monkeys [ms]
  (prn (mapv (fn [[k v]] [k (:items v)])
             ms)))

(defn monkey-turn [monkeys-lookup {:keys [monkey items operation divisible-by targets]}]
  (doto
   (reduce
    (fn [lookup item]
      (let [worry-item (worry-level item operation)
            target-monkey (get targets (zero? (mod worry-item divisible-by)))]
        (-> lookup
            ;; :inspections is a special key to track number of items the monkey has inspected
            (update-in [monkey :inspections] (fnil inc 0))
            (update-in [monkey :items] subvec 1)
            (update-in [target-monkey :items] conj worry-item))))
    monkeys-lookup
    items)
    ;; debug print
   #_prn-monkeys
    ))

(let [monkeys sample-monkeys
      monkeys-lookup (->> monkeys
                          (m/index-by :monkey)
                          (into (sorted-map)))]
  (->> (monkey-turn monkeys-lookup (first monkeys))
       (map (fn [[k v]] [k (:items v)]))))
;; => ([0 []]
;;     [1 [54 65 75 74]]
;;     [2 [79 60 97]]
;;     [3 [74 79 98]])

(defn round [monkeys-lookup]
  (loop [ml monkeys-lookup
         n 0]
    (if (< n (count monkeys-lookup))
      (recur (monkey-turn ml (get ml n))
             (inc n))
      ml)))

(defn monkeys->lookup [monkeys]
  (->> monkeys (m/index-by :monkey) (into (sorted-map))))
(round (monkeys->lookup sample-monkeys))
;; => debug print:
;; [[0 []] [1 [54 65 75 74]] [2 [79 60 97]] [3 [74 500 620]]]
;; [[0 [20 23 27 26]] [1 []] [2 [79 60 97]] [3 [74 500 620]]]
;; [[0 [20 23 27 26]] [1 [2080]] [2 []] [3 [74 500 620 1200 3136]]]
;; [[0 [20 23 27 26]] [1 [2080 25 167 207 401 1046]] [2 []] [3 []]]

;; NOTE: "after 20th round" means that you need to get the 21st element
(->> (nth (iterate round (monkeys->lookup sample-monkeys))
      20)
     vals
     (map (juxt :monkey :inspections)))
;; => ([0 101] [1 95] [2 7] [3 105])

(defn monkey-business [monkeys rounds]
  (->> (nth (iterate round (monkeys->lookup monkeys))
            rounds)
       vals
       (map (juxt :monkey :inspections))
       (sort-by (comp - second)) ; sort by most inspections
       (take 2)
       (map second)
       (apply *)))
(monkey-business sample-monkeys 20)
;; => 10605


(defn part-1 []
  (monkey-business (parse-monkeys full-input) 20))

(assert= 110264 (part-1))



;;; Part 2.
