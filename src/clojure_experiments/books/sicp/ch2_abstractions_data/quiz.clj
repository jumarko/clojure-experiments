(ns clojure-experiments.books.sicp.ch2-abstractions-data.quiz
  "Exercises to practice the most important ideas from Chapter 2.")

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


