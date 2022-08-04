(ns clojure-experiments.macros.formats.fressian
  "https://github.com/clojure/data.fressian"
  (:require [clojure.data.fressian :as fress]
            [clojure-experiments.performance.memory :as memory]
            [clj-memory-meter.core :as mm]
            [cognitect.transit :as transit])
  (:import (java.io ByteArrayInputStream
                    ByteArrayOutputStream)))


(def my-data {:a (vec (range 1000000))
              :b (vec (repeat 1000000 "Hello"))})
(mm/measure my-data)
;; => "33.2 MiB"

;;; Fressian: https://github.com/clojure/data.fressian#usage
(comment

  (def written (time (fress/write my-data)))
  ;; "Elapsed time: 404.440516 msecs"
  (mm/measure written)
;; => "16.0 MiB"

  (def read-data (time (fress/read written)))
  ;; "Elapsed time: 374.062715 msecs"
  ;; TODO: wait, why it's so much larger than `my-data`?
  (mm/measure read-data)
  ;; => "76.3 MiB"

  ;; ... they are equal after all...
  (= my-data read-data)
  ;; => true


  ;; => different serialization? (not using vectors?)
  (def my-data2 {:a (doall (range 1000000))
                 :b (doall (repeat 1000000 "Hello"))})
  (mm/measure my-data2)
  ;; => "129.7 MiB"

  ;; JAVA TYPES!!!
  (type (:a read-data))
  ;; => java.util.Arrays$ArrayList

  (type (:b my-data))
  ;; => clojure.lang.PersistentVector
  (type (:b my-data2))
  ;; => clojure.lang.Repeat

  )


;;; Transit: https://github.com/cognitect/transit-clj
(comment

  (def written (ByteArrayOutputStream.))
  (def writer (transit/writer written :msgpack))
  (time (transit/write writer my-data))
  ;; "Elapsed time: 279.97247 msecs"
  (mm/measure written)
  ;; => "16.0 MiB"

  (def in (ByteArrayInputStream. (.toByteArray written)))
  (def reader (transit/reader in :msgpack))
  (def read-data (time (transit/read reader)))
  ;; "Elapsed time: 542.290872 msecs"
  (mm/measure read-data)
  ;; => "79.0 MiB"

  ;; they are equal but read-data  (79.0 MiB) is still lot larger
  ;; than my-data (33.2 MiB)
  (= my-data read-data)
  ;; => true

  (type (:a read-data))
  ;; => clojure.lang.PersistentVector

  (memory/print-memory-layout my-data)
  ;; clojure.lang.PersistentArrayMap@5509c8f0d footprint:
  ;;    COUNT       AVG       SUM   DESCRIPTION
  ;;        5        24       120   [B
  ;;    64519       143   9290624   [Ljava.lang.Object;
  ;;        2        24        48   clojure.lang.Keyword
  ;;        1        32        32   clojure.lang.PersistentArrayMap
  ;;        2        40        80   clojure.lang.PersistentVector
  ;;    64516        24   1548384   clojure.lang.PersistentVector$Node
  ;;        2        32        64   clojure.lang.Symbol
  ;;  1000000        24  24000000   java.lang.Long
  ;;        5        24       120   java.lang.String
  ;;        2        16        32   java.util.concurrent.atomic.AtomicReference
  ;;  1129054            34839504   (total)

  (memory/print-memory-layout read-data)

  .)
