(ns clojure-experiments.performance.performance
  (:require [clj-async-profiler.core :as prof]
            [clj-java-decompiler.core :as decompiler :refer [decompile disassemble]]
            [no.disassemble :as nd]
            [criterium.core :as crit]
            [clj-java-decompiler.core :refer [decompile disassemble] :as decompiler]
            ))

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
  (prof/profile {:width 2400}
   (dotimes [_ 10] (reduce + (range 10000000)))
   (dotimes [_ 10] (reduce / (range 10000000)))
   (dotimes [_ 10] (reduce * (range 10000000))))


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


;;; 
(comment exploring *unchecked-math*, generated bytecode and performance implications
  
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


;;; mapv vs map + into when there are multiple stages
;; alexmiller  when I hear "a bunch of maps", that suggests you might be going through multiple mapv calls on the same data. 
;; in that case, using `(into [] (comp (map F1) (map F2) (map F3)) V)` 
;; is going to much more efficient than `(->> V (mapv F1) (mapv F2) (mapv F3))`
;; in that the former will not create any intermediate vectors
(comment

  (time (->> (range 10000000)
             (mapv inc)
             (mapv #(* % 3))
             (mapv #(+ % 2))
             (mapv #(- % 10))
             (mapv str)
             (take 10)
             ))
  ;; "Elapsed time: 2291.661106 msecs"

  (time (->> 
         (into [] (comp
                   (map inc)
                   (map #(* % 3))
                   (map #(+ % 2))
                   (map #(- % 10))
                   (map str))
               (range 10000000))
         (take 10)))
  ;; "Elapsed time: 1094.197845 msecs"

  )


;;; ^:const behavior
;; works on primitive values surprisingly well:
(def pi1 3.14)
(defn circ1 [r] (* 2 pi1 r))
(comment 
  (time (dotimes [_ 1e5] (circ1 5)))
  (crit/quick-bench (circ1 5)))
;; Execution time mean : 18.908745 ns
;; Execution time std-deviation : 2.226636 ns


;; => nil


(def ^:const pi2 3.14)
(defn circ2 [r] (* 2 pi2 r))
(comment 
  (time (dotimes [_ 1e5] (circ2 5)))
  (crit/quick-bench (circ2 5)))
;; Execution time mean : 5.132742 ns
;; Execution time std-deviation : 0.213913 ns
(decompile (* 2 pi1 100))
(decompile (* 2 pi2 100))

(def mm {:a [1 2 3]})
(def ^:const mmc {:a [1 2 3]})
(decompile (assoc mm :b []))
(decompile (assoc mmc :b []))

(disassemble (assoc mm :b []))
(disassemble (assoc mmc :b []))

;; but less well on arbitrary objects/values


;;; Interesting performance issue that Andy Fingerhunt hit: (function faster the first time it's called)
;;; https://github.com/jafingerhut/leeuwenhoek/blob/master/doc/runs-slower-after-first.md
;;; cgrand 10:19 AM @andy.fingerhut your loop is in return position and returns a primitive, the function return type is object, this return type mismatch is the root of the issue:
;;;    No clear idea. I believe it has something to do with on stack replacement, and later realizing that the replacement sig does not match the replacee sig  maybe even pushing some boxing inside the loop. Inspecting assembly could help figure out.
(defn foo2 [n]
  (let [n (int n)]
    (loop [i (int 0)]
      (if (< i n)
        (recur (inc i))
        i))))
#_(time (foo2 100000000))
;; "Elapsed time: 53.865585 msecs"
#_(time (foo2 100000000))
;; "Elapsed time: 226.535655 msecs"

;; return object
(defn foo2 [n]
  (let [n (int n)]
    (loop [i (int 0)]
      (if (< i n)
        (recur (inc i))
        :foo))))
#_(time (foo2 100000000))
;; "Elapsed time: 57.383683 msecs"
#_(time (foo2 100000000))
;; "Elapsed time: 56.647265 msecs"

;; return primitive
(defn ^long foo2 [n]
  (let [n (int n)]
    (loop [i (int 0)]
      (if (< i n)
        (recur (inc i))
        i))))
#_(time (foo2 100000000))
;; "Elapsed time: 51.049683 msecs"
#_(time (foo2 100000000))
;; "Elapsed time: 47.310656 msecs"


