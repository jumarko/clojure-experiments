(ns clojure-experiments.books.sicp.ch2-abstractions-data.s2-hierarchical-data-and-closure-property
  "This deals with exampels from the section 2.2 - pages 97 - 141?
  See also s2_picture_language.clj for the Picture Language exercises."
  (:require [clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :refer [square]]
            [clojure-experiments.books.sicp.ch1-abstractions-procedures.s2-procedures-and-processes :refer [prime?]]
            ))

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
  (if (seq l1)
    (cons (first l1) (append (rest l1) l2))
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


;;;; ----------------------------
;;;; 2.2.2 Hierarchical Structures (Trees - p. 107)
;;;; ----------------------------

;;; recursion is a natural way to deal with trees
;;; we perform operation on branches, then on branches of the branches, ..., until we reach leaves.

;; example: count-leaves
(defn count-leaves [l]
  (cond
    (empty? l) 0
    (seqable? (first l)) (+ (count-leaves (first l)) (count-leaves (rest l)))
    :leaf (+ 1 (count-leaves (rest l)))))
    
(count-leaves '(((1 2) 3 4)
                ((1 2) 3 4)))
;; => 8


;;; Ex. 2.224 (p. 110)
;;; We want to evaluate (list 1 (list 2 (list 3 4)))
(list 1 (list 2 (list 3 4)))
;; => (1 (2 (3 4)))

;; box-and-pointer representation: check http://community.schemewiki.org/?sicp-ex-2.24
;; [] - [] - [] - [] - nil
;;  1    2 -  3    4

;; tree representation
;;(1 (2 (3 4)))
;; |   |
;; 1 (2 (3 4))
;;        |
;;       3 4


;;; Ex. 2.25 (p. 110)
;;; Give combinations of car and cdr which will pick 7 from following lists
(-> '(1 2 (5 7) 9)
    rest
    rest
    first
    rest
    first)
;; => 7

(-> '((7))
    first
    first)
;; => 7

;; THIS IS TRICKY!
(-> '(1 (2 (3 (4 (5 (6 7))))))
    rest
    first
    rest
    first
    rest
    first
    rest
    first
    rest
    first
    rest
    first
    )
;; => 7


;;; Ex. 2.26 (p. 110)
;;; What result is printed when invoking the expressions?
(def x (list 1 2 3))
(def y (list 4 5 6))

(append x y)
;; => (1 2 3 4 5 6)

(cons x y)
;; => ((1 2 3) 4 5 6)

(list x y)
;; => ((1 2 3) (4 5 6))


