(ns clojure-experiments.java.processes.impl
  "This impl ns should not be required directly unless the minimum supported version is Java 9.")

(defn ^{:min-java-version "9"} all-processes []
  (java.lang.ProcessHandle/allProcesses))
