(ns clojure-experiments.advent-of-code.advent-2020.day-07
  "https://adventofcode.com/2020/day/7
  Input: https://adventofcode.com/2020/day/7/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.string :as str]
            [clojure.set :as set]))

;;;; This is about navigating a graph of bags
;;;; Some useful Clojure graph libraries:
;;;; - loom
;;;; - stuartsierra/dependency
;;;; - asumi


(def sample-input
  "light red bags contain 1 bright white bag, 2 muted yellow bags.
dark orange bags contain 3 bright white bags, 4 muted yellow bags.
bright white bags contain 1 shiny gold bag.
muted yellow bags contain 2 shiny gold bags, 9 faded blue bags.
shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
dark olive bags contain 3 faded blue bags, 4 dotted black bags.
vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
faded blue bags contain no other bags.
dotted black bags contain no other bags.")

;;; parsing input
(defn- parse-bag [bag-str]
  (let [[_ bag-count color] (re-find #"\s*(\d*)\s*(.+) bags?" bag-str)]
    (when-not (=  "no other" color)
      {:bag/color color
       :bag/count (Integer/parseInt bag-count)})))
(parse-bag "1 bright white bag")
;; => #:bag{:color "bright white", :count 1}

(defn- parse-bags [bags-within-a-bag]
  (->> (str/split bags-within-a-bag #", ")
       (mapv parse-bag)
       (remove nil?)
       set))
(parse-bags "1 bright white bag, 2 muted yellow bags.")
;; => #{#:bag{:color "muted yellow", :count 2} #:bag{:color "bright white", :count 1}}

(defn parse-rule [rule-line]
  (when-let [[_ bag bags-it-contains] (re-matches #"(.+) bags contain (.+)" rule-line)]
    [bag (parse-bags bags-it-contains)]))
(parse-rule "dotted black bags contain no other bags.")
;; => ["dotted black" #{}]

(def sample-rules 
  (->> (str/split sample-input #"\n") (mapv parse-rule) (into {})))
;; => [{"light red" #{#:bag{:color "muted yellow", :count 2} #:bag{:color "bright white", :count 1}}}
;;     {"dark orange" #{#:bag{:color "muted yellow", :count 4} #:bag{:color "bright white", :count 3}}}
;;     {"bright white" #{#:bag{:color "shiny gold", :count 1}}}
;;     {"muted yellow" #{#:bag{:color "shiny gold", :count 2} #:bag{:color "faded blue", :count 9}}}
;;     {"shiny gold" #{#:bag{:color "vibrant plum", :count 2} #:bag{:color "dark olive", :count 1}}}
;;     {"dark olive" #{#:bag{:color "faded blue", :count 3} #:bag{:color "dotted black", :count 4}}}
;;     {"vibrant plum" #{#:bag{:color "faded blue", :count 5} #:bag{:color "dotted black", :count 6}}}
;;     {"faded blue" #{}}
;;     {"dotted black" #{}}]

;;; finding bags that can cary "shiny gold", maybe trainsitively

(def test-rules (into {} (read-input 7 parse-rule)))

(defn transitive-colors [color-map]
  (into {}
        (mapv
         (fn [[color deps]]
           [color
            (loop [all-deps #{}
                   deps deps]
              (if-let [level2-deps (seq (mapcat #(get color-map (:bag/color %)) deps))]
                (recur (into all-deps (concat deps level2-deps)) level2-deps)
                (into all-deps deps)))])
         color-map)))

(def mym {:a #{:b }
        :b #{:c :d}
        :d #{:f}})
(transitive-colors mym)
;; => {:a #{:c :b :d :f}, :b #{:c :d :f}, :d #{:f}}

(defn count-colors-with-transitive-dep
  [colors-with-deps dep-color]
  (count
   (filter
    (fn [[_color deps]]
      (some #{dep-color} (mapv :bag/color deps)))
    colors-with-deps)))

(time (count-colors-with-transitive-dep
  (transitive-colors test-rules)
  "shiny gold"))
;; "Elapsed time: 553.702664 msecs"
;; => 300