;;; Ex. 2.27 (p. 110)
;;; Modify reverse procedue to do a "deep reverse"
(def x '((1 2) (3 4)))
(reverse x)
;; => ((3 4) (1 2))
#_(deep-reverse x)
;; => ((4 3) (2 1))

;; not this is hard to optimize for tail call?
;; Well, actually it seems that my implementation is ok, just cannot process _very_ deep trees
;; but can handle a top-level list with lots of elements
(defn deep-reverse [l]
  (loop [acc ()
         l l]
    (if (empty? l)
      acc
      (if (seqable? (first l))
        ;; we need to go deep
        (recur (cons (deep-reverse (first l)) acc) (next l))
        ;; simple element, just cons
        (recur (cons (first l) acc) (next l))))))
(deep-reverse x)
;; => ((4 3) (2 1))

(count (deep-reverse (concat x (range 100000))))


;;; Ex. 2.28 (p. 111)
;;; `fringe` returns all the leaves of the tree
;;; Check also solutions: http://community.schemewiki.org/?sicp-ex-2.28
(defn fringe [l]
  (loop [leaves []
         l l]
    (if (empty? l)
      leaves
      (if (seqable? (first l))
        ;; we need to go deep
        (recur (append (fringe (first l)) leaves)
               (next l))
        ;; just found a leaf!
        (recur (conj leaves (first l)) (next l))))))
(fringe x)
;; => (3 4 1 2)

(fringe '(1 (2 (3 (4 (5 (6 7)))))))
;; => (6 7 5 4 3 2 1)


;; From solutions: http://community.schemewiki.org/?sicp-ex-2.28
(defn fringe [tree] 
  (cond (nil? tree) nil 
        (not (seqable? tree)) (list tree) 
        :else (append (fringe (first tree)) (fringe (next tree))))) 
;; works on a simple list
(fringe x)
;; => (1 2 3 4)

(fringe '(1 (2 (3 (4 (5 (6 7)))))))
;; => (1 2 3 4 5 6 7)


;;; Ex. 2.29 left out



;;; Mapping over trees (p. 112)
;;; map together with a recursion if a powerful abstraction for dealing with trees

;; start with `scale-tree` implementation:
(defn scale-tree [tree factor]
  ;; `seq?` is enough since we use `next`
  (cond
    (nil? tree) nil

    (number? tree) (* tree factor)

    :branch
    (cons (scale-tree (first tree) factor)
          (scale-tree (next tree) factor))))

(scale-tree '(1 (2 (3 (4 (5 (6 7))))))
            2)
;; => (2 (4 (6 (8 (10 (12 14))))))


;; alternative implementation of `scale-tree` will use `map` and recursion

(defn scale-tree-map [tree factor]
  (map
   (fn [sub-tree]
     (if (seq? sub-tree)
       (scale-tree-map sub-tree factor)
       (* sub-tree factor)))
   tree)
  )
(scale-tree-map '(1 (2 (3 (4 (5 (6 7))))))
                2)
;; => (2 (4 (6 (8 (10 (12 14))))))


;;; Ex. 2.30 (p. 112)
;;; Define square-tree similar to square-list
;;; Show both `map`-based and recursion-based implementation.

(defn square-tree [tree]
  (cond
    (nil? tree) nil
    (number? tree) (square tree)
    :branch (cons (square-tree (first tree))
                  (square-tree (next tree)))))
(square-tree '(1 (2 (3 (4 (5 (6 7)))))))
;; => (1 (4 (9 (16 (25 (36 49))))))

(defn square-tree-map [tree]
  (map
   (fn [sub-tree]
     (if (seq? sub-tree)
       (square-tree-map sub-tree)
       (square sub-tree)))
   tree))
(square-tree-map '(1 (2 (3 (4 (5 (6 7)))))))
;; => (1 (4 (9 (16 (25 (36 49))))))


;;; Ex. 2.31 (p.113)
;;; Abstract 2.30 to a higher-order procedure `tree-map`:
;;; so you can define `square-tree` easily using this `tree-map` procedure:
(defn tree-map [f tree]
  (map
   (fn [sub-tree]
     (if (seq? sub-tree)
       (tree-map f sub-tree)
       (f sub-tree)))
   tree))

(defn square-tree2 [tree]
  (tree-map square tree))

(square-tree2 '(1 (2 (3 (4 (5 (6 7)))))))
;; => (1 (4 (9 (16 (25 (36 49))))))

;; the same should work with `clojure.core/tree-seq`
;; ???
(defn square-tree3 [tree]
  (map square (tree-seq seq? seq tree)))

;; doesn't work:
(tree-seq seq? seq '(1 (2 (3 (4 (5 (6 7)))))))
(square-tree3 '(1 (2 (3 (4 (5 (6 7)))))))

;;; Ex. 2.32 (p.113)
;;; Set of all subsets
(defn subsets [s]
  (let [xs (set s)]
    (if (empty? xs)
      #{#{}} ; this is an important bit - set containing an empty set, NOT JUST #{} !!! 
      (let [rst (subsets  (next xs))]
        ;; `append` doesn't really work for us because it produces `(nil)`
        (clojure.set/union rst (set (map (fn [subset-for-remaining-elems]
                                (conj subset-for-remaining-elems
                                      (first xs)))
                              rst)))))))

(subsets #{1 2 3 })
;; => #{#{} #{3} #{2} #{1} #{1 3 2} #{1 3} #{1 2} #{3 2}}



;;;; ----------------------------
;;;; 2.2.3. Sequences as conventional interfaces (p. 113)
;;;; ----------------------------

;;; consider this implementation similar to count-leaves which sums squares of leaves that are odd:
(defn sum-odd-squares [tree]
  (cond
    (number? tree) (if (odd? tree) (square tree) 0)
    (empty? tree) 0
    :branch (+ (sum-odd-squares (first tree))
               (sum-odd-squares (rest tree)))))
(sum-odd-squares '(1 (2 (3 (4 (5 (6 7)))))))
;; => 84

;;; Compare `sum-odd-squares` to `even-fibs` which constructs a list of all even fibonacci numbers

;; (copied from Chapter 1)
(defn fibr [n]
  (cond
    (zero? n) 0
    (= 1 n) 1
    (< 1 n) (+ (fibr (- n 1))
               (fibr (- n 2)))))

(defn even-fibs [n]
  (letfn [(nxt [k]
            (if (> k n)
              []
              (let [fib-num (fibr k)]
                (if (even? fib-num)
                  (cons fib-num (nxt (+ k 1)))
                  (nxt (inc k))))))]
    (nxt 0)))
(even-fibs 10)
;; => (0 2 8 34)
;; compare to:
(map fibr (range 11))
;; => (0 1 1 2 3 5 8 13 21 34 55)
;;     *     *     *       *


;;; let's implement filter since we already have map
(defn my-filter [pred xs]
  (cond
    (empty? xs) ()
    (pred (first xs)) (cons (first x) (my-filter pred (rest xs)))
    :else  (my-filter pred (rest x))))

(filter odd? (range 10))
;; => (1 3 5 7 9)

;;; and also implement `accumulate`
(defn accumulate [op initial xs]
  (if (empty? xs)
    initial
    (op (first xs)
        (accumulate op initial (rest xs)))))

(accumulate + 0 (range 1 6))
;
(accumulate * 1 (range 1 6))
;; => 120

;;; Now the only thing that remains is the "enumeration"
;;; This is different for even-fibs and sum-odd-squares, respectively:

;; even-fibs need enumeration of interval:
(defn enumerate-interval [low high]
  (if (> low high)
    ()
    (cons low (enumerate-interval (inc low) high))))
(enumerate-interval 2 7)
;; => (2 3 4 5 6 7)

;; sum-odd-squares needs enumeration of leaves in a tree:
(defn enumerate-tree [tree]
  (cond
    (nil? tree) ()

    (seq? tree) (append (enumerate-tree (first tree))
                        (enumerate-tree (next tree)))
    :leaf (list tree)))

(enumerate-tree (list 1 (list 2 (list 3 4)) 5))
;; => (1 2 3 4 5)


;;; Now we can redefine sum-odd-squares and even-fibs in terms of the helper procedures
;;; to follow the signal-flow representation more closely:
(defn sum-odd-squares [tree]
  (->> tree
       (enumerate-tree)
       (filter odd?)
       (map square)
       (accumulate + 0)))
(sum-odd-squares '(1 (2 (3 (4 (5 (6 7)))))))
;; => 84

;; or alternatively using another format from the book:
(defn sum-odd-squares [tree]
  (accumulate + 0
              (map square
                   (filter odd?
                           (enumerate-tree tree)))))
(sum-odd-squares '(1 (2 (3 (4 (5 (6 7)))))))
;; => 84


(defn even-fibs [n]
  (->> (enumerate-interval 0 n)
       (map fibr)
       (filter even?)
       (accumulate cons nil)))
(even-fibs 10)
;; => (0 2 8 34)


;;; We can now also combine pieces from even-fibs and sum-odd-squares to create
;;; a funtion which lists squares of fibonacci numbers:
(defn list-fib-squares [n]
  (accumulate cons
              nil
              (map square
                   (map fibr
                        (enumerate-interval 0 n)))))
;; or more Clojur-y syntax:
(defn list-fib-squares [n]
  (->> (enumerate-interval 0 n)
       (map fibr)
       (map square)
       (accumulate cons nil)))
(list-fib-squares 10)
;; => (0 1 1 4 9 25 64 169 441 1156 3025)


;;; Ex. 2.33 (p. 119)
;;; Fill in the blanks to define `map`, `append`, `length` via `accumulate`
(defn map2 [f xs]
  (accumulate (fn [y rst]
                (cons (f y) rst))
              ;; if we used list and cons we'd have to 'reverse' it at the end?
              ()
              xs))
(map2 inc (range 9))
;; => (1 2 3 4 5 6 7 8 9)
(defn append2 [xs ys]
  (accumulate cons
              ys
              xs))
(append2 (range 10) (range 100 110))
;; => (0 1 2 3 4 5 6 7 8 9 100 101 102 103 104 105 106 107 108 109)

(defn length2 [xs]
  (accumulate (fn [x length-1]
                (inc length-1))
              0
              xs))
(length2 (range 10))
;; => 10


;;; Ex. 2.34 (p.119)
;;; Evaluate polynomial using Horner's rule
(defn horner-eval [x coefficient-seq]
  (accumulate
   (fn [coef xn-1-result]
     (+ coef (* x xn-1-result)))
   0
   coefficient-seq))

(horner-eval 2 '(1 3 0 5 0 1))
;; => 79
;; you can verify the result manually by converting to polynomial representation:
;; x^5 + 5*x^3 + 3*x + 1
;; = 32 + 40 + 6 + 1
;; = 79



;;; Ex. 2.35 (p. 120)
;;; Redefine count-leaves as an accumulation
(defn count-leaves2 [tree-list]
  ;; cheating!?
  (length (enumerate-tree tree-list)))
(count-leaves2 '(((1 2) 3 4)
                 ((1 2) 3 4)))
;; => 8

;; another try - again "cheating" with `enumerate-tree`
(defn count-leaves3 [tree-list]
  (accumulate
   (fn [fst rest-count]
     (inc rest-count)) 
   0
   (enumerate-tree tree-list)))
(count-leaves3 '(((1 2) 3 4)
                 ((1 2) 3 4)))
;; => 8

;; another try - completely manual recursion
(defn count-leaves4 [tree-list]
  (accumulate
   (fn [left right-count]
     (let [left-count (if (seq? left)
                        (count-leaves4 left)
                        1)]
       (+ left-count right-count)))
   0
   tree-list))
(count-leaves4 '(((1 2) 3 4)
                 ((1 2) 3 4)))
;; => 8
(count-leaves4 ())
;; => 0


;;; Ex. 2.36 (p.120)
;;; Write accumulate-n which can accept multiple sequences in the last arg (sequence of seqs)
(defn accumulate-n [op init seqs]
  (if (nil? (first seqs))
    ()
    (cons (accumulate op init (map first seqs))
          (accumulate-n op init (map next seqs)))))
(accumulate-n
 +
 0
 [[1 2 3]
  [4 5 6]
  [7 8 9]
  [10 11 12]])
;; => (22 26 30)


;;; Ex. 2.37 Matrix operations (p.120)
;;; matrices are represented as sequence of vectors
;; | 1 2 3 4|
;; | 4 5 6 6|
;; | 6 7 8 9|
;; is represented as:
(def my-matrix 
  [[1 2 3 4]
   [4 5 6 6]
   [6 7 8 9]])

;; dot-product is defined:
(defn dot-product [v w]
  (accumulate + 0 (map * v w)))
(dot-product [1 2 3 4] [4 5 6 6])
;; => 56

;; Complete definitions for other operations
(defn matrix-*-vector [m v]
  (map (fn [mv] (dot-product mv v))
       m))
(matrix-*-vector
 my-matrix
 [1 2 3 4])
;; => (30 56 80)

(defn transpose [m]
  (accumulate-n
   cons
   []
   m))
(transpose my-matrix)
;; => ((1 4 6) (2 5 7) (3 6 8) (4 6 9))

(defn matrix-*-matrix [m n]
  (let [ncols (transpose n)]
    (map
     (fn [mrow]
       ;; notice that this is just `matrix-*-vector` http://community.schemewiki.org/?sicp-ex-2.37
       (map
        (fn [ncol]
          (dot-product mrow ncol))
        ncols))
     m)))
(matrix-*-matrix my-matrix my-matrix)
;; => ((27 33 39 43)
;;     (60 75 90 100)
;;     (82 103 124 138))

(defn matrix-*-matrix [m n]
  (let [ncols (transpose n)]
    (map
     (fn [mrow]
       (matrix-*-vector ncols mrow))
     m)))
(matrix-*-matrix my-matrix my-matrix)
;; => ((27 33 39 43)
;;     (60 75 90 100)
;;     (82 103 124 138))



;;; Ex. 2.38 (p. 121) 
;;; accumulate is the same as `fold-right`
(def fold-right accumulate)

(defn fold-left [op initial xs]
  (letfn [(iter [result rst]
            (if (nil? rst)
              result
              (iter (op result (first rst))
                    (next rst))))]
    (iter initial xs)))
(fold-right / 1 (list 1 2 3))
;; => 3/2
(fold-left / 1 (list 1 2 3))
;; => 1/6

(fold-right list () (list 1 2 3))
;; => (1 (2 (3 ())))
(fold-left list () (list 1 2 3))
;; => (((() 1) 2) 3)

;; What property has xs satisfy to give same results for both fold-left and fold-right?
;; associativity?
(fold-right * 1 (list 1 2 3))
;; => 6
(fold-left * 1 (list 1 2 3))
;; => 6
(fold-right + 0 (list 1 2 3))
;; => 6
(fold-left + 0 (list 1 2 3))
;; => 6

;; commutativity? - counterexample that associative fn which isn't commutative (string concatenation)
;;                  still does work
(fold-right str "" (list "abc" "def" "ghi"))
;; => "abcefdghi"
(fold-left str "" (list "abc" "def" "ghi"))
;; => "abcdefghi"


;;; Ex. 2.39 (p.122)
;;; define `reverse` in terms of fold-left and fold-right
(defn reverse [xs]
  (fold-left
   (fn [acc x] (cons x acc))
   ()
   xs))
(reverse (range 10))
;; => (9 8 7 6 5 4 3 2 1 0)
(defn reverse [xs]
  (fold-right
   (fn [x y] (conj y x))
   []
   xs))
(reverse (range 10))
;; => [9 8 7 6 5 4 3 2 1 0]

;; or using solution from the web: http://community.schemewiki.org/?sicp-ex-2.39
(defn reverse [xs]
  (fold-right
   (fn [x already-reversed] (append already-reversed (list x)))
   ()
   xs))
(reverse (range 10))
;; => (9 8 7 6 5 4 3 2 1 0)



;;; Nested Mappings (p. 122) - alternative to for loops
;;; Generating sequence of pairs i,j such that 1 <= j < i <= n and i+j is prime
;;; 
;; first little cheating with clojure.core/for
(defn prime-sum-pairs [n]
  (for [i (range 1 (inc n))
        j (range 1 i)
        :when (prime? (+ i j))]
    [i j (+ i j)]))
(prime-sum-pairs 6)
;; => ([2 1 3] [3 2 5] [4 1 5] [4 3 7] [5 2 7] [6 1 7] [6 5 11])

;; now manual method using nested mappings
(defn- prime-sum? [[i j]] (prime? (+ i j)))
;; first start
(->> (enumerate-interval 1 6)
     (map (fn [i] (map (fn [j] [i j])
                       (enumerate-interval 1 (dec i)))))
     (accumulate append ()))
;; => (nil [2 1] [3 1] [3 2] [4 1] [4 2] [4 3] [5 1] [5 2] [5 3] [5 4] [6 1] [6 2] [6 3] [6 4] [6 5])

;; accumulate & append is common enough that `flatmap` exists
(defn flatmap [proc xs]
  (accumulate append () (map proc xs)))
(defn- make-pair-sum [[i j]]
  [i j (+ i j)])
(defn prime-sum-pairs2 [n]
  (->> (enumerate-interval 1 n)
       (flatmap (fn [i] (map (fn [j] [i j])
                             (enumerate-interval 1 (dec i)))))
       (filter prime-sum?)
       (map make-pair-sum)))

(prime-sum-pairs2 6)
;; => ([2 1 3] [3 2 5] [4 1 5] [4 3 7] [5 2 7] [6 1 7] [6 5 11])



;;; Set permutations (p.123 - 124)
;;; Generating a sequence of all permutations
;; start with helper remove procedure which will remove an element from a sequence
(defn remove2 [x xs]
  (filter #(not= % x) xs))
(remove2 3 (range 5))
;; => (0 1 2 4)

(defn permutations [s]
  (if (empty? s)
    '(()) ;; notice a list containing an empty list
    (flatmap
     (fn [x]
       (map (fn [p] (cons x p))
            (permutations (remove2 (first s) s))))
     s)))
(permutations #{1 2 3})
;; => ((1 3 2) (1 2 2) (3 3 2) (3 2 2) (2 3 2) (2 2 2))


;;; Ex. 2.40 (p. 123)
;;; generate a sequence of pairs i,j such that 1 <= j < i <= n
;;; Use that to simplify `prime-sum-pairs` above
(defn unique-pairs [n]
  (flatmap
   (fn [i] (map (fn [j] [i j])
                (range 1 i)))
   (range 2 (inc n))))
(unique-pairs 5)
;; => ([2 1] [3 1] [3 2] [4 1] [4 2] [4 3] [5 1] [5 2] [5 3] [5 4])

;; simiplify prime-sum-pairs (I call it prime-pairs)
(defn prime-sum-pairs3 [n]
  (->> (unique-pairs n)
       (filter prime-sum?)
       (map make-pair-sum)))

(prime-sum-pairs3 6)
;; => ([2 1 3] [3 2 5] [4 1 5] [4 3 7] [5 2 7] [6 1 7] [6 5 11])



;;; Ex. 2.41 (p. 124)
;;; Find all tripls i,j,k <= given n: (i + j + k) = given s
(defn triples [n s]
  )
