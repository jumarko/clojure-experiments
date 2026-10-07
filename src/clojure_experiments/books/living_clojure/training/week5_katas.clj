(ns clojure-experiments.books.living-clojure.training.week5-katas
  "See https://github.com/gigasquid/wonderland-clojure-katas/tree/master/doublets.
  Check words.edn"
  (:require [clojure.edn :as edn]
            [clojure.set :as set]))

(def dictionary (edn/read-string (slurp "src/clojure_experiments/books/living_clojure/training/words.edn")))
;;; Heuristic - some ideas:

;; 1. only consider words of the same length
(filter #(= 4 (count %))
        dictionary)
;; => ("muta"
;;     "task"
;;     "quat"
;;     "head"
;;     "heal"
;;     "teal"
;;     "tell"
;;     "tall"
;;     "tail"
;;     "door"
;;     "boor"
;;     "book"
;;     "look"
;;     "lock"
;;     "bank"
;;     "bonk"
;;     "loon"
;;     "loan")


;; 2. only consider words that have at least one letter common with the starting word (at the same position)

(set/intersection (set "book") (set "door"))



;; Q: What's the simplest thing to do?
;; Just try all the plausible words
;; Those would be ones that have at least as many letters common with the target word as the original word
;; Make sure to try each of the words only once.

(defn word-diff [w1 w2]
  (count (filter false? (map #(= %1 %2) w1 w2))))

(word-diff "door" "boor")
;; => 1
(word-diff "book" "door")
;; => 2

(defn candidates [w1 w2]
  (->> dictionary
       ;; remove w1, w2 themselves
       (remove #{w1 w2})
       ;; words of the same length
       (filter #(= (count w1) (count %)))
       ;; whose diff isn't greater than diff between w1 and w2
       ;;(filter #(<= (word-diff w1 w2) (word-diff w1 %)))
       ;; actually, just consider word-diff one
       (filter (fn [candidate]
                 (and (= 1 (word-diff w1 candidate)) ; valid transformation are only those that change a single letter
                      ;; NOTE: originally, I thought this is a good idea but it prevents us from finding valid transitions
                      #_(<= (word-diff candidate w2) ; at the same time, we don't wanna try words that are "further away" from the target than w1 is.
                          (word-diff w1 w2)))))
       ;; give preference to those words that move us closer to the target
       ;; TODO: does it work properly?
       #_(sort #(- (word-diff %2 w2)
                 (word-diff %1 w2)))))

(let [w1 "door"
      w2 "lock"
      candidates (candidates w1 w2)]
  candidates)
;; => ("boor")


;; Now, we could just try each of the candidates one-by-one and see where that goes.
;; The trouble is that there can be still many possibilities and we need to track what we tried.

(defn doublets [start end]
  (loop [word start
         steps [start]
         max-steps 100]
    (let [next-word (first (remove
                            (set steps) ; remove those that we already tried
                            (candidates word end)))]
      (if (or (= next-word end) (nil? next-word) (zero? max-steps))
        (conj steps end)
        (recur next-word (conj steps next-word) (dec max-steps))))))

(doublets "door" "lock")
;; => ["door" "boor" "book" "bonk" "lock"]

(doublets "bank" "loan") ;; TODO: doesn't work - no candidate reached
;; => ["bank" "bonk" "book" "boor" "door" "loan"]


(doublets "wheat" "bread")

;; Let's try again - first candidates will only take 1 word
(defn candidates [word]
  (->> dictionary
       (remove #{word})
       ;; words of the same length
       (filter #(= (count word) (count %)))
       (filter (fn [candidate]
                 (= 1 (word-diff word candidate)) ; valid transformation are only those that change a single letter
                 ))))
(candidates "door")
;; => ("boor")
(candidates "boor")
;; => ("door" "book")

;; Now, let's try with tree-seq -> generate all the possible paths
;; and traverse them
(tree-seq (fn continue? [[word _]]
            (and (some? word)
                 (not= word "lock")))
          (fn next-step [[word processed]]
            (remove processed (candidates word)))
          ["door" #{"door"}])

(sequential? [1])

