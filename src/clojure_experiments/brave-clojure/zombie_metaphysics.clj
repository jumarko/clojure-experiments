(ns clojure-experiments.brave-clojure.zombie-metaphysics
  (require [clojure.string :as str]))

(def example-1 "Before I got married I had six theories about bringing up children; now I have six children and no theories.\n-- John Wilmot (Earl of Rochester) \n")
(def example-2 "At every party, there are two kinds of people-those who want to go home and those who don't. The trouble is, they are usually married to each other.\n-- Ann Landers \n")
(def example-3 "When opportunity knocks, some people are in the backyard looking for four-leaf clovers.\n-- Polish Proverb \n")

(def word-splitter #"[^a-zA-Z0-9]")

(defn- get-words
  ([phrase]
   (remove str/blank?
           (str/split phrase word-splitter))))

(defn- get-random-quote
  ([]
   (slurp "https://www.braveclojure.com/random-quote")))

(defn- count-words-on-quote
  ([quote]
   (-> quote
       str/split-lines
       first
       str/lower-case
       get-words
       frequencies)))

(defn- update-atom
  ([atomic]
   (future (let [quote (get-random-quote)
                 values (count-words-on-quote quote)]
             (println quote)
             (swap! atomic (partial merge-with + values))))))

(defn quote-word-count
  ([]
   (quote-word-count 1))
  ([n]
   (let [update (partial update-atom (atom {}))]
     (take n (repeatedly update)))))

;	the line below is required for the exercise
(quote-word-count 5)
