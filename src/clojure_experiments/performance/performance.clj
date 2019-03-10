(ns clojure-experiments.performance.performance
  (:require [clj-async-profiler.core :as prof]
            [clj-java-decompiler.core :as decompiler :refer [decompile disassemble]]
            [no.disassemble :as nd]))

;;; Boxed math

;; http://insideclojure.org/2014/12/15/warn-on-boxed/
(defn sum-squares [a b]
  (+ (* a a) (* b b)))



;;; clj-async-profiler
;;; http://clojure-goes-fast.com/blog/profiling-tool-async-profiler/
;;; http://clojure-goes-fast.com/blog/clj-async-profiler-tips/
(defn test-sum []
  (reduce + (map inc (range 1000))))


(defn test-div []
  (reduce / (map inc (range 1000))))

(defn burn-cpu [secs]
  (let [start (System/nanoTime)]
    (while (< (/ (- (System/nanoTime) start) 1e9) secs)
      (test-sum)
      (test-div))))
(comment

  (prof/start {})
  (burn-cpu 10)
  (prof/stop {})
  ;; => /private/tmp/clj-async-profiler/results/flamegraph-2019-03-10-12-12-46.svg
  ;; (open in Chrome)

  (prof/list-event-types)
;; => Basic events:
;;      cpu
;;      alloc
;;      lock
;;      wall
;;      itimer

;; end comment
  )
;; => nil

(comment
  
  (decompile (sum-squares 2 3))
  (decompile (+ (* 2 2 ) (* 3 3)))

  (set! *unchecked-math* :warn-on-boxed)

  (sum-squares 2 3)
  
  (disassemble (sum-squares 2 3))
  (disassemble (+ (* 2 2 ) (* 3 3)))

  ;; requires leinigen plugin
  (nd/disassemble (sum-squares 2 3))


  (decompile
   (loop [i 100, sum 0]
     (if (< i 0)
       sum
       (recur (unchecked-dec i) (unchecked-add sum i)))))

  ;; end comment
  )
