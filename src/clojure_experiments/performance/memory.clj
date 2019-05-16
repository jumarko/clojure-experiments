(ns clojure-experiments.performance.memory
  (:require [clj-memory-meter.core :as mm])
  (:import (org.openjdk.jol.info ClassLayout GraphLayout)))


;;; Excercising memory throughput as in the 'Optimizing Java' book - p.39
(comment
  
  (def array-size (* 2 1024 1024))
  (def test-data (int-array array-size)))

;;; Comparing sizes of various collections

;; vectors vs seqs - 1,000,000 elements
;; => vector is vastly more efficient
(comment

  (mm/measure 1000)
;; => "24 B"  

  (mm/measure (doall (range 1000)))
;; => "93.8 KB"  
  (mm/measure (vec (range 1000)))
;; => "28.8 KB"

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

