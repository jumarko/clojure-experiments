(ns clojure-experiments.logging-and-monitoring.jvm
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


  (let [
        active-vms (-> (MonitoredHost/getMonitoredHost snil) .activeVms)]
    (prn active-vms))
;; end
  )


;;; Has this method ever been called inside a running JVM: https://stackoverflow.com/questions/55698109/has-this-method-ever-been-called-inside-a-running-jvm
;;; It requires sa-jdi.jar in classpath (comes with JDK 8).
(comment

  (import '(sun.jvm.hotspot.oops InstanceKlass Method))


  ;; Note about HotSpot Serviceability Agent:
  ;; It only works out of process- suspends the JVM and it's quite slow
  ;; See https://stackoverflow.com/questions/61821224/how-to-access-jvm-internal-data-structures-using-the-hotspot-dynamic-attach-mech
  ;; 

  ;; VM: http://www.javamagazine.mozaicreader.com/JanFeb2017#&pageSet=32&page=0
  (import '(sun.jvm.hotspot HotSpotAgent))
  (import '(sun.jvm.hotspot.runtime VM))

  ;; need to initialize VM first by attaching
  ;; - this doesn't work -> would need to find a different way how to "attach"
  ;;      Can't ptrace attach to the process
  ;; (def hotspot-agent (HotSpotAgent.))
  ;; (.attach hotspot-agent (-> (java.lang.ProcessHandle/current) .pid int))

  ;; this doesn't have any effect on the `VM` I use below - it's still not initialized
  (import '(com.sun.tools.attach VirtualMachine))
  ;; inspired by `clj-async-profiler.core`
  (VirtualMachine/attach (-> (java.lang.ProcessHandle/current) .pid str))

  (def system-dict (-> (VM/getVM) .getSystemDictionary))


  (VM/initialize nil true)
  (VM/getVM)

  ;; end
  )


