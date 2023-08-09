(ns clojure-experiments.performance.clojure-goes-brno
  "Clojure Goes Brrr: a quest for performance (by Oleksandr Yakushev).
  https://www.youtube.com/watch?v=s3mjVAMNVrA"
  (:require
   [clj-async-profiler.core :as prof]
   [virgil.compile :as virgil]))

;;; Convenient tools from this gist: https://gist.github.com/alexander-yakushev/63515455759e66bfa19dbaa126fccf56
(let [time*
      (fn [^long duration-in-ms f]
        (let [^com.sun.management.ThreadMXBean bean (java.lang.management.ManagementFactory/getThreadMXBean)
              bytes-before (.getCurrentThreadAllocatedBytes bean)
              duration (* duration-in-ms 1000000)
              start (System/nanoTime)
              first-res (f)
              delta (- (System/nanoTime) start)
              deadline (+ start duration)
              tight-iters (max (quot (quot duration delta) 10) 1)]
          (loop [i 1]
            (let [now (System/nanoTime)]
              (if (< now deadline)
                (do (dotimes [_ tight-iters] (f))
                    (recur (+ i tight-iters)))
                (let [i' (double i)
                      bytes-after (.getCurrentThreadAllocatedBytes bean)
                      t (/ (- now start) i')]
                  (println
                   (format "Time per call: %s   Alloc per call: %,.0fb   Iterations: %d"
                           (cond (< t 1e3) (format "%.0f ns" t)
                                 (< t 1e6) (format "%.2f us" (/ t 1e3))
                                 (< t 1e9) (format "%.2f ms" (/ t 1e6))
                                 :else (format "%.2f s" (/ t 1e9)))
                           (/ (- bytes-after bytes-before) i')
                           i))
                  first-res))))))]
  (defmacro time+
    "Like `time`, but runs the supplied body for 2000 ms and prints the average
  time for a single iteration. Custom total time in milliseconds can be provided
  as the first argument. Returns the returned value of the FIRST iteration."
    [?duration-in-ms & body]
    (let [[duration body] (if (integer? ?duration-in-ms)
                            [?duration-in-ms body]
                            [2000 (cons ?duration-in-ms body)])]
      `(~time* ~duration (fn [] ~@body)))))

(defn heap []
  (let [u (.getHeapMemoryUsage (java.lang.management.ManagementFactory/getMemoryMXBean))
        used (/ (.getUsed u) 1e6)
        total (/ (.getMax u) 1e6)]
    (format "Used: %.0f/%.0f MB (%.0f%%), free: %.0f MB" used total (/ used total 0.01)
            (/ (.freeMemory (Runtime/getRuntime)) 1e6))))

(comment
  )


;;; clj-async-profiler

;; start web server ui
(comment 
  (prof/serve-ui "localhost" 8123)
  .)

;; do some profiling
(comment
  (prof/profile (Thread/sleep 200))
  .)

;; diff graphs
(comment
  (prof/generate-diffgraph X Y)
  .)


;;; Compile java code with virgil
;; Note that this needs exports on modern JDKs to allow access to `com.sun.tools.javac.api.JavacTool`
;; - see https://errorprone.info/docs/installation#jdk-16
;; --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
;; --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
;; --add-exports jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED
;; --add-exports jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED
;; --add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
;; --add-exports jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED
;; --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
;; --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
;; --add-opens jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
;; --add-opens jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED
(comment
  (virgil/compile-all-java ["src"])
  .)
