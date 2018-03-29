(ns clojure-repl-experiments.repl
  (:require [clojure.java.io :as io]))

;;; here's a primitive REPL
;;; Suggested by Ch. Grand and recommended by guys from Clojure vienna meetup: 14.3.2018
(defn simple-repl []
  (->> (read)
       eval
       println
       (while true)))


;;; reading clojure file - example from Getting Clojure book:
(defn read-source [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (loop [result []]
      (let [expr (read r false :eof)]
        (if (= expr :eof)
          result
          (recur (conj result expr)))))))

#_(def my-source (read-source "/Users/jumar/workspace/clojure/clojure-repl-experiments/src/clojure_repl_experiments/repl.clj"))

;; another REPL by Russ - doesn't work properly for some reason!!!
(defn russ-repl []
  (loop []
    (println (eval (read)))
    (recur)))

;;; Here's Russ' eval implementation
(defn eval-symbol
  "Just lookup symbols in current namespace."
  [expr]
  (.get (ns-resolve *ns* expr)))

(defn eval-vector
  "Recursively evaluates vector's content."
  [expr]
  (vec (map reval expr)))

(defn eval-list
  "Recursively evaluates the content of the list and call it as a function."
  [expr]
  (let [evaled-items (map reval expr)
        f (first evaled-items)
        args (rest evaled-items)]
    (apply f args)))

(defn reval [expr]
  (cond
    (string? expr) expr
    (keyword? expr) expr
    (number? expr) expr
    (symbol? expr) (eval-symbol expr)
    (vector? expr) (eval-vector expr)
    (list? expr) (eval-list expr)
    :else :completely-confused))


(defn my-when [test body-fn]
  (if test
    (body-fn)))

(my-when (> 1 0) #(println "Hello"))
(my-when (< 1 0) #(println "Hello"))

(defmacro my-when-2 [test & body]
  `(if ~test
     (do ~@body)))

(my-when-2 (> 1 0) (println "Hello"))
(my-when-2 (> 1 0) (println "Hello") (println "another thing") (println "finally"))

(def counter (ref 0 ))
(dosync
 (commute counter (fn [x] (println "Hello") (inc x))))
