(ns clojure-experiments.tupelo
  "Tupelo library examples: https://github.com/cloojure/tupelo"
  (:require [tupelo.core :as t]))

;;; spy functions



;;; lazy-cons



;;; with-exception-default
(defn parse-with-default [str-val default-val]
  (try
    (Long/parseLong str-val)
    (catch Exception e
      default-val))) ; default value

(parse-with-default "66-Six" 42)
;;=> 42

(defn proc-num [n]
(when-not (number? n)
  (throw (IllegalArgumentException. "Not a number")))
n)

(defn proc-int [n]
  (when-not (int? n)
    (throw (IllegalArgumentException. "Not int")))
  n)

(defn proc-odd [n]
  (when-not (odd? n)
    (throw (IllegalArgumentException. "Not odd")))
  n)

(defn proc-ten [n]
  (when-not (< 10 n)
    (throw (IllegalArgumentException. "Not big enough")))
  n)

(defn process [arg]
  (with-exception-default 42  ; <= default value to return if anything fails
    (-> arg
        proc-num
        proc-int
        proc-odd
        proc-ten)))

(process nil)    ;=> 42
(process :a)     ;=> 42
(process "foo")  ;=> 42
(process 12)     ;=> 42

(process 13)     ;=> 13
