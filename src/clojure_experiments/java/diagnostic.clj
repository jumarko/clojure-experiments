(ns clojure-experiments.java.diagnostic
  (:import (javax.management ObjectName))
  (:require [clojure.string :as str]))

;;; DiagnosticCommandMBean is a wonderful thing for getting all sorts of information!!!
;;; IDEA: codescene self-diagnostic capabilities?!
;;;
;;; simulating jcmd GC.class_histogram programatically
;;; https://stackoverflow.com/questions/48325951/how-can-i-get-the-gc-class-histogram-programmatically
;;; DiagnosticCommandMBean tests: https://github.com/openjdk/jdk/tree/master/test/jdk/com/sun/management/DiagnosticCommandMBean
;;; http://marxsoftware.blogspot.com/2016/04/programmatic-jcmd-access.html
(def platform-server (java.lang.management.ManagementFactory/getPlatformMBeanServer))
(def dcmd-object-name (ObjectName. "com.sun.management:type=DiagnosticCommand"))

(defn get-operations
  "Returns all operations supported by DiagnosticCommand mbean.
  The result is a sequence of `javax.management.MBeanOperationInfo` objects.

  See javadoc:
  - https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/MBeanOperationInfo.html
  - https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/MBeanInfo.html"
  []
  (seq (.getOperations (.getMBeanInfo platform-server dcmd-object-name))))

