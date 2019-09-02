(ns clojure-experiments.concurrency
  "Namespace related to concurrency and parallelism features of Clojure and related libraries."
  (:require [com.climate.claypoole :as cp]
            [criterium.core :as c]
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
          (catch Exception e#
            (println e#)
            (throw e#)))))

#_(logging-future (Thread/sleep 20) (/ 1 0))


;;; Now we get some extra goodies by preserving also the client stacktrace
;;; See https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
(defn- client-trace []
  (Exception. "Client stack trace"))

(defn logging-future+* [body]
  `(let [client-stack-trace# (client-trace)]
     (future 
       (try ~@body
            (catch Exception e#
              (log/error e#)
              (log/error client-stack-trace# "Submitting client stack-trace:")
              (throw e#))))))


(defmacro logging-future+
  "Logs any error that occurs during the execution of given body in a `future`
  *including* the client stack trace at the time of submitting the future for execution."
  [& body]
  (logging-future+* body))

#_(logging-future+ (Thread/sleep 1000) (throw (Exception. "ERROR!")))


;;; bindings
;;; See
;;; - https://stackoverflow.com/questions/20139463/clojure-binding-vs-with-redefs
(def ^:dynamic *a* nil)

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

