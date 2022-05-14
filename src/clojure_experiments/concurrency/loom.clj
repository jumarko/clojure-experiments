(ns clojure-experiments.concurrency.loom
  "Here's a nice article about loom's virtual threads: https://ales.rocks/notes-on-virtual-threads-and-clojure
  See also http://cr.openjdk.java.net/~rpressler/loom/loom/sol1_part1.html#right-sizing-threads

  This requires JDK 19 with `--enable-preview`."
  (:require [clojure.core.async :as async])
  (:import (java.util.concurrent Executors)))

;; see https://download.java.net/java/early_access/loom/docs/api/java.base/java/lang/Thread.html
(defn thread-factory [name]
  (-> (Thread/ofVirtual)
      (.name name 0)
      (.factory)))

;; you can then use thread-factory e.g. for futures - they use clojure.lang.Agent/soloExecutor
(comment
  (set-agent-send-off-executor!
   (Executors/newThreadPerTaskExecutor
    (thread-factory "clojure-agent-send-off-pool-")))

  .)


(defn run-virtually-concurrently [sleep-times]
  ;; https://download.java.net/java/early_access/loom/docs/api/java.base/java/util/concurrent/Executors.html#newThreadPerTaskExecutor(java.util.concurrent.ThreadFactory)
  ;; TODO: shouldn't this be `Executors/newVirtualThreadPerTaskExecutor`??
  (let [executor (Executors/newThreadPerTaskExecutor (thread-factory "perfectly-scoped-pool-"))]
    (try
      (.submit executor ^Callable #(identity 2000))
      (.submit executor ^Callable #(prn "Starting a long running operation"))
      ;; isn't Thread/sleep still a problem?
      ;; => it should be fine: https://stackoverflow.com/questions/65333188/is-thread-sleep-different-with-virtual-threads-fibers-in-project-loom-for-ja/65333535#65333535
      ;;    - see 
      (dotimes [_ sleep-times] (.submit executor ^Callable #(Thread/sleep 10000)))
      (.submit executor ^Callable #(prn "Done."))
      4
      (finally (.close executor)))))

(defn run-os-concurrently [sleep-times]
  ;; https://download.java.net/java/early_access/loom/docs/api/java.base/java/util/concurrent/Executors.html#newThreadPerTaskExecutor(java.util.concurrent.ThreadFactory)
  (let [executor (Executors/newThreadPerTaskExecutor (Executors/defaultThreadFactory))]
    (try
      (dotimes [_ sleep-times] (.submit executor ^Callable #(Thread/sleep 10000)))
      (println "Done OS.")
      (finally (.close executor)))))

(comment
  ;; virtual threads
  (run-virtually-concurrently 1000)

  ;; native threads
  (run-os-concurrently 100)
  (run-os-concurrently 1000)
  ;; this still works on my machine -
  (run-os-concurrently 5000)
  ;; but with 10K threads I get OOM:
  ;;   unable to create native thread: possibly out of memory or process/resource limits reached
  (run-os-concurrently 10000)

  .)

;; reporting function
(defn report-free-memory-in-mb [interval-ms]
  (Thread/sleep interval-ms)
  (println (format "%.2f MB" (double (/ (.freeMemory (Runtime/getRuntime)) 1024 1024)))))

(comment
  (def watchdog (future (while true (report-free-memory-in-mb 1000))))
  (future-cancel watchdog)

  .)


;;; Sean Corfield's experiment with core.async - using virtual threads for blocking operations
;;; https://clojurians.slack.com/archives/C8NUSGWG6/p1652487349607519?thread_ts=1652433406.842079&cid=C8NUSGWG6

(defonce ^:private go-factory! (thread-factory "go-pool-"))

(defmacro go! [& body]
  `(let [c# (async/chan)
         t# (.newThread go-factory!
                        ^:once
                        (fn* []
                             (try
                               (async/>!! c# (do ~@body))
                               (finally
                                 (async/close! c#)))))]
     (.start t#)
     c#))

(defmacro go-loop! [binding & body]
  `(go! (loop ~binding ~@body)))

(comment

  ;; simple way to inspect channels values
  (defn record-val [x]
    (prn "Tapped: " x))
  (add-tap record-val)

  (let [c (async/chan)]
    (go-loop! [ns (range 10)]
              (when (seq ns)
                (async/>!! c (first ns))
                (recur (rest ns))))
    (go-loop! []
              (tap> (async/<!! c))
              (recur)))

  )
