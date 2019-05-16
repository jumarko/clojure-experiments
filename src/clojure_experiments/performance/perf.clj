(ns clojure-experiments.performance.perf
  "Performance counters exposed by JVM.
  See 'Production-Time Profiling and Diagnostics on the JVM': https://youtu.be/Pqf367x-OuA?list=PLgEC2LYlSuNNDHqBhm2WeW8CODp7XgxDy&t=625.
  Perf.java: https://github.com/AdoptOpenJDK/openjdk-jdk11u/blob/master/src/java.base/share/classes/jdk/internal/perf/Perf.java"
  (:import (jdk.internal.perf Perf)))


(comment

  ;; unfortunately, this doesn't work because Perf is an internal class that isn't exported
  (doto (Perf/getPerf) )



  ;; end
  )

