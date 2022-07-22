(ns clojure-experiments.java.classpath
  (:require [lambdaisland.classpath :as cp])
  #_(:import (io.github.classgraph ClassGraph)
           (nonapi.io.github.classgraph.classpath ClasspathFinder)
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

  (ClassGraph.)


  ;; 
  )

;;; https://lambdaisland.com/blog/2021-08-25-classpath-is-a-lie
;;; https://github.com/lambdaisland/classpath
(comment

  (cp/classloader-chain)
  ;; => (#object[clojure.lang.DynamicClassLoader 0x1d92c596 "clojure.lang.DynamicClassLoader@1d92c596"]
  ;;     ....
  ;;     #object[clojure.lang.DynamicClassLoader 0x7d3bbfce "clojure.lang.DynamicClassLoader@7d3bbfce"]
  ;;     #object[jdk.internal.loader.ClassLoaders$AppClassLoader 0x251a69d7 "jdk.internal.loader.ClassLoaders$AppClassLoader@251a69d7"]
  ;;     #object[jdk.internal.loader.ClassLoaders$PlatformClassLoader 0x4c194da "jdk.internal.loader.ClassLoaders$PlatformClassLoader@4c194da"])

  ;; inspect classpaths in detail
  (cp/classpath-chain)

  .)

;; you can also update the classpath!!!
(comment

  .)
