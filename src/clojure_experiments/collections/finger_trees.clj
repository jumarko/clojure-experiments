(ns clojure-experiments.collections.finger-trees
  "Experiments with https://github.com/clojure/data.finger-tree
  See discussion here: https://clojurians.slack.com/archives/C03S1KBA2/p1659682276157529
  (about why clojure's persistent list doesn't support constant-time operations on both ends)."
  (:require
   [clojure.data.finger-tree :as ft]
   [criterium.core :as crit]))

(def my-list (apply ft/double-list (range 10)))

my-list
;; => (0 1 2 3 4 5 6 7 8 9)

(peek my-list)
;; => 9
(pop my-list)
;; => (0 1 2 3 4 5 6 7 8)

;;; compare conj vs conjl
;;; See https://github.com/clojure/data.finger-tree/blob/master/src/main/clojure/clojure/data/finger_tree.clj#L224-L230
;;; - note: `conj` is implemented through clojure.lang.IPersistentCollection/cons
(conj my-list 10)
;; => (0 1 2 3 4 5 6 7 8 9 10)

(ft/conjl my-list 10)
;; => (10 0 1 2 3 4 5 6 7 8 9)


;;; benchmark - conj vs conjl
(comment

  (crit/quick-bench (conj my-list 10))
  ;; Evaluation count : 8247816 in 6 samples of 1374636 calls.
  ;; Execution time mean : 73.144892 ns
  ;; Execution time std-deviation : 10.021466 ns
  ;; Execution time lower quantile : 65.036886 ns ( 2.5%)
  ;; Execution time upper quantile : 86.263047 ns (97.5%)
  ;; Overhead used : 7.130587 ns

  (crit/bench (conj my-list 10))

  (crit/quick-bench (ft/conjl my-list 10))
  ;; Evaluation count : 19903338 in 6 samples of 3317223 calls.
  ;; Execution time mean : 24.063897 ns
  ;; Execution time std-deviation : 1.599263 ns
  ;; Execution time lower quantile : 22.863717 ns ( 2.5%)
  ;; Execution time upper quantile : 26.077004 ns (97.5%)
  ;; Overhead used : 7.130587 ns

  .)
