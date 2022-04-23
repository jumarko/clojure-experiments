(ns clojure-experiments.java.processes
  "Experiments with OS processes.
  See `ProcessHandle` javadoc: https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.html"
  (:require [clojure.string :as str]
            [clojure.java.shell :as sh]
            [signal.handler :as signal]))

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


;;; Signal handling
;;; https://github.com/pyr/signal

;; How to install SIGCHLD handler
;; - this signal could be useful for asynchronouse reaping of zombies
;;   (see The Linux Programming Interface p. 555-557)
(comment
  (signal/with-handler :chld
    (println (java.util.Date.) "Sigchld received")))


;;; Creating sub-processes
(comment
  (time (sh/sh "/usr/local/bin/git" "clone" "https://jumarko:abc@github.com/jumarko/poptavka3"))

  ;; the sub-processes won't be interrupted - instead, it continues until it's done
  ;; If you also evaluate `signal/with-handler :chld` call above,
  ;; you will see 'Sigchld received' printed after 5 seconds
  (time (let [sleeping-future (future (println "future started")
                                 (sh/sh "sleep" "5")
                                 (println "future done"))]
     (Thread/sleep 3000)
     (println "Interrupting the waiting process")
     (future-cancel sleeping-future)
     (println "future canceled")))

  ;; this is to test some larger output - the file is 98 KB
  ;; you will notice, that here the subprocess is blocked until
  ;; we read enough data from the pipe (after the 10-second sleep in `my-sh` is over)
  ;; - this is because max pipe buffer size is 64 kB
  ;; https://unix.stackexchange.com/a/11954/63528
  (my-sh "cat" "resources/cars.json")


  ;; this simply doesn't show up as zombie at all
  (time (my-sh "/usr/bin/git" "clone" "https://jumarko:abc@github.com/jumarko/poptavka3"))

  ;; but maybe this??
  (time (my-sh "sleep" "5"))

  ;; if you don't have ssh key, this should fail although it's a valid clone url
  (time (sh/sh "git" "clone" "git@github.com:jumarko/poptavka.git "))


  (require '[clojure.java.io :refer (as-file copy)])
  (import '(java.io ByteArrayOutputStream StringWriter)
           '(java.nio.charset Charset))

  ;;; all of this is copied from `clojure.java.shell`
  ;;; the only reason is to inject Thread/sleep in the `my-sh` function
(def ^:dynamic *sh-dir* nil)
(def ^:dynamic *sh-env* nil)

(defmacro with-sh-dir
  "Sets the directory for use with sh, see sh for details."
  {:added "1.2"}
  [dir & forms]
  `(binding [*sh-dir* ~dir]
     ~@forms))

(defmacro with-sh-env
  "Sets the environment for use with sh, see sh for details."
  {:added "1.2"}
  [env & forms]
  `(binding [*sh-env* ~env]
     ~@forms))

(defn- parse-args
  [args]
  (let [default-encoding "UTF-8" ;; see sh doc string
        default-opts {:out-enc default-encoding :in-enc default-encoding :dir *sh-dir* :env *sh-env*}
        [cmd opts] (split-with string? args)]
    [cmd (merge default-opts (apply hash-map opts))]))

(defn- ^"[Ljava.lang.String;" as-env-strings 
  "Helper so that callers can pass a Clojure map for the :env to sh."
  [arg]
  (cond
   (nil? arg) nil
   (map? arg) (into-array String (map (fn [[k v]] (str (name k) "=" v)) arg))
   true arg))

(defn- stream-to-bytes
  [in]
  (with-open [bout (ByteArrayOutputStream.)]
    (copy in bout)
    (.toByteArray bout)))

(defn- stream-to-string
  ([in] (stream-to-string in (.name (Charset/defaultCharset))))
  ([in enc]
     (with-open [bout (StringWriter.)]
       (copy in bout :encoding enc)
       (.toString bout))))

(defn- stream-to-enc
  [stream enc]
  (if (= enc :bytes)
    (stream-to-bytes stream)
    (stream-to-string stream enc)))

(defn my-sh
  [& args]
  (let [[cmd opts] (parse-args args)
        proc (.exec (Runtime/getRuntime) 
               ^"[Ljava.lang.String;" (into-array cmd)
               (as-env-strings (:env opts))
               (as-file (:dir opts)))
        {:keys [in in-enc out-enc]} opts]
    (if in
      (future
        (with-open [os (.getOutputStream proc)]
          (copy in os :encoding in-enc)))
      (.close (.getOutputStream proc)))

    (Thread/sleep 10000)

    (with-open [stdout (.getInputStream proc)
                stderr (.getErrorStream proc)]
      (let [out (future (stream-to-enc stdout out-enc))
            err (future (stream-to-string stderr))
            exit-code (.waitFor proc)]
        {:exit exit-code :out @out :err @err}))))


  .)
