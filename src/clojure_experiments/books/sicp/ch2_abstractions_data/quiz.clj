(ns clojure-experiments.books.sicp.ch2-abstractions-data.quiz
  "Exercises to practice the most important ideas from Chapter 2."
  (:require [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :refer [square]]
            [clojure-experiments.books.sicp.ch2-abstractions-data.s2-hierarchical-data-and-closure-property :refer [accumulate]]))

;;; Section 2.1 Data Abstraction and Abstraction Barriers
;;; Describe how would you implement operations on rational numbers
;;; so that API clients don't need to worry about underlying implementation.
;;; Then, try changing the implementation (e.g. pairs -> maps) and make sure it still works.


;; Ex. 2.6. Church numerals (p. 93)
;; How do you represent them and how you can use them
;; If stuck see http://community.schemewiki.org/?sicp-ex-2.6

(comment
  
  (defn zero ,,,)
  (defn one ,,,)
  (defn two ,,,)
  (defn three ,,,)

  (defn church-to-int [church-number] ,,,)

  (defn add-one ,,,)

  (defn plus ,,,))


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


;;; Ex. 2.34 (p.119)
;;; Evaluate polynomial using Horner's rule and `accumulate` function
(defn horner-eval [x coefficient-seq]
  (accumulate
   ,,,
   coefficient-seq
   ))

#_(horner-eval 2 '(1 3 0 5 0 1))
;; => 79



;;; Ex. 2.36 and 2.37 (p.120)
;;; Write accumulate-n which can accept multiple sequences in the last arg (sequence of seqs)
;;; Optional: Then use this to define matrix operation function(s): dot-product, matrix-*-vector, transpose, matrix-*-matrix
;;;            (this is not that valuable and quite time-consuming!)
(defn accumulate-n [op init seqs]
  (if (nil? (first seqs))
    ()
    (cons ,,,)))
#_(accumulate-n
 +
 0
 [[1 2 3]
  [4 5 6]
  [7 8 9]
  [10 11 12]])
;; => (22 26 30)


;;; Picture language (p. 132)
;;; How would you define procedure `flipped-pairs` in terms of `square-of-four`?

;; this "traditional" definition:
(defn flipped-pairs [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

;; this is `square-of-four
(defn square-of-four [tl tr bl br]
  (fn sof [painter]
    (let [top (beside (tl painter) (tr painter))
          bottom (beside (bl painter) (br painter))]
    (below bottom top))))

;; now the new definition???



;;; Ex. 2.45 general `split` procedure (p. 134)
;;; `right-split` and `up-split` can be expressed as instances of `split`:
;;; TODO: define the `split` procedure
(defn split [orig-placer split-placer]
  ,,,)

(def right-split (split beside below))
(def up-split (split below beside))
