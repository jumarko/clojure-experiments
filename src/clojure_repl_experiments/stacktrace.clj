(ns clojure-repl-experiments.stacktrace
  "Various utilities to examine java call stack et al.")

;;; Helper get-stack functions implemented for SICP (exercises 1.17 and 1.18)
;;; They can be used to get better overview of space "orders of growth"

(defn get-stack [fn-name]
  (->> (Thread/currentThread)
       .getStackTrace
       seq
       (filter #(.contains (.getClassName %)
                           (clojure.string/replace fn-name "-" "_")))))
(defn get-stack-depth
  [fn-name]
  (-> (get-stack fn-name)
      count
      ;; there are always two elements in stack per fn call - invokeStatic and invoke
      (#(/ % 2))))
