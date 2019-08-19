(ns clojure-experiments.purely-functional.property-based-testing.08-building-simple-generators
  "Purely Functional TV course - lesson 8: Building your own simple generators:
  https://purelyfunctional.tv/lesson/building-your-own-simple-generators/"
  (:require
   [clojure.test.check.generators :as gen]))


;;; such-that
;;; Has "mathemtical" sound but it really is just a "filter"
;;; Note: that such-that actually generates all the things (e.g. natural numbers) and only filter them after that

(gen/sample (gen/such-that even? gen/nat))
;; => (0 0 0 0 4 2 4 0 2 2)

(gen/sample (gen/such-that odd? gen/nat))
;; => (1 1 1 3 3 1 5 5 1 7)
(gen/sample (gen/such-that neg? gen/small-integer))
;; => (-1 -1 -2 -4 -3 -1 -2 -3 -5 -1)



;;; fmap is like "map"
;;; prefer `fmap` to `such-that` because it does less work
;;; with `such-that` you're throwing away part of generated items (maybe even 99%)

;; multiply everyone by 2
(gen/sample (gen/fmap #(* 2 %) gen/nat))
;; => (0 0 0 2 2 8 12 14 0 2)

;; odd one
(gen/sample (gen/fmap #(inc (* 2 %)) gen/nat))
;; => (1 1 1 3 3 5 7 11 1 7)

;; negate everyone
(gen/sample (gen/fmap - gen/nat))
;; => (0 -1 -1 -3 -4 0 -5 -5 -4 -6)

;; BUT previous example generates zeros too (unlike the `such-that neg?`)
;; I could combine - with inc
(gen/sample (->> gen/nat
                 (gen/fmap inc)
                 (gen/fmap -)))
;; => (-1 -2 -1 -1 -5 -5 -4 -7 -8 -4)


;;; ********************************************************
;;; prefer `fmap` to `such-that` because it does less work

;; with `such-that` you're throwing away part of generated items (maybe even 99%)
;; -> it keeps generating all these numbers, filtering them out and failing because it cannot find
;;     anything satisfying the predicate
(gen/sample (gen/such-that  #(zero? (mod % 100)) gen/nat))
;; =>    Couldn't satisfy such-that predicate after 10 tries.

;; use `fmap` INSTEAD!
(gen/sample (gen/fmap #(* 100 %) gen/nat))
;; => (0 0 100 200 100 400 300 100 100 300)
;;; ********************************************************


;;; Generating strings based on regex?

;; this will fail because strings generated by gen/string-alphanumeric are really unlikely to match regex
(gen/sample (gen/such-that #(re-matches #"[abcd]+" %) gen/string-alphanumeric))

;; Let's try with vector since we have collection of characters
(gen/sample (gen/fmap #(apply str %) ; convert coll. of chars to strings
                      ;; this will gives us nonempty collections of chars
                      (gen/not-empty (gen/vector (gen/elements [\a \b \c \d])))))
;; => ("dd" "b" "a" "db" "bbab" "ab" "ddbab" "ddbcaa" "caaba" "ddbdabcc")


;;; `gen/bind`
;;; You generate one value and use that value to create a generator
;;; Notice that `fmap` takes a value and returns a value;
;;; `bind`, on the other hand, takes a value and returns a _generator_

;; let's say we want to generate a matrix MxN
;; `fmap inc` to get rid of zeros
;; `bind` to generate the matrix
(gen/sample (gen/bind
             (gen/tuple (gen/fmap inc gen/nat)
                        (gen/fmap inc gen/nat))
             (fn [[n m]]
               (gen/vector
                (gen/vector gen/small-integer m)
                n))))
;; => ([[0]]
;;     [[1]]
;;     [[0]]
;;     [[-2 1 -2 0] [0 3 2 -1] [0 -1 -2 0]]
;;     [[4 -1 -3 0 -2]]
;;     [[-1 3 -4 5 3] [-4 -3 5 0 2] [4 -3 -2 -2 -1] [4 -1 2 -5 3] [-1 3 -1 0 -2]]
;;     [[1] [-4] [-3] [6] [-6] [-2]]
;;     [[-7 -6 7 -2 -6 -6]]
;;     [[-7 -7 -7 -2] [-8 3 -7 6] [-6 -7 1 -4]]
;;     [[-8 6 -8 9 -5 1 5 -5]
;;      [-6 -2 -8 -8 8 4 5 2]
;;      [-1 2 -1 -2 -7 2 9 -9]
;;      [-7 -4 -7 9 2 -3 8 -8]
;;      [8 6 -6 7 -2 5 1 -2]
;;      [7 6 2 -9 6 8 -1 -7]
;;      [0 -3 -6 -8 3 -6 -7 -8]])


;; Now use `gen/let` to make it clearer:
(gen/sample (gen/let  [len gen/nat]
              (gen/vector gen/nat len)))
;; => ([] [0] [] [3 3] [2 0 4] [] [0 3 1 0] [2 1 5] [1 2 0 3 0] [9 5 6 4 5])
(defn positive-int []
  (gen/fmap inc gen/nat))
(gen/sample (gen/let [[n m] (gen/tuple (positive-int)
                                       (positive-int))]))


;;; AVOID bind AND let everytime you can - shrinkage doesn't work as well
;;; => use fmap if possible

