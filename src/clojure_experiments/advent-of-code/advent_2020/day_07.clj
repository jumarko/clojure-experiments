(ns clojure-experiments.advent-of-code.advent-2020.day-07
  "https://adventofcode.com/2020/day/7
  Input: https://adventofcode.com/2020/day/7/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.string :as str]
            [clojure.set :as set]
            [com.stuartsierra.dependency :as dep]))

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


;;; part 2: how many individual bags are required inside your single shiny gold bag?
(defn count-required-bags
  [rules bag-color]
  (let [transitives (transitive-colors rules)]
    (->> (get transitives bag-color)
         (mapv :bag/count)
         (apply +))))

(def sample2-input
  "shiny gold bags contain 2 dark red bags.
dark red bags contain 2 dark orange bags.
dark orange bags contain 2 dark yellow bags.
dark yellow bags contain 2 dark green bags.
dark green bags contain 2 dark blue bags.
dark blue bags contain 2 dark violet bags.
dark violet bags contain no other bags.")
(def sample2-rules
  (->> (str/split sample-input #"\n") (mapv parse-rule) (into {})))

(count-required-bags sample2-rules "shiny gold")
;; Should be 126!
;; => 21


;;; So the above approach doesn't work because it "flattens"
;;; the dependencies and thus we cannot properly multiply
;;; the bags counts.
;;; Instead we need to preserve the graph structure
;;; and only compute counts on demand


(defn subgraph [color-map node]
  (lazy-seq
   (if-let [deps (seq (get color-map (:bag/color node)))]
     (map (fn [dep] (cons dep (subgraph color-map dep)))
          deps)
     nil)))
(subgraph sample-rules #:bag{:color "shiny gold"})
;; => ((#:bag{:color "vibrant plum", :count 2}
;;      (#:bag{:color "faded blue", :count 5})
;;      (#:bag{:color "dotted black", :count 6}))
;;     (#:bag{:color "dark olive", :count 1}
;;      (#:bag{:color "faded blue", :count 3})
;;      (#:bag{:color "dotted black", :count 4})))

(defn make-graph [color-map]
  (into {} (map (fn [[color _]]
                  [color (subgraph color-map #:bag{:color color})])
                color-map)))

(def bag-graph (make-graph sample-rules))

;;; Constructing the graph was an interesting exercise
;;; But I don't need that!
;;; It's much easier to just count the dependencies as we go
(defn count-required-bags2
  [rules {:bag/keys [color count] :as bag}]
  (let [inner-bags-count
        (->> (get rules (:bag/color bag))
             (mapv (fn [{:bag/keys [_ count] :as inner-bag}]
                     (count-required-bags2 rules inner-bag)))
             (apply +))]
    (if (zero? inner-bags-count)
      count
      (+ count (* count inner-bags-count)))))
;; => #'clojure-experiments.advent-of-code.advent-2020.day-07/count-required-bags2

(dec (count-required-bags2 sample2-rules #:bag {:color "shiny gold"
                                            :count 1}))
;; => 32
(time (dec (count-required-bags2 test-rules #:bag {:color "shiny gold"
                                              :count 1})))
;; "Elapsed time: 0.308957 msecs"
;; => 8030
