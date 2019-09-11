(ns clojure-experiments.books.sicp.ch2-abstractions-data.s2-hierarchical-data-and-closure-property
  "This deals with exampels from the section 2.2 - pages 97 - 141?
  TODO: maybe separate the Picture Language exercise?")

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



