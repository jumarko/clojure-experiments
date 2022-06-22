(ns clojure-experiments.performance.async-profiler
  (:require [clj-async-profiler.core :as prof]))

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

(defn- network-call []
  (apply str (take 15 (slurp "https://idnes.cz"))))
;; it's about 150 msecs on my machine/network in Brno, CZ
#_(time (network-call ))
;; "Elapsed time: 159.988287 msecs"


;; Let's demonstrate how CPU profiling can yield a misleading profile
;; in terms of where the real bottleneck is.
;; Here, CPU-expensive operations consume about 2 seconds
;; while Network IO about 10 seconds
(comment
  (time (prof/profile {:width 2400 :return-file true
                       ;; try wall-clock profiling to get more accurate picture: https://github.com/jvm-profiling-tools/async-profiler#wall-clock-profiling
                       :event :wall :threads true }
                      (print "waiting for cpu: ")
                      (time (burn-cpu 1))

                      (print "waiting for Network IO: ")
                      (time (dotimes [_ 50] (network-call)))

                      (print "waiting for cpu: ")
                      (time (burn-cpu 1))))
  ;; waiting for cpu: "Elapsed time: 1001.41616 msecs"
  ;; waiting for Network IO: "Elapsed time: 10125.600056 msecs"
  ;; waiting for cpu: "Elapsed time: 1001.359411 msecs"
  ;; "Elapsed time: 12708.13256 msecs"
  .)


(comment

  (prof/start {})

  ;; or 
  #_(prof/start {:event :alloc})

  (burn-cpu 10)

  (doto (prof/stop {:width 2400}) prn)
;; => Execution error (ExceptionInfo) at clj-async-profiler.core/stop (core.clj:262).
;;    Profiler is not active
  ;; => /private/tmp/clj-async-profiler/results/flamegraph-2019-03-10-12-12-46.svg
  ;; (open in Chrome)

  (prof/list-event-types)
;; => Basic events:
;;      cpu
;;      alloc
;;      lock
  (require '[clj-async-profiler.core :as prof])
  (prof/start {:width 2400});;      wall
;;      itimer

  (prof/serve-files 8888) 


;; end comment
  )
;; => nil


;;; clj-async-profiler -> differential flamegraphs
;;; http://clojure-goes-fast.com/blog/clj-async-profiler-040/
(comment
  ;; division will be slowest because of Ratios
  (prof/profile {:width 2400 :return-file true}
   (dotimes [_ 10] (reduce + (range 10000000)))
   (dotimes [_ 10] (reduce / (range 10000000)))
   (dotimes [_ 10] (reduce * (range 10000000))))

  (prof/profile {:width 2400 :return-file true}
                (Thread/sleep 200))
  

  ;; profile external process
  (def pid 64735)
  (prof/start {:width 1800 :pid pid})
  (Thread/sleep 5000)
  (prof/stop {:width 1800 :pid pid})

  ;; try again
  (prof/profile {:width 2400}
   (dotimes [_ 15] (reduce + (range 10000000)))
   (dotimes [_ 5] (reduce / (range 10000000))))


  ;; ...and compare
  (prof/generate-diffgraph 1 2 {:width 2400})

  ;; end 
  )






