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

  ;; or 
  (prof/start {:event :alloc})

  (burn-cpu 10)

  (prof/stop {})
;; => Execution error (ExceptionInfo) at clj-async-profiler.core/stop (core.clj:262).
;;    Profiler is not active
  ;; => /private/tmp/clj-async-profiler/results/flamegraph-2019-03-10-12-12-46.svg
  ;; (open in Chrome)

  (prof/list-event-types)
;; => Basic events:
;;      cpu
;;      alloc
;;      lock
;;      wall
;;      itimer

  (prof/serve-files 8888) 


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


;;; https://clojurefun.wordpress.com/2012/08/06/clojure-performance-tip-exploiting-primitive-casts-20/
(comment

  
  (defn length1 [x y z]
    (Math/sqrt (+ (* x x) (* y y) (* z z))))
  (time (dotimes [i 1000000] (length1 10 10 10)))

  (defn length2 [x y z]
    (let [x (double x)
          y (double y)
          z (double z)]
      (Math/sqrt (+ (* x x) (* y y) (* z z)))))
  (time (dotimes [i 1000000] (length2 10 10 10)))

  (defn length3 [^double x ^double y ^double z]
    (Math/sqrt (+ (* x x) (* y y) (* z z))))
  (time (dotimes [i 1000000] (length3 10.0 10.0 10.0))))



;;; Time measuring performance
;;; https://web.archive.org/web/20160308031939/https://blogs.oracle.com/dholmes/entry/inside_the_hotspot_vm_clocks
;;; https://shipilev.net/blog/2014/nanotrusting-nanotime/

(comment 
  (doseq [x (range 1000)]
    (println "x: " (System/currentTimeMillis)))

  (require '[criterium.core :as crit])

  (crit/quick-bench     (System/currentTimeMillis))
  ;; Evaluation count : 15211470 in 6 samples of 2535245 calls.
  ;; Execution time mean : 34.717222 ns
  ;; Execution time std-deviation : 2.461151 ns
  ;; Execution time lower quantile : 32.458668 ns ( 2.5%)
  ;; Execution time upper quantile : 37.786261 ns (97.5%)
  ;; Overhead used : 6.270366 ns

  (crit/quick-bench     (System/nanoTime))
  ;; Evaluation count : 13578144 in 6 samples of 2263024 calls.
  ;; Execution time mean : 41.184378 ns
  ;; Execution time std-deviation : 3.742258 ns
  ;; Execution time lower quantile : 37.631263 ns ( 2.5%)
  ;; Execution time upper quantile : 45.147612 ns (97.5%)
  ;; Overhead used : 6.270366 ns

  ;; end 
  )
