(ns clojure-experiments.java.classpath
  (:import (nonapi.io.github.classgraph.classpath ClasspathFinder)
           (nonapi.io.github.classgraph.scanspec ScanSpec)
           (nonapi.io.github.classgraph.utils LogNode)))

;;; ClassGraph is cool project for scanning classpath: https://github.com/classgraph/classgraph
;;; see also https://ask.clojure.org/index.php/8599/should-java-classpath-combine-classpath-sources
(comment

  (def cp-finder (ClasspathFinder. (ScanSpec.) nil))
  (def cp-entries
    (->> cp-finder
         (.getClasspathOrder)
         (.getClasspathEntryUniqueResolvedPaths)
         (into #{})))

  ;; 
  )
