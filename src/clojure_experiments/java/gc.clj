(ns clojure-experiments.java.gc
  "Gargbage-collection related stuff."
  (:require [clojure-experiments.java.diagnostic :as diagnostics]
            [clojure.string :as str]))


;;; https://malloc.se/blog/zgc-jdk17
;;; GarbageCollectorMXBeans for Cycles and Pauses

(comment
  (doseq [gc-bean (java.lang.management.ManagementFactory/getGarbageCollectorMXBeans)]
    (println "GC stats <count, time> for" (.getName gc-bean)
             [(.getCollectionCount gc-bean) (.getCollectionTime gc-bean)]))

  ,)


;;; DiagnosticCommandMBean is a wonderful thing for getting all sorts of information!!!
;;;
;;; simulating jcmd GC.class_histogram programatically
;;; https://stackoverflow.com/questions/48325951/how-can-i-get-the-gc-class-histogram-programmatically
(comment

  ;; list all gc related diagnostic commands
  (->> (diagnostics/get-operations)
       (filter #(str/starts-with? (.getName %) "gc"))
       (map #(.getName %)))
  ;; => ("gcClassHistogram" "gcFinalizerInfo" "gcHeapInfo" "gcRun" "gcRunFinalization")

  (diagnostics/run-diagnostic-command-and-print-result "gcHeapInfo")

  (diagnostics/run-diagnostic-command-and-print-result "gcClassHistogram" 10)

  .)


