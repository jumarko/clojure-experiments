(ns clojure-experiments.java.threads)

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
  (do (def fio
     (future 
       (try
         (println "slurping from the disk")
         (slurp "/Users/jumar/workspace/clojure/clojure-rte/java_pid73938.hprof")
         (println "slurped from the disk")
         (catch InterruptedException ie
           (println "Interrupted.")))))
      (Thread/sleep 100)
      (println "Interrupting...")
      (future-cancel fio)
      (println "Interrupted?"))
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
