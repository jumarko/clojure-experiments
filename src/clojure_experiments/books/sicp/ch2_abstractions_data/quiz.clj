(ns clojure-experiments.books.sicp.ch2-abstractions-data.quiz
  "Exercises to practice the most important ideas from Chapter 2."
  (:require [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :refer [square]]))

;;; Section 2.1 Data Abstraction and Abstraction Barriers
;;; Describe how would you implement operations on rational numbers
;;; so that API clients don't need to worry about underlying implementation.
;;; Then, try changing the implementation (e.g. pairs -> maps) and make sure it still works.


;; Ex. 2.6. Church numerals (p. 93)
;; How do you represent them and how you can use them
;; If stuck see http://community.schemewiki.org/?sicp-ex-2.6

(defn zero ,,,)
(defn one ,,,)
(defn two ,,,)
(defn three ,,,)

(defn church-to-int [church-number] ,,,)

(defn add-one ,,,)

(defn plus ,,,)


;;; Section 2.2 Hierarchical Data Structures and 'Closure' property

;; What is a 'sequence'

;; Ex. 2.18 (p. 103)
;; Write the 'reverse' function


;; Ex. 2.25 (p. 110)
;; Give the combination of first/rest (car/cdr) which will extract 7 from the following list
(-> '(1 (2 (3 (4 (5 (6 7)))))))



;; Ex. 2.27 (p. 110)
;; and/or consider implementing 2.28 (fringe)
(defn deep-reverse [l]
  )
(def x '((1 2) (3 4)))
(deep-reverse x)
;; => ((4 3) (2 1))


;; Ex. 2.29 (p. 111)
;; binary mobile


;;; Ex. 2.31 (p.113)
;;; Abstract 2.30 to a higher-order procedure `tree-map`:
;;; so you can define `square-tree` easily using this `tree-map` procedure:
;;; Also try to implement `square-tree` using `clojure.core/tree-seq`

(defn tree-map [f tree]
  ,,,)

(defn square-tree2 [tree]
  (tree-map square tree))

(square-tree2 '(1 (2 (3 (4 (5 (6 7)))))))
;; => (1 (4 (9 (16 (25 (36 49))))))


;;; Section 2.2.3 Sequences as conventional interfaces (p. 113 - 117)
;;; Demonstrate how you can implement sum-odd-squares (operating on a tree)
;;; and even-fibs (return even fibonacci numbers up to given n) using "signal-flow" plans
;;; Do it as a pipeline of stages.
(defn sum-odd-squares [tree])

(defn- fibr [n]
  (cond
    (zero? n) 0
    (= 1 n) 1
    (< 1 n) (+ (fibr (- n 1))
               (fibr (- n 2)))))
(defn even-fibs [n])
