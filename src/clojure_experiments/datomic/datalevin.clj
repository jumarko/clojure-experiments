(ns clojure-experiments.datomic.datalevin
  "https://github.com/juji-io/datalevin/blob/master/README.md
  See Installation instructions: https://github.com/juji-io/datalevin/blob/master/doc/install.md
  Requires following jvm opts in deps.edn:
               :jvm-opts [\"--add-opens=java.base/java.nio=ALL-UNNAMED\"
                          \"--add-opens=java.base/sun.nio.ch=ALL-UNNAMED\"]
"
  (:require
   [datalevin.core :as d]))

;;; https://github.com/juji-io/datalevin/blob/master/README.md#use-as-a-datalog-store

;; Define an optional schema.
;; Note that pre-defined schema is optional, as Datalevin does schema-on-write.
;; However, attributes requiring special handling need to be defined in schema,
;; e.g. many cardinality, uniqueness constraint, reference type, and so on.
(def schema {:aka  {:db/cardinality :db.cardinality/many}
             ;; :db/valueType is optional, if unspecified, the attribute will be
             ;; treated as EDN blobs, and may not be optimal for range queries
             :name {:db/valueType :db.type/string
                    :db/unique    :db.unique/identity}})

;; Create DB on disk and connect to it, assume write permission to create given dir
(def conn (d/get-conn "/tmp/datalevin/mydb" schema))

