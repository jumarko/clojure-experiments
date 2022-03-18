(ns clojure-experiments.java.gc
  "Gargbage-collection related stuff.")


;;; https://malloc.se/blog/zgc-jdk17
;;; GarbageCollectorMXBeans for Cycles and Pauses

(comment
  (doseq [gc-bean (java.lang.management.ManagementFactory/getGarbageCollectorMXBeans)]
    (println "GC stats <count, time> for" (.getName gc-bean)
             [(.getCollectionCount gc-bean) (.getCollectionTime gc-bean)]))

  ,)
