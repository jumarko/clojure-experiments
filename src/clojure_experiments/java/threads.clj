(ns clojure-experiments.java.threads
  (:require [clojure.string :as str])
  (:import [java.util.concurrent Executors]
           [java.util Date]))

;; you can change thread stack size if JVM supports it:
;; - https://stackoverflow.com/questions/64829317/how-to-extend-stack-size-without-access-to-jvm-settings
;; - https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/Thread.html#%3Cinit%3E(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)

(defn rec [n]
  (if (pos? n)
    (+ 1 (rec (dec n)))
    0))

;; this will fail with the default thread stack size
#_(rec 10000)

;; but even larger number succeeds with larger stack size (5 MB?)
(comment 
  (.start (Thread. nil #(println (rec 50000)) "extended stack" 5000000))
  )


;;; interruptions & InterruptedException

(comment
  

  (def f (future (println "started")
                 (try
                   (Thread/sleep 10000)
                   (catch InterruptedException ie
                     (println "Interrupted.")))
                 (println "finished")))
  (future-cancel f)
  ;;=> prints:
  ;; started
  ;; Interrupted.
  ;; finished

  ;; try to interrupt disk io
  (let [big-file-path "/Users/jumar/workspace/git/git-trace.log"
        fio (future
              (try
                (println (Date.) "slurping from the disk")
                (slurp big-file-path)
                (println (Date.) "slurped from the disk")
                (catch InterruptedException ie
                  (println (Date.) "INTERRUPTED."))
                (catch Exception e
                  (println (Date.) "ERROR:" e))
                (catch Error e
                  (println (Date.) "FATAL ERROR:" e))))]
    (Thread/sleep 100)
    (println (Date.) "Interrupting...")
    (future-cancel fio)
    (println (Date.) "Interrupted?"))
      ;;=> prints:
  ;; slurping from the disk
  ;; Interrupting...
  ;; Interrupted?
  ;; slurped from the disk


  ,)

;;; Handy macro for setting thread names - improves debugging
(defmacro with-thread-name-suffix
  "Appends `thread-name-suffix` to the current thread name (separates them with \"___unix-time-millis_<currentTimeInMillis>__\"),
  executes the body,
  and then restores the original thread name.
  This can be useful for debugging - by temporarily setting thread name to something meaningful;
  we can get a useful piece of information immediately visible in the Thread stacks."
  [thread-name-suffix & body]
  `(let [current-name# (.getName (Thread/currentThread))]
     (.setName (Thread/currentThread)
               (str current-name# "___unix-time-millis_" (System/currentTimeMillis) "__" ~thread-name-suffix))
     (try
       ~@body
       ;; restore the original thread name
       (finally (.setName (Thread/currentThread) current-name#)))))


(defn dump-thread-stacks
  "Prints thread stacks for all running threads into stdout as per `Thread/getAllStackTraces`;
  See https://docs.oracle.com/en/java/javase/17/docs/api/java.management/java/lang/management/ThreadMXBean.html#dumpAllThreads(boolean,boolean,int)
  and ThreadInfo: https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/ThreadInfo.html

  NOTE: ThreadInfo class offers convenient string representation (.toString) which you can use to print basic thread information
  and a _subset_ of threads stacks (max 8 elements).
  See `thread-info-str` if you need a complete thread stack.
"
  []
  (let [thread-mbean (java.lang.management.ManagementFactory/getThreadMXBean)
        thread-infos (.dumpAllThreads thread-mbean true true)]
    thread-infos))

(defn thread-info-str
  "Returns string representation of ThreadInfo.
  Similar to ThreadInfo#toString but includes the whole stacktrace.
  See also
  - ThreadInfo.toString always cuts off the stack trace after 8 elements: https://bugs.openjdk.org/browse/JDK-8019366
  - ThreadInfo#toString impl: https://github.com/openjdk/jdk/blob/master/src/java.management/share/classes/java/lang/management/ThreadInfo.java#L676"
  [^java.lang.management.ThreadInfo thread-info]
  (str thread-info)
  (let [{:keys [stackTrace threadName threadId daemon priority threadState lockName lockOwnerName lockOwnerId suspended inNative] :as ti}
        (bean thread-info)
        header (format "\"%s\"%s prio=%s Id=%s %s%s%s%s%s\n"
                       threadName
                       (if daemon " daemon" "")
                       priority threadId threadState
                       (if lockName (str " on " lockName)
                           "")
                       (if lockOwnerName (format " owned by \"%s\" Id=%s" lockOwnerName lockOwnerId)
                           "")
                       (if suspended " (suspended)" "")
                       (if inNative " (in native)" ""))
        stack (str/join "\n\t " stackTrace)]
    (str header "\n" stack)))

(defn print-thread-stacks
  "As `dump-thread-stacks` but prints them via `println`.
  Notice that this means only limited stacks are printed (max 8 stack trace elements)
  - see https://bugs.openjdk.org/browse/JDK-8019366
  "
  []
  (run! println (mapv thread-info-str (dump-thread-stacks))))

(comment
  (print-thread-stacks)
  ;;
  )

;;; https://clojurians.slack.com/archives/C03S1KBA2/p1719422755625319
;;; Performance difference when threads reused immediately VS when sleeping between tasks
;;; They used fixed thread pool: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executors.html#newFixedThreadPool(int)
(defn fixed-thread-pool [n]
  (Executors/newFixedThreadPool n))

(comment
  ;; NOTE: this doesn't make much sense - I would have to be smarter when submitting and measuring
(let [ftp (fixed-thread-pool 6)]
  (mapv (fn [i]
          (future (.submit ftp
                           (fn []
                             (Thread/sleep (rand-int 5))
                             (println "Done: " i)))))
        (range 20)))

  ;;
  )

