(ns clojure-experiments.java.processes
  "Experiments with OS processes.
  See `ProcessHandle` javadoc: https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.html"
  (:require [clojure.string :as str]))

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

;; when-class macro copied from clojure.core because it's private
;; UPDATE: see also https://github.com/jeff303/java-case
(defmacro when-class [class-name & body]
  `(try
     (Class/forName ^String ~class-name)
     ~@body
     (catch ClassNotFoundException _#)))

(defn list-os-processes
  "Lists all native OS processes.
  Returns a vector of all the proceses represented as maps corresponding
  to the ProcessHandle.Info class: https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.Info.html.
  The function requires Java 9+ for proper function - returns nil for incompatible (older) versions."
  []
  (when-class "java.lang.ProcessHandle"
    (let [all-proc-fn (requiring-resolve 'clojure-experiments.java.processes.impl/all-processes)]
      (->> (all-proc-fn)
           .iterator
           iterator-seq
           (mapv process-info)))))

(defn filter-processes-by-command
  "Returns a vector of all processes matching given command.
  This can be a full path of the executable like '/usr/local/bin/git'
  or just a simple name like 'git'."
  [cmd]
  (->> (list-os-processes)
       (filterv (fn [{:keys [command]}]
                  (when command
                    (or (= command cmd)
                        (str/ends-with? command (str "/" cmd))))))))

(comment
  (filter-processes-by-command "git")
;; => [{:arguments ["log" "--graph" "--stat"],
;;      :command "/usr/local/bin/git",
;;      :command-line "/usr/local/bin/git log --graph --stat",
;;      :start-instant #object[java.time.Instant 0x2b50bea "2021-07-30T13:00:14.871Z"],
;;      :total-cpu-duration nil,
;;      :user "jumar"}]
  ,)


