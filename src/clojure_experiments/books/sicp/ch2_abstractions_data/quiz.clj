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

;; Ex. 2.52 (p. 141)


;;; Ex. 2.45 general `split` procedure (p. 134)
;;; `right-split` and `up-split` can be expressed as instances of `split`:
;;; TODO: define the `split` procedure
(defn split [orig-placer split-placer]
  ,,,)

(def right-split (split beside below))
(def up-split (split below beside))



;; Ex. 2.57 (p. 151)
;; Expand the differentiation program to handle sums and products of arbitrary numbers of two or more terms.
;; Try to do this by changing only the representation for sums and products.
;; e.g. the last example in the book (p. 150) could be expressed as:
(deriv '(* x y (+ x 3)) 'x)
;; instead of
(deriv '(* (* x y) (+ x 3)) 'x)
;; => (+ (* x y) (* (+ x 3) y))

(deriv '(* x y (+ x 3)) 'x)
;; Currently returns: y
;; updated version returns:
;; => (+ (* x y) (* y (+ x 3)))

(defn augend [e]
  ,,,)

(defn multiplicand [e]
  ,,,)

;; try again
#_(assert (= (deriv '(* x y (+ x 3)) 'x)
           '(+ (* x y) (* y (+ x 3)))))

;; POSSIBLY Ex. 2.58 (P. 151)
;; Infix notation
;; Originally SKIPPED.


;; Ex. 2.65 (p. 160)
;; Use the results of exercises 2.63 and 2.64 to give O(n) implementations
;; of `union-set` and `intersection-set`.

;; https://wizardbook.wordpress.com/2010/12/07/exercise-2-65/?unapproved=1657&moderation-hash=994fdf7556d04e34484c5928a38eac3e#comment-1657
;; http://community.schemewiki.org/?sicp-ex-2.65

;; helper functions
(defn entry [[e _l _r]] e)
(defn left-branch [[_e l _r]] l)
(defn right-branch [[_e _l r]] r)
(defn empty-set [] [])
(defn make-tree [e left right]
  ;; basic invariant check
  (assert (< (or (entry left) (dec e))
             e
             (or (entry right) (inc e)))
          (format "Elements in the left branch must be < entry and entry < elements in the right branch: entry=%s, left=%s, right=%s" e left right))
  (list e left right))
(defn adjoin-set [x s]
  (cond
    (empty? s) (make-tree x () ())

    (= x (entry s)) s

    (< x (entry s)) (make-tree (entry s)
                               (adjoin-set x (left-branch s))
                               (right-branch s))
    (> x (entry s)) (make-tree (entry s)
                               (left-branch s)
                               (adjoin-set x (right-branch s)))))
;; test data:
(def my-set (make-tree  3
                        (make-tree 1 nil nil)
                        (make-tree 5 nil nil)))

(def my-set-2 (->> (make-tree  1 nil nil)
                   (adjoin-set 0)
                   (adjoin-set 10)
                   (adjoin-set 4)
                   (adjoin-set -2)))
;; TODO union-set

;; TODO intersection-set


;;; Huffman encoding - implement `decode` function!
;; Functions for Representing Huffman trees (p. 164/5) provided here:

(defn make-leaf [symbol weight]
  ;; I'd probably use `:leaf` in Clojure instead of the symbol 'leaf
  (list 'leaf symbol weight))

(defn leaf? [obj]
  (= 'leaf (first obj)))

;; Note destructing throws an exception if it's not a list/vector 
(defn symbol-leaf [[_ s _]]
  s)

(defn weight-leaf [[_ _ w]]
  w)

;; Note: here I could use real clojure sets instead of plain lists
;; but I'm following with the book...
(defn left-branch [tree]
  (first tree))

(defn right-branch [tree]
  (second tree))

;; Notice that `symbols` and `weight` are "generic"
;; -> they must do something slightly different when dealing with leaves vs trees.

(defn symbols [tree]
  (if leaf? tree
      (list (symbol-leaf tree))))

(defn weight [tree]
  (if (leaf? tree)
    (weight-leaf tree)
    (nth tree 3)))

;; tree is (left branch, right branch, symbols, total weight)
(defn make-code-tree [left right]
  (list left
        right
        (concat (symbols left) (symbols right))
        (+ (weight left) (weight right))))

;; Now we have basic representation we can implement decoding

;; taken from p. 162 (at the top) 
(def message "100010100101101100011010100100000111001111")
;; for tree, see figure 2.18 on p. 163
(def hf-tree (make-code-tree
              (make-leaf 'A 8)

              ;; BCDEFGH branch
              (make-code-tree

               ;; BCD left branch
               (make-code-tree
                (make-leaf 'B 3)
                (make-code-tree
                 (make-leaf 'C 1)
                 (make-leaf 'D 1)))

               ;; EFGH right branch
               (make-code-tree
                (make-code-tree
                 (make-leaf 'E 1)
                 (make-leaf 'F 1))
                (make-code-tree
                 (make-leaf 'G 1)
                 (make-leaf 'H 1))))))
(clojure.pprint/pprint hf-tree)

;; TODO
(defn decode [bits tree]

  ;; this helper fn serves the purpose to capture the original complete tree
  ;; to use it during decoding
  (letfn [(decode-1 [bits current-branch]
            ;; TODO
            ,,,
            )]

    (decode-1 bits tree)))


(assert (= '(B A C A D A E A F A B B A A A G A H)
           (decode message hf-tree)))

;; Ex. 2.69 (p. 168)
;; Finish given `generate-huffman-tree` procedure
;; by providing implementation of `successive-merge` (using `make-code-tree` under the hood)
;; http://community.schemewiki.org/?sicp-ex-2.69
; https://wizardbook.wordpress.com/2010/12/07/exercise-2-69/
(defn- successive-merge [ordered-set]
  ,,,)

(defn adjoin-set [x s]
  (cond
    (empty? s) (list x)
    (< (weight x) (weight (first s))) (cons x s)
    :else (cons (first s) (adjoin-set x (rest s)))))

(defn make-leaf-set [[fst & rst :as pairs]]
  (if (empty? pairs)
    ()
    (let [[symbol frequency] fst]
      (adjoin-set (make-leaf symbol frequency)
                  (make-leaf-set rst)))))

(defn generate-huffman-tree [pairs]
  (successive-merge (make-leaf-set pairs)))

;; testing data
(def my-pairs [['A 4] ['B 2] ['C 1] ['D 1]])
(def my-hf-tree (make-code-tree
                 (make-leaf 'A 4)
                 (make-code-tree
                  (make-leaf 'B 2)
                  (make-code-tree
                   (make-leaf 'D 1)
                   (make-leaf 'C 1)))))
(make-leaf-set my-pairs)
;; => ((leaf D 1) (leaf C 1) (leaf B 2) (leaf A 4))

my-hf-tree
;; => ((leaf A 4) ((leaf B 2) ((leaf D 1) (leaf C 1) (D C) 2) (B D C) 4) (A B D C) 8)

(generate-huffman-tree my-pairs)
;; => (((leaf A 4) ((leaf B 2) ((leaf D 1) (leaf C 1) (D C) 2) (B D C) 4) (A B D C) 8))

(assert (= my-hf-tree
           (generate-huffman-tree my-pairs)))

(generate-huffman-tree ())
;; => nil
