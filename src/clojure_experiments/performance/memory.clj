(ns clojure-experiments.performance.memory
  (:require [clj-memory-meter.core :as mm]
            [cljol.dig9 :as cljol])
  (:import (org.openjdk.jol.info ClassLayout GraphLayout)))


;;; Excercising memory throughput as in the 'Optimizing Java' book - p.39
(comment
  
  (def array-size (* 2 1024 1024))
  (def test-data (int-array array-size)))


;;; cljol
;;; doesn't play well with large data structures: https://github.com/jafingerhut/cljol/issues/2
(def my-map {:a 1 :b 2 :c 3})
#_(cljol/view my-map)


;;; cljol - lazy sequences: https://github.com/jafingerhut/cljol/blob/master/doc/README-gallery.md

;; we need to tweek cljol opts to avoid realizing the lazy sequence fully
(def opts {})


;; let's define fibonacci numbers seq
(defn fib-fn [a b]
  (lazy-seq (cons a (fib-fn b (+' a b)))))
(def fib-seq (fib-fn 0 1))
(take 10 fib-seq)
;; => (0 1 1 2 3 5 8 13 21 34)

(cljol/view [fib-seq] opts)


(cljol/view [[fib-seq (nthrest fib-seq 1) (nthrest fib-seq 2)]] opts)
(cljol/write-dot-file [[fib-seq (nthrest fib-seq 1) (nthrest fib-seq 2)]]
                  "lazy-fib-seq-vector-of-nthrest.dot" opts)


;;; Memory-Meter: Comparing sizes of various collections

;; vectors vs seqs - 1,000,000 elements
;; => vector is vastly more efficient
(comment

  (mm/measure 1000)
;; => "24 B"  

  (mm/measure (doall (range 1000)))
;; => "93.8 KB"  
  (mm/measure (vec (range 1000)))
;; => "28.8 KB"

;; `vector-of` !
(mm/measure (apply (partial vector-of :long) (range 1000)))
;;=>  "9.3 KB"
(mm/measure (apply (partial vector-of :int) (range 1000)))
;; => "5.4 KB"

  (mm/measure (doall (range 1000000)))
;; => "91.6 MB"
  (mm/measure (vec (range 1000000)))
;; => "28.1 MB"




  ;;; JOL layout
  
  (println (.toFootprint (GraphLayout/parseInstance (into-array [(doall (range 1000))]))))
  ;; clojure.lang.LongRange@390895e7d footprint:
  ;; COUNT       AVG       SUM   DESCRIPTION
  ;; 1000        64     64000   clojure.lang.LongRange
  ;; 1        24        24   clojure.lang.LongRange$1
  ;; 1000        32     32000   clojure.lang.LongRange$LongChunk
  ;; 2001               96024   (total)



  (println (.toFootprint (GraphLayout/parseInstance (into-array [(vec (range 1000))]))))
  ;; clojure.lang.PersistentVector@53f51c6fd footprint:
  ;; COUNT       AVG       SUM   DESCRIPTION
  ;; 33       141      4656   [Ljava.lang.Object;
  ;;                           1        40        40   clojure.lang.PersistentVector
  ;;                           32        24       768   clojure.lang.PersistentVector$Node
  ;;                           1000        24     24000   java.lang.Long
  ;;                           1        16        16   java.util.concurrent.atomic.AtomicReference
  ;;                           1067               29480   (total)


;; end
  )

