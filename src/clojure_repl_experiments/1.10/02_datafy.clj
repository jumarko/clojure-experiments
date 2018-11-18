(ns clojure-repl-experiments.1.10.02_datafy
  (:require [clojure.datafy :as d]
            [clojure.java.data :as java-data]))

;;;; datafy
;;;; https://clojureverse.org/t/what-use-cases-do-you-see-for-clojure-datafy/3143
;;;; https://www.birkey.co/2018-10-26-datafy-and-tap%3E-in-clojure-1.10.html
;;;; https://github.com/clojure/clojure/commit/93841c0dbf9db2f358474408d5e21530f49ef8b3
;;;; ===========================================================================

;; let us write an fn to give use any member that we would like to find more about:
(defn member-lookup [class member]
  (->> class
       d/datafy
       :members
       (filter (fn [[k v]] (= (symbol member) k)))))

(member-lookup String "intern")
;; => ([intern
;;      [#clojure.reflect.Method{:name intern,
;;                               :return-type java.lang.String,
;;                               :declaring-class java.lang.String,
;;                               :parameter-types [],
;;                               :exception-types [],
;;                               :flags #{:public :native}}]])

(d/nav)
