(ns clojure-experiments.performance.memory
  (:require [clj-memory-meter.core :as mm]
            [cljol.dig9 :as cljol]
            [jvm-alloc-rate-meter.core :as ameter])
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

;; cljol experiments 
(comment
  (cljol/view [fib-seq] opts)
  (cljol/view [[fib-seq (nthrest fib-seq 1) (nthrest fib-seq 2)]] opts)
  (cljol/write-dot-file [[fib-seq (nthrest fib-seq 1) (nthrest fib-seq 2)]]
                        "lazy-fib-seq-vector-of-nthrest.dot" opts))


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


;;; Measuring allocation rates via jvm-alloc-rate-meter: https://github.com/clojure-goes-fast/jvm-alloc-rate-meter
;;; WATCH OUT! it uses System.currentTimeMillis ! https://github.com/clojure-goes-fast/jvm-alloc-rate-meter/blob/master/src/jvm_alloc_rate_meter/MeterThread.java#L45
(comment

  (def am (ameter/start-alloc-rate-meter #(println "Rate is:" (/ % 1e6) "MB/sec")))
  ;; typical output in "rest"
  ;; (BUT this was run in Cider and mostly because of cider's overhead (printing, etc.))
  ;; (in lein repl it was much less frequent)
  ;; Rate is: 4.185932 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 2.092966 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 2.084644 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 0.0 MB/sec
  ;; Rate is: 2.084644 MB/sec
  ;; Rate is: 0.0 MB/sec


  ;; Test it out
  (while true
    (byte-array 1e7)
    (Thread/sleep 100))

  ;; The meter should report ~100 MB/sec allocation rate into the console.

  ;; To stop the meter thread
  (am)

;;
  )

;;; Measuring memory allocated by a single thread / method
;; https://stackoverflow.com/questions/61539760/benchmarking-jvm-memory-consumption-similarly-to-how-it-is-done-by-the-os
(defn thread-allocated-bytes [t]
  (let [thread-mbean (java.lang.management.ManagementFactory/getThreadMXBean)
        thread-id (.getId t)]
    (.getThreadAllocatedBytes thread-mbean thread-id)))

(defn allocated-bytes
  [f]
  (let [thread (Thread/currentThread)
        start (thread-allocated-bytes thread)]
    (f)
    (- (thread-allocated-bytes thread) start)))

(allocated-bytes #())
;; => 20336
(allocated-bytes (fn []))
;; => 20176
;; notice that JOL shown 29480 bytes for vector of 1000 numbers so this looks close
(allocated-bytes (fn [] (vec (range 1000000))))
;; => 29436912

;; bytes allocated by a different thread
;; - notice that thread is stopped abruptly to avoid over-consumption

(comment
  
  (let [how-much 1e8
        t (Thread.  (fn []
                      (let [v (vec (range how-much))])
                      (Thread/sleep 1000) (println "DONE.")))]
    (println "Thread allocated - before start:" (thread-allocated-bytes t))
    (.start t)
    (println "Thread allocated - after start:" (thread-allocated-bytes t))

    (Thread/sleep 100)
    (println "Thread allocated - after 100 ms:" (thread-allocated-bytes t))

    (when (< 1e6 (thread-allocated-bytes t))
      (println "Stopping the thread...")
      ;; note that `(.interrupt t)` isn't enough here - the thread is busy allocating and cannot be interrupted
      (.stop t)
      (println "Thread stopped")
      (println "Thread allocated - after stop:" (thread-allocated-bytes t)))

    (Thread/sleep 200)
    (println "Thread allocated - after 300 ms:" (thread-allocated-bytes t))

    (Thread/sleep 800)
    (println "Thread allocated - after 1100 ms:" (thread-allocated-bytes t)))
  ,)
;; prints:
;; Thread allocated - before start: -1
;; Thread allocated - after start: 14064
;; Thread allocated - after 100 ms: 125412240
;; Stopping the thread...
;; Thread stopped
;; Thread allocated - after stop: -1
;; Thread allocated - after 300 ms: -1
;; Thread allocated - after 1100 ms: -1



;;; TODO: add jvm-hiccup-meter: https://github.com/clojure-goes-fast/jvm-hiccup-meter


