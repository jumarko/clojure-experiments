(ns clojure-experiments.concurrency
  "Namespace related to concurrency and parallelism features of Clojure and related libraries."
  (:require [com.climate.claypoole :as cp]
            [criterium.core :as c]))


;;; How to install UncaughtExceptionHandler for futures?
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (println ))))
;; unforunatelly, uncaught handler doesn't apply to futures
(future (do (Thread/sleep 20) (/ 1 0)))


(defmacro logging-future [& body]
  `(future
     (try ~@body
          (catch Exception e#
            (println e#)
            (throw e#)))))

(logging-future (Thread/sleep 20) (/ 1 0))



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

