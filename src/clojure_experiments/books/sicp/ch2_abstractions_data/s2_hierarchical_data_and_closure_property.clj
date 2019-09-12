(ns clojure-experiments.books.sicp.ch2-abstractions-data.s2-hierarchical-data-and-closure-property
  "This deals with exampels from the section 2.2 - pages 97 - 141?
  TODO: maybe separate the Picture Language exercise?"
  (:require [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :refer [square]]))

;;;; ----------------------------
;;;; 2.2.1.Representing sequences
;;;; ----------------------------

;;; Sequence is an "ordered collection of data objects" (p. 99)
(def one-through-four (list 1 2 3 4))

one-through-four
;; => (1 2 3 4)

;; 'car'
(first one-through-four)
;; => 1

;; 'cdr'
(rest one-through-four)
;; => (2 3 4)


;; 'cadr' = (car (cdr ...))
(fnext one-through-four)
;; => 2

;; cons
(cons 10 one-through-four)
;; => (10 1 2 3 4)


;; `list-ref`: the same thing as `nth` (p. 101)
(defn list-ref [items n]
  (if (zero? n)
    (first items)
    (recur (rest items) (dec n))))
(list-ref one-through-four 0)
;; => 1
(list-ref one-through-four 3)
;; => 4
(list-ref one-through-four 5)
;; => nil

(defn length [items]
  (loop [len 0
         items items]
    (if items
      ;; this is the "reduction" step
      (recur (inc len) (next items))
      len)))
(time (length (range 100000)))
;; => 100000
;; "Elapsed time: 4.061916 msecs"

(def squares '(1 4 9 16 25))
(def odds '(1 3 5 7 ))

(defn append [l1 l2]
  (if l1
    (cons (first l1) (append (next l1) l2))
    l2))

(append squares odds)  
;; => (1 4 9 16 25 1 3 5 7)

#_(take 10 (append (range 1000000) (range 10)))
;; StackOverflowError

;; My attempt to "fix" Stack Overflow...
(defn append2 [l1 l2]
  (loop [l2 l2
         acc (vec l1)]
    (if l2
      (recur (next l2) (conj acc (first l2)))
      acc)))
(take 10 (append2 (range 1000000) (range 10)))
;; => (0 1 2 3 4 5 6 7 8 9)



;;; Ex. 2.17 (p. 103)
;;; Define `last-pair` that returns the list that contains only the last element of a given nonempty list
(defn last-pair [l]
  (let [n (next l)]
    (if n
      (last-pair n)
      (first l))))
(last-pair (range 10))
;; => 9


;;; Ex. 2.18 (p. 103)
;;; Define a procedure `reverse` (note that it's also a clojure.core fn)
(defn my-reverse [l]
  (loop [acc ()
         l l]
    (if l
      (recur (cons (first l) acc) (next l))
      acc)))
(my-reverse squares)
;; => (25 16 9 4 1)


;;; Ex. 2.19 (p. 103-104)
;;; Using change-counting program from p. 40-41 make it more flexible by accepting any currency

;; This is the original program from page 41
(defn- first-denomination [kinds-of-coins]
  (condp = kinds-of-coins
    1 1
    2 5
    3 10
    4 25
    5 50))

(defn change
  ([amount] (change amount 5))
  ([amount kinds-of-coins]
   (cond
     (zero? amount)
     1

     (or (neg? amount) (zero? kinds-of-coins))
     0

     :else
     (+ (change amount
                (dec kinds-of-coins))
        (change (- amount (first-denomination kinds-of-coins))
                kinds-of-coins)))))
(change 100)
;; => 292

;; Now modify it to accept arbitrary currency
(def us-coins [50 25 10 5 1])
(def uk-coins [100 50 20 10 5 2 1 0.5])

(defn- first-denomination [coins-values]
  (first coins-values))

(defn- except-first-denomination [coins-values]
  (rest coins-values))

(defn- no-more? [coins]
  (empty? coins))

(defn change
  ([amount] (change amount us-coins))
  ([amount coins-values]
   (cond
     (zero? amount)
     1

     (or (neg? amount) (no-more? coins-values))
     0

     :else
     (+ (change amount
                (except-first-denomination coins-values))
        (change (- amount (first-denomination coins-values))
                coins-values)))))
(change 100)
;; => 292
(change 100 uk-coins)
;; => 104561


;;; Mapping over lists (p. 105)
(defn scale-list [items factor]
  (when-not (empty? items)
    (cons (* factor (first items))
          (scale-list (rest items) factor))))
(scale-list (range 10) 3)
;; => (0 3 6 9 12 15 18 21 24 27)

;; Now with `map` we can do better
(defn my-map [f items]
  (when-not (empty? items)
    (cons (f (first items))
          (my-map f (rest items)))))
(my-map (partial * 3) (range 10))
;; => (0 3 6 9 12 15 18 21 24 27)

;; of course that map implementation is very rough, at least it should work for large lists
#_(take 10 (my-map (partial * 3) (range 100000)))
(defn my-map [f items]
  (loop [items items
         acc []]
    (if (empty? items)
      acc
      (recur (rest items)
             (conj acc (f (first items)))))))
(take 10 (my-map (partial * 3) (range 100000)))
;; => (0 3 6 9 12 15 18 21 24 27)


;;; Ex. 2.21 (p. 106)
(defn square-list [l]
  (if (empty? l)
    (empty l)
    (cons (square (first l))
          (rest l))))
(square-list '(1 2 3 4))
;; => (1 2 3 4)

(defn square-list [l]
  (map square l))
(square-list '(1 2 3 4))
;; => (1 4 9 16)


;;; Ex. 2.22
;;; Writes the `square-list` iterative variant
(defn square-list-iter [items]
  (letfn [(iter [things answer]
            (if (empty? things)
              answer
              (iter (rest things)
                    (cons (square (first things))
                          answer))))]
    (iter items '())))
(square-list-iter '(1 2 3 4))
;; => (16 9 4 1)
;; this happens because we're prepending to the list as we traverse over the list
;; so the last element 4 will be squared and added as the first element to the 'answer'

;; Now Louis tries to fix the bug...
(defn square-list-iter [items]
  (letfn [(iter [things answer]
            (if (empty? things)
              answer
              (iter (rest things)
                    (cons answer
                          (square (first things))))))]
    (iter items '())))
;; in clojure this throws an exception
#_(square-list-iter '(1 2 3 4))


;;; Ex. 2.23 (p. 107)
(defn for-each [f items]
  (doall (map f items))
  true)
(for-each println '(1 2 3 4));; => true
