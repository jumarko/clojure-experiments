(ns clojure-experiments.concurrency
  "Namespace related to concurrency and parallelism features of Clojure and related libraries."
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [taoensso.timbre :as log]))

;;; How to install UncaughtExceptionHandler for futures?
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (println ))))
;; unforunatelly, uncaught handler doesn't apply to futures
#_(future (do (Thread/sleep 20) (/ 1 0)))


(defmacro logging-future [& body]
  `(future
     (try ~@body
          (catch Throwable e#
            (println e#)))))

#_(logging-future (Thread/sleep 20) (/ 1 0))
(let [start (System/nanoTime)] 
  (type (/ (double (- (System/nanoTime) start)) 
           1000000.0)))

(let [start (System/nanoTime)]
  (type (/ (- (System/nanoTime) start)
           1000000.0)))

;;; Now we get some extra goodies by preserving also the client stacktrace
;;; See https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
(defn logging-future+* [file line body]
  `(let [client-stack-trace# (Exception. "Client stack trace")]
     (future
       (try ~@body
            (catch Throwable e#
              (log/error e# "Unhandled exception at:"
                       ~file "line:" ~line
                       "on thread:"
                       (.getName (Thread/currentThread)))
              (log/error client-stack-trace# "client stack trace:"))))))
(defmacro logging-future+
  "Logs any error that occurs during the execution of given body in a `future`
  *including* the client stack trace at the time of submitting the future for execution."
  [& body]
  (logging-future+* *file* (:line (meta &form)) body))
#_(logging-future+ (Thread/sleep 1000) (throw (Exception. "ERROR!")))

;; an improved version of logging-future (logged-future) shared by Sean Corfield:
;; https://groups.google.com/u/1/g/clojure/c/t3Pp8l9Pe4A
(defmacro logged-future
  "Given a body, execute it in a try/catch and log any errors."
  [& body]
  (let [line (:line (meta &form))
        file *file*]
    `(future
       (try
         ~@body
         (catch Throwable t#
           (println t# "Unhandled exception at:"
                    ~file "line:" ~line
                    "on thread:"
                    (.getName (Thread/currentThread))))))))

#_(logged-future (Thread/sleep 1000) (throw (Exception. "ERROR!")))

;;; pmap related experiments
;;; `map-throttled` it's similar to `pmap` but runs in the same thread
;;; and never consumes more than given `max-n` elements

;; `rechunk` here: https://clojuredocs.org/clojure.core/chunk#example-5c9cebc3e4b0ca44402ef6ec
;; todo: check throttler: http://brunov.org/clojure/2014/05/14/throttler/
(defn re-chunk [n xs]
  (lazy-seq
   (when-let [s (seq (take n xs))]
     (let [cb (chunk-buffer n)]
       (doseq [x s] (chunk-append cb x))
       (chunk-cons (chunk cb) (re-chunk n (drop n xs)))))))

(defn map-throttled
  "like `map` but never realizes more than `max-n` elements ahead of the consumer of the return value.
  useful for cases like an rate limited asynchronous http api (e.g. startquery aws cloudwatch insights api).
  uses `re-chunk`."
  [max-n f coll]
  (map f (re-chunk max-n coll)))

(comment

  (def a (map-throttled
          1
          (fn [i]
            (println "Iteration " i)
            (println "fetch a")
            (println "fetch b")
            (println "fetch c")
            (println "fetch d"))
          (range 4)))

  ;; this will print Iteration 1...
  (take 1 a)
  ;; this will print Iteration 2...
  (take 2 a)


  ;;
  )

;;; bindings
;;; See
;;; - https://stackoverflow.com/questions/20139463/clojure-binding-vs-with-redefs
(def ^:dynamic *a* nil)

;; you can rebound functions e.g. - but still they have to be declared as dynamic
(defn ^:dynamic my-dynamic-f [a b]
  (+ a b))

(defn with-logging [f]
  (fn [& args]
    (println "args:" args)
    (let [ret (apply f args)]
      (println "ret:" ret))))

(defn call-it [a b]
  (println "Call it!")
  (my-dynamic-f a b))

(binding [my-dynamic-f (with-logging my-dynamic-f)]
  (call-it 10 20))



(comment
  
  ;; this will print "*a* is nil"
  (binding [*a* 2]
    (doto (Thread. #(println "*a* is " *a*)) .start .join))

  ;; HOWEVER, using binding-conveyor-fn we can make it work
  ;; this will print "*a* is 2"
  	(binding [*a* 2]
      (doto (Thread. (#'clojure.core/binding-conveyor-fn  #(println "*a* is " *a*))) .start .join)))

;;; Claypoole: Threadpool tools for Clojure
;;; https://github.com/TheClimateCorporation/claypoole
;;; 
;;; To use claypoole, make a threadpool via threadpool and use it with claypoole's version of one of these standard Clojure functions:
;;; future
;;; pmap
;;; pcalls
;;; pvalues
;;; for
;;; doseq
;;; run!

(defn compute [input]
  (reduce (fn [acc x] (+ acc (reduce + (range x))))
          (range input)))

(def my-input [10000 20000 15000 5000])
#_(time (doall (map compute my-input)))
;;=> 13,564 ms
#_(time (->> (cp/pmap 4 compute my-input)
             (println "Finished")))
;;=> 8,213 ms


;;; How to execute some Clojure futures in a single thread?
;;; https://stackoverflow.com/questions/47998199/how-to-execute-some-clojure-futures-in-a-single-thread
(defn task-1 []
  (println "task 1...")
  (Thread/sleep 1000))
(defn task-2 []
  (println "tak 2...")
  (Thread/sleep 2000))
(defn task-3 []
  (println "task 3...")
  (Thread/sleep 3000))
(defn start-async-calc []
  (let [f1 (promise)
        f2 (promise)
        f3 (promise)]
    (future
      (deliver f1 (task-1))
      (deliver f2 (task-2))
      (deliver f3 (task-3))
      (println "DONE"))
    {:task1 f1
     :task2 f2
     :task3 f3}))
#_(start-async-calc)



;;; Executor Service
(import (java.util.concurrent Executors ExecutorService))

(def service (Executors/newFixedThreadPool 4))
;; Notice if you don't use type hint it will run it as Runnable instead of Callable
;; and you'll get nil as a result
(comment
  (def f (.submit ^ExecutorService service
                 ^Callable (fn []
                             (println "starting")
                             (Thread/sleep 5000)
                             (println "finished.")
                             :value))))

;;; Limit blocking tasks to given timeout
;;; Useful for bounding total time of an HTTP call
(defn- with-timeout [timeout task-fn timeout-failed-fn]
  (let [task (future (task-fn))
        task-result (deref task timeout :timeout)]
    (if (= :timeout task-result)
      (do 
        (if (future-cancel task)
          ;; TODO: replace with `log/debug` in production code
          (println "Task cancelled")
          (println "Task could not be canceled - maybe it's already finished."))
        (timeout-failed-fn))
      task-result)))



;;; Use locking to prevent concurrent writers corrupt JSON data stored in a file
;; It used to be like this:
(defn- known-errors-from
  [destination]
  (if (.exists (io/as-file destination))
    (json/parse-stream (io/reader destination) keyword)
    []))

(defn- persist-to-file
  [{:keys [path-fn] :as _context}
   category
   {:keys [description] :as details}]
  (try
    (let [destination (path-fn "errors.json")
          known-errors (known-errors-from destination)
          updated-errors (conj known-errors {:category category :error (:error details)})]
      (json/generate-stream updated-errors (io/writer destination))
      updated-errors)
    (catch Throwable t
      (log/error t "Failed to persist the reported error: " description))))

;; Then it was changed to this:
(let [lk (Object.)]
  (defn- known-errors-from 
    "Read known errors from file - call only with lock taken!"
    [destination]
    (if (.exists (io/as-file destination))
      (with-open [r (io/reader destination)]
        (doall (json/parse-stream r keyword)))
      []))
  
  (defn- write-errors
    "Write errors to file - call only with lock taken"
    [errors destination]
    (with-open [w (io/writer destination)]
      (json/generate-stream errors w)))
  
  (defn- with-lock-persist-to-file
    "Persisting errors to file must be thread safe.
     Use a local lock and make sure everything is:
     - realized before taking the lock,
     - persisted when releasing the lock"
    [path-fn log-entry]
    (let [destination (path-fn "error.json")]
      (locking lk
        (let [known-errors (known-errors-from destination)
              updated-errors (conj known-errors log-entry)]
          (write-errors updated-errors destination)
          updated-errors)))))

(defn persist-to-file
  [{:keys [path-fn] :as _context}
   category
   {:keys [description] :as details}]
  (try
    (with-lock-persist-to-file path-fn {:category category :error (:error details)})
    (catch Throwable t
      (log/error t "Failed to persist the reported error: " description))))

;; finally blocks vs interrupted threads => works just fine
(comment
  (do 
    (def my-future
      (future
        (try
          (Thread/sleep 10000)
          (println "Normally.")
          (catch Exception e
            (println "Catching...")
            (Thread/sleep 2000)
            (println "Catched."))
          (finally
            (Thread/sleep 1000)
            (println "Finally!")))))
    ;; finally still executed
    (future-cancel my-future)
    ;; Notice that it's marked as "done" and "cancelled" immediatelly after the cancellation
    ;; - but the thread running the task can still be doing other things indefinetely;
    ;;   e.g. sleeping in the finally block as above
    (println "cancelled?" (future-cancelled? my-future))
    (println "done?" (future-done? my-future))
    (println "cancelled")
    (try @my-future (catch Exception e (println (.getMessage e))))
    (println "dereferenced"))
  ;; => you'll immediately see this:
  ;; Catching...
  ;; cancelled? true
  ;; done? true
  ;; cancelled
  ;; nil
  ;; dereferenced
  ;; <after 2 seconds>
  ;; Catched.
  ;; <after 10 seconds>
  ;; Finally!


  (def my-thread (Thread.
                  (fn []
                    (try
                      (Thread/sleep 10000)
                      (println "Normally.")
                      (finally
                        (println "Finally!"))))))
  (.start my-thread)
  ;; finally still executed
  (.interrupt my-thread)
  ;;
  )


;; realized? is block on delay while it's waiting for realization:
(def myd (delay (Thread/sleep 2000)))
;; returns immediatelly
(realized? myd)
;; => false
(comment 
  (future @myd)
  ;; blocked until the delay is "realized" in the future above
  (realized? myd))


;;; `with-local-vars`
;;; Check some resources:
;;; - with-local-vars vs. let https://groups.google.com/g/clojure/c/j_FyAasCHXY
;;; - Clojure with-local-vars in closure: https://stackoverflow.com/questions/42040754/clojure-with-local-vars-in-closure
(with-local-vars
  [x y
   y x
   z 10]
  (list (var-get x) (var-get y) (var-get z)))
;; => (#<Var: --unnamed--> #<Var: --unnamed--> 10)



;; https://clojurians.slack.com/archives/C03S1KBA2/p1672857305344059
(defn periodically [func millis]
  (let [p (promise)]
    (future
      (while (= (deref p millis :timeout) :timeout)
        (func)))
    #(deliver p :cancel)))
(comment
  (def myp (periodically (fn [] (println "Hello"))
                         1000))

  (myp)

  .)
