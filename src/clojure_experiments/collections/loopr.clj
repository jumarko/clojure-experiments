(ns clojure-experiments.collections.loopr
  "Experiments with Aphyr's loopr macro: https://aphyr.com/posts/360-loopr-a-loop-reduction-macro-for-clojure"
  (:require [dom-top.core :refer [loopr]]))

(def bigvec (vec (range 10000000)))

;;; Compute average

;; average with `reduce` - multiple accumulators require destructuring (clunky and less performant)
(time
 (let [[sum count] (reduce (fn [[sum count] x]
                             [(+ sum x) (inc count)])
                           [0 0]
                           bigvec)]
   (/ sum count)))
;; => 999999/2
"Elapsed time: 332.288451 msecs"

;; average with `loop` - cleaner but needs to create unnecessary sequences (`next` calls)
(time
 (loop [sum 0
        count 0
        xs bigvec]
   (if (seq xs)
     (recur (+ sum (first xs))
            (inc count)
            (next xs))
     (/ sum count))
   ))
;; => 999999/2
"Elapsed time: 305.2998 msecs"


;; Average with `loopr`
(time
 (loopr [sum 0 count 0]
        [x bigvec]
        (recur (+ sum x) (inc count))
        (/ sum count)))
;; => 999999/2
"Elapsed time: 152.353879 msecs"



;;; Multi-dimensional reductions
;;; We want to produce a set of all pets
(def people [{:name "zhao"
              :pets ["miette" "biscuit"]}
             {:name "chloe"
              :pets ["arthur meowington the third" "miette"]}])


;; ... with reduce it's ugly -> nested reductions
(reduce (fn [pet-names person]
          (reduce (fn [pet-names pet]
                    (conj pet-names pet))
                  pet-names
                  (:pets person)))
        #{}
        people)
;; => #{"biscuit" "miette" "arthur meowington the third"}


;; ... nested loops are so ugly that I simply skip this part :)

;; ... but `for` is really nice
(for [person people
      pet (:pets person)]
  pet)
;; => ("miette" "biscuit" "arthur meowington the third" "miette")
;; ... except it only produces a seq, not a set
(set (for [person people
           pet (:pets person)]
       pet))
;; => #{"biscuit" "miette" "arthur meowington the third"}

;; ... finally loopr
(loopr [pet-names #{}]
       [person people
        pet (:pets person)]
       (recur (conj pet-names pet)))
;; => #{"biscuit" "miette" "arthur meowington the third"}



;;; Early return - simply omit `recur`

;; e.g. finding a key with a particular value in the map
;; (see also https://stackoverflow.com/questions/18176372/clojure-get-map-key-by-value)
(loopr []
       [[k v] {:x 1 :y 2 :z 3 :a 10}]
       (if (= v 2) k (recur))
       :not-found)
;; => :y
(loopr []
       [[k v] {:x 1 :y 2 :z 3 :a 10}]
       (if (= v 4) k (recur))
       :not-found)
;; => :not-found

;; and getting all the elements with such  value is easy tool (but this is not "early return")
(loopr [matching-keys #{}]
       [[k v] {:x 1 :y 2 :z 2 :a 10 :b 2 :c 100}]
       (if (= v 2)
         (recur (conj matching-keys k))
         (recur matching-keys)))
;; => #{:y :z :b}