(comment

  ;; all available mbeans: https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/MBeanServer.html#queryMBeans(javax.management.ObjectName,javax.management.QueryExp)
  (set (.queryMBeans platform-server nil nil))

  ;; get mbean info: https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/MBeanInfo.html
  ;; ... and use it to list all mbean operations
  ;; - https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/MBeanOperationInfo.html
  (map #(.getName %) (get-operations))
  
  ;; => ("compilerCodeHeapAnalytics"
  ;;     "compilerCodecache"
  ;;     "compilerCodelist"
  ;;     "compilerDirectivesAdd"
  ;;     "compilerDirectivesClear"
  ;;     "compilerDirectivesPrint"
  ;;     "compilerDirectivesRemove"
  ;;     "compilerQueue"
  ;;     "gcClassHistogram"
  ;;     "gcFinalizerInfo"
  ;;     "gcHeapInfo"
  ;;     "gcRun"
  ;;     "gcRunFinalization"
  ;;     "help"
  ;;     "jfrCheck"
  ;;     "jfrConfigure"
  ;;     "jfrDump"
  ;;     "jfrStart"
  ;;     "jfrStop"
  ;;     "jvmtiAgentLoad"
  ;;     "jvmtiDataDump"
  ;;     "threadDumpToFile"
  ;;     "threadPrint"
  ;;     "vmCds"
  ;;     "vmClassHierarchy"
  ;;     "vmClasses"
  ;;     "vmClassloaderStats"
  ;;     "vmClassloaders"
  ;;     "vmCommandLine"
  ;;     "vmDynlibs"
  ;;     "vmEvents"
  ;;     "vmFlags"
  ;;     "vmInfo"
  ;;     "vmLog"
  ;;     "vmMetaspace"
  ;;     "vmNativeMemory"
  ;;     "vmPrintTouchedMethods"
  ;;     "vmSetFlag"
  ;;     "vmStringtable"
  ;;     "vmSymboltable"
  ;;     "vmSystemProperties"
  ;;     "vmSystemdictionary"
  ;;     "vmUptime"
  ;;     "vmVersion")


  ;; invoke diagnostic command: vmFlags
  ;; see http://clojure-goes-fast.com/blog/performance-nemesis-reflection/ for explanation of "[Ljava.lang.String;"
  (str/split (.invoke platform-server dcmd-object-name "vmFlags" (object-array 1) (into-array ["[Ljava.lang.String;"]))
             #" ")
  ;; => ["-XX:CICompilerCount=4"
  ;;     "-XX:ConcGCThreads=3"
  ;;     "-XX:G1ConcRefinementThreads=10"
  ;;     "-XX:G1EagerReclaimRemSetThreshold=12"
  ;;     "-XX:G1HeapRegionSize=1048576"
  ;;     "-XX:G1RemSetArrayOfCardsEntries=12"
  ;;     "-XX:G1RemSetHowlMaxNumBuckets=8"
  ;;     "-XX:G1RemSetHowlNumBuckets=4"
  ;;     "-XX:GCDrainStackTargetSize=64"
  ;;     "-XX:InitialHeapSize=536870912"
  ;;     "-XX:MarkStackSize=4194304"
  ;;     "-XX:MaxHeapSize=1073741824"
  ;;     "-XX:MaxNewSize=643825664"
  ;;     "-XX:MinHeapDeltaBytes=1048576"
  ;;     "-XX:MinHeapSize=8388608"
  ;;     "-XX:NonNMethodCodeHeapSize=5839372"
  ;;     "-XX:NonProfiledCodeHeapSize=122909434"
  ;;     "-XX:ProfiledCodeHeapSize=122909434"
  ;;     "-XX:ReservedCodeCacheSize=251658240"
  ;;     "-XX:+SegmentedCodeCache"
  ;;     "-XX:+ShowCodeDetailsInExceptionMessages"
  ;;     "-XX:SoftMaxHeapSize=1073741824"
  ;;     "-XX:+UseCompressedClassPointers"
  ;;     "-XX:+UseCompressedOops"
  ;;     "-XX:+UseFastUnorderedTimeStamps"
  ;;     "-XX:+UseG1GC"
  ;;     "-XX:-UseNUMA"
  ;;     "-XX:-UseNUMAInterleaving"
  ;;     "\n"]

  ;; invoke command gcClassHistogram
  (->> (get-operations)
       (filter #(= "gcClassHistogram" (.getName %)))
       first
       .getSignature
       seq
       (map (juxt #(.getName %) #(.getType %))))
  ;; => ("[Ljava.lang.String;")

  (->> (.invoke platform-server dcmd-object-name "gcClassHistogram" (object-array 1) (into-array ["[Ljava.lang.String;"]))
       (str/split-lines)
       (take 12)
       (run! println))
  .)

(defn run-diagnostic-command
  ([command-name]
   (run-diagnostic-command command-name 10))
  ([command-name output-limit]
   (let [result (.invoke platform-server
                         dcmd-object-name
                         command-name
                         (object-array 1)
                         (into-array ["[Ljava.lang.String;"]))]
     (->> result
          (str/split-lines)
          (take output-limit)))))

(defn run-diagnostic-command-and-print-result
  [& args]
  (run! println
        (apply run-diagnostic-command args)))

(comment
  ;; show only the top class
  (run-diagnostic-command-and-print-result "gcClassHistogram" 3)

  ;; try another interesting command; VM.info
  (run-diagnostic-command-and-print-result "vmInfo" 50)

  ;; and misc others
  (run-diagnostic-command-and-print-result "gcHeapInfo")
  (run-diagnostic-command-and-print-result "vmClassloaders")
  (run-diagnostic-command-and-print-result "vmCommandLine")
  (run-diagnostic-command-and-print-result "vmEvents")
  (run-diagnostic-command-and-print-result "vmClasses")
  (run-diagnostic-command-and-print-result "vmSystemProperties" 100)
  (run-diagnostic-command-and-print-result "vmMetaspace" 200)
  ;; Note: Native memory tracking must be enabled via `‑XX:NativeMemoryTracking` jvm option
  (run-diagnostic-command-and-print-result "vmNativeMemory")
  (run-diagnostic-command-and-print-result "compilerCodeHeapAnalytics" 100)


  .)


;;; MaxDirectMemory - tricky to get this value
;;; See
;;; - Default HotSpot Maximum Direct Memory Size https://dzone.com/articles/default-hotspot-maximum-direct-memory-size
;;; - Replace access to sun.misc.VM for JDK 11 https://stackoverflow.com/questions/53543062/replace-access-to-sun-misc-vm-for-jdk-11
;;;   => you need `--add-export java.base/jdk.internal.misc=xyz` (see deps.edn)

;; MaxDirectMemorySize is by default the same as MaxHeapSize
(jdk.internal.misc.VM/maxDirectMemory)
;; => 1073741824
