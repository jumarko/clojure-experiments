(ns clojure-experiments.gotchas
  "Things to watch for when using clojure (core) api.")

;;; rseq doesn't support all sequential collections
;;; - only vectors and sorted maps
(rseq [1 2 3])
;; => (3 2 1)

#_(rseq (map inc [1 2 3]))
;; 1. Unhandled java.lang.ClassCastException
;; class clojure.lang.LazySeq cannot be cast to class clojure.lang.Reversible (clojure.lang.LazySeq
;; core.clj: 1596  clojure.core/rseq
