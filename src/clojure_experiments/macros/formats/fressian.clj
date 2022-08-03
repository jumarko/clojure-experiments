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

