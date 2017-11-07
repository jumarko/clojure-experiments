(ns clojure-repl-experiments.concurrency
  "Namespace related to concurrency and parallelism features of Clojure and related libraries."
  (:require [com.climate.claypoole :as cp]))


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


