(ns clojure-experiments.bigdata.hive
  "Hive is SQL interface for big data stored on HDFS.
  It supports many different storages including S3.
  It is also used by Athena - see `clojure-experiments.aws.athena`"
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))


(defn json-array->hive
  "Converts a JSON file (java.io.File) containing a big array of objects
  to a format supported by Hive (requires one JSON document/object per line).
  See https://stackoverflow.com/questions/50401653/aws-athena-create-table-by-an-array-of-json-object"
  [in-file out-file]
  (with-open [in (io/reader in-file)
              out (io/writer out-file)]
    (let [json-array  (json/read in)]
      (run! (fn [item]
              (json/write item out)
              ;; append a new line, otherwise we would end up with a single-line file
              (.write out "\n"))
            json-array))))

(comment
  (def dir "/Users/jumar/Work/CodeScene/data-science/cloud-refactoring-recommendations/")
  (json-array->hive (str dir "2022-12-01_code_improvements_dump_full.json")
                    (str dir "2022-12-01_code_improvements_dump_full.hive.json"))
  .)
