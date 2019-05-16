(ns clojure-experiments.monitoring
  "Various snippets and 'tools' for monitoring/debugging running JVM."
  (:import (sun.jvmstat.monitor MonitoredHost
                                VmIdentifier)))


;;; Creating Your Own Debugging Tools (Andrei Pangin): http://www.javamagazine.mozaicreader.com/JanFeb2017#&pageSet=29&page=0
(comment

  ;; see this for proper "nil casting": https://stackoverflow.com/questions/32641762/casting-nil-in-clojure
  (let [^String snil nil
        host (MonitoredHost/getMonitoredHost snil)
        active-vms (-> host .activeVms)
        vm (.getMonitoredVm host (VmIdentifier. "11078"))
        jvm-perf-counters (.findByPattern vm ".*")]
    (prn (MonitoredHost/getMonitoredHost snil))
    (prn vm)
    (doseq [counter jvm-perf-counters]
      (println (.getName counter) (.getValue counter)))
    )


  (let [ (-> (MonitoredHost/getMonitoredHost snil) .activeVms)]
    (prn active-vms))
;; end
  )


;;; Has this method ever been called inside a running JVM: https://stackoverflow.com/questions/55698109/has-this-method-ever-been-called-inside-a-running-jvm
;;; It requires sa-jdi.jar in classpath (comes with JDK 8).
(comment

  (import '(sun.jvm.hotspot.oops InstanceKlass Method))
  (import '(sun.jvm.hotspot.runtime VM))
  (import '(sun.jvm.hotspot.tools Tool))

  ;; end
  )


