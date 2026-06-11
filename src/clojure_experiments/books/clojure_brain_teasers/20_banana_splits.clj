(ns clojure-experiments.books.clojure-brain-teasers.20-banana-splits
  (:require [clojure.string :as str]))

(= (str/split "banana" #"an") ["b" "" "a"])
;; => true

;; You might have expected ["ba" "" ""], but the default behavior of split omits
;; trailing empty string parts.
(= (str/split "banana" #"na") ["ba"])
;; => true

;; you can also use the `limit` argument N to make it split at most N-1 times
(str/split "banana" #"an" 1)
;; => ["banana"]
(str/split "banana" #"an" 2)
;; => ["b" "ana"]
(str/split "banana" #"an" 3)
;; => ["b" "" "a"]
(str/split "banana" #"an" 4)
;; => ["b" "" "a"]


(str/split "banana" #"na" 2)
;; => ["ba" "na"]
;; Notice that when providing the limit, it doesn't remove trailing empty parts!
(str/split "banana" #"na" 3)
;; => ["ba" "" ""]
;; ... this is the same behavior as when passing -1
(str/split "banana" #"na" -1)
;; => ["ba" "" ""]
;; ... but with 0 it gets removed as without the limit argument
(str/split "banana" #"na" 0)
;; => ["ba"]


;;; `split-lines`
(str/split-lines "a\nb\nc\n\n")
;; => ["a" "b" "c"]

;; Because ‘split-lines‘ is built on ‘split‘, it has the same behavior in dropping
;; trailing empty lines. 
(->> "a\nb\nc\n\n"
     str/split-lines
     (map str/upper-case)
     (str/join "\n"))
;; => "A\nB\nC"

;; to get trailing empty lines you can create your own function
(defn split-lines-trailing [s]
  (str/split s #"\n" -1))
(->> "a\nb\nc\n\n"
     split-lines-trailing
     (map str/upper-case)
     (str/join "\n"))
;; => "A\nB\nC\n\n"
