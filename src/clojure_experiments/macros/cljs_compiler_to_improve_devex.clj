(ns clojure-experiments.macros.cljs-compiler-to-improve-devex
  "This is Roman Liutikov's video: https://www.youtube.com/watch?v=OVBmq7fmBrI
  Shared on Clojurians slack: https://clojurians.slack.com/archives/C8NUSGWG6/p1761238163135419
  See here to see how the warning looks like in the browser: https://youtu.be/OVBmq7fmBrI?t=358
"
  (:require
   [clojure.spec.alpha :as s]))


(s/def :sql/select keyword?)
(s/def :sql/from keyword?)
(s/def :sql/where (s/cat :field keyword?
                         :op keyword?
                         :value number?))
(s/def :sql/query (s/keys :req-un [:sql/select :sql/from :sql/where]))

(def query {:select :*
            :from :users
            :where [:id '= 1]})

;; Not valid because `:op` is supposed to be a keyword, not a symbol
(s/valid? :sql/query query)
(s/explain-data :sql/query query)
;; => #:clojure.spec.alpha{:problems ({:path [:where :op], :pred clojure.core/keyword?, :val =, :via [:sql/query :sql/where], :in [:where 1]}), :spec :sql/query, :value {:select :*, :from :users, :where [:id = 1]}}


;; Now, we would like to provide users (devs) a macro that takes a query
;; and turns it into a String at compile-time.
(defmacro sql [query]
  ;; first, we must validate the query
  (assert (s/valid? :sql/query query) "Invalid SQL query")
  (let [ast (s/conform :sql/query query)]
    "..."))

(comment
  (sql query)
  ;;=> Assert failed: Invalid SQL query
  ;;
  )


;; But maybe we don't want to throw an error,
;; instead we just want to emit a _warning_ to the developer
;; For this, Roman is using `cljs.analyzer/warning` from CLJS compiler
;; See here to see how the warning looks like in the browser: https://youtu.be/OVBmq7fmBrI?t=358
(defmacro sql [query]
  ;; first, we must validate the query
  #_(assert (s/valid? :sql/query query) "Invalid SQL query")
  (when-not (s/valid? :sql/query query)
    ;; NOTE: only works in ClojureScript!
    #_(cljs.analyzer/warning :sql/invalid-query &env nil)
    )
  (let [ast (s/conform :sql/query query)]
    "..."))


