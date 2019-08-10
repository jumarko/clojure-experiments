(ns clojure-experiments.books.brave-clojure.zombie-metaphysics
  (:require [clojure.string :as str]))

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

(defn- random-quote-words-counts []
  (let [quote (get-random-quote)
        values (count-words-on-quote quote)]
    values))

(random-quote-words-counts)
;; => {"see" 1, "warm" 1, "it" 2, "is" 1, "like" 1, "you" 1, "friendship" 1, "can" 1, "that" 1, "brings" 1, "on" 1, "but" 1, "peeing" 1, "feeling" 1, "yourself" 1, "only" 1, "get" 1, "everyone" 1, "the" 1}

(defn update-words-counts [atomic-counts new-counts]
  (merge-with + atomic-counts new-counts))

(update-words-counts (random-quote-words-counts) {"you" 10})
;; => {"more" 1, "going" 1, "doesn" 1, "garage" 1, "you" 12, "than" 1, "church" 1, "standing" 1, "makes" 1, "any" 1, "make" 1, "a" 3, "t" 1, "christian" 1, "car" 1, "to" 1, "in" 1}

(defn- update-atom
  ([atomic]
   (future (let [words-counts (random-quote-words-counts)]
             (swap! atomic update-words-counts words-counts)))))

(defn quote-word-count
  ([]
   (quote-word-count 1))
  ([n]
   (let [my-atom (atom {})
         ;; this is a lazy seq!
         futures (take n (repeatedly #(update-atom my-atom)))]
     (println @my-atom)
     ;; force futures and wait until they finishes
     (run! deref futures)
     (println @my-atom))))

(comment
  (quote-word-count 5)
  ;;=> prints
  ;; {}
  ;; {happen 1, going 1, his 2, horn 1, s 1, science 1, right 2, made 1, of 1, two 2, annoying 2, not 1, birthday 1, is 2, people 2, you 2, fiction 1, closer 1, those 1, just 1, for 1, screen 1, read 2, my 1, chair 1, recipes 1, already 1, that 1, same 1, go 2, nothing 2, think 1, brakes 1, moved 1, have 3, couldn 1, so 4, interrupting 2, on 2, tvs 1, when 2, t 1, and 1, i 7, one 2, louder 1, big 1, husband 1, talking 2, re 2, well 1, repair 1, your 2, way 1, to 5, get 1, we 1, as 2, end 1, the 3, wanted 1, there 2}
  )

