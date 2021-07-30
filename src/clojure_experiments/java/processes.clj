(ns clojure-experiments.java.processes
  "Experiments with OS processes.
  See `ProcessHandle` javadoc: https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.html"
  (:require [clojure.string :as str])
  (:import [java.lang ProcessHandle]))

(defn process-info
  "Accepts an instance of java.lang.ProcessHandle 
  and converts it to a map corresponding to the attributes of
  ProcessHandle.Info class (https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.Info.html)"
  [process-handle]
  (let [info (.info process-handle)
        orr (fn [optional] (.orElse optional nil))]
    {:arguments (some-> (orr (.arguments info))
                        vec)
     :command (orr (.command info))
     :command-line (orr (.commandLine info))
     :start-instant (orr (.startInstant info))
     :total-cpu-duration (orr (.totalCpuDuration info))
     :user (orr (.user info))}))


(defn list-os-processes
  "Lists all native OS processes.
  Returns a vector of all the proceses represented as maps corresponding
  to the ProcessHandle.Info class: https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.Info.html"
  []
  (->> (ProcessHandle/allProcesses)
       .iterator
       iterator-seq
       (mapv process-info)))

(comment
  (->> (list-os-processes)
       (filterv #(some-> % :command (str/ends-with? "git"))))
;; => [{:arguments ["log" "--graph" "--stat"],
;;      :command "/usr/local/bin/git",
;;      :command-line "/usr/local/bin/git log --graph --stat",
;;      :start-instant #object[java.time.Instant 0x2b50bea "2021-07-30T13:00:14.871Z"],
;;      :total-cpu-duration nil,
;;      :user "jumar"}]
  ,)
