(ns clojure-experiments.stacktrace
  "Various utilities to examine java call stack et al."
  (:require [haystack.parser :as stacktrace.parser]
            [haystack.analyzer :as stacktrace.analyzer]
            [clojure.string :as str]))

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


;;; Haystack - library for parsing stacktraces extracted from Cider
;;; https://github.com/clojure-emacs/haystack/

;; - this stacktrace is from logs - unfortunately, parser isn't able to make anything sensible out of it
(def my-stacktrace-data
  (stacktrace.parser/parse (slurp "/Users/jumar/workspace/empear/src/codescene/cloud/web/stacktrace.txt")))


(defn throw! []
  (ex-info "ERROR" {:name :hello}))

(def my-stacktrace
  (->> (stacktrace.analyzer/analyze (try (throw!) (catch Exception e e)))
       first
       :stacktrace))
(first (map keys my-stacktrace))
;; => (:name :file :line :class :method :type :flags :file-url)

(set (mapcat :flags my-stacktrace))
;; => #{:dup :tooling :java :clj}

(->> my-stacktrace
     (filter #(contains? (:flags %) :project))
     (map #(select-keys % [:var :line 30 :name :file :file-url :flags])))
