(ns clojure-experiments.parsers.clindex
  "clindex is a cool tool for indexing project dependencies:
   - https://github.com/jpmonettas/clindex
   Check also clograms: https://github.com/jpmonettas/clograms"
  (:require [clindex.api :as clindex]
            [datascript.core :as d]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

;; first you index a project folder for some platforms
(comment 
  (clindex/index-project! "./" {:platforms #{:clj}})

  ;; then retrieve the datascript db for the platform you want to explore
  (def db (clindex/db :clj))

  ;; now you can explore your code using datalog, pull or whatever you can run against datascript
  ;; lets query all the vars that start with "eval"
  (->> (d/q '[:find ?vname ?nname ?pname ?vline ?fname
              :in $ ?text
              :where
              [?fid :file/name ?fname]
              [?pid :project/name ?pname]
              [?nid :namespace/file ?fid]
              [?pid :project/namespaces ?nid]
              [?nid :namespace/name ?nname]
              [?nid :namespace/vars ?vid]
              [?vid :var/name ?vname]
              [?vid :var/line ?vline]
              [(str/starts-with? ?vname ?text)]]
            db
            "eval")
       (map #(zipmap [:name :ns :project :line :file] %))
       (pprint/print-table))

  ;;
  )
