(ns clojure-experiments.walk
  (:require [clojure.string :as string]
            [clojure.walk :as w]))

(w/walk prn #(println "RESULT: " %) [[1 2] {:a 1 :b 20} [{:aa 1000}]])


(defn transform-keys
  "Recursively transforms all map keys in coll with the transform-key fn.
  See https://stackoverflow.com/questions/25787001/hyphenating-underscored-clojure-map-keys-for-database-results
  and `walk/keywordize-keys`"
  [transform-key coll]
  (letfn [(transform [x] (if (map? x)
                           (into {} (map (fn [[k v]] [(transform-key k) v]) x))
                           x))]
    (w/postwalk transform coll)))

(defn dash->underscore [akey]
  (cond
    (keyword? akey)
    (keyword (string/replace (name akey) \- \_))

    (string? akey)
    (string/replace (name akey) \- \_)

    :dont-know-how-to-transform
    akey))

(defn to-db-keys-with-underscores
  "Replaces all string or keyword keys in the map containing dashes to corresponding keys with underscores
  to conform to the SQL syntax requirements.
  Works recursively for (i.e. nested maps supported)."
  [m]
  (transform-keys dash->underscore m))

(comment
  (to-db-keys-with-underscores {:job-id 123 :project-name "abc" :results-dir {:absolute-path "/x/y/z"}})
  ;; => {:job_id 123, :project_name "abc", :results_dir {:absolute_path "/x/y/z"}}

  ;; works on strings too
  (to-db-keys-with-underscores {"job-id" 123 "project-name" "abc" "results-dir" {"absolute-path" "/x/y/z"}})
  ;; => {"job_id" 123, "project_name" "abc", "results_dir" {"absolute_path" "/x/y/z"}}

  ;; should keep non string/keyword keys untouched
  (to-db-keys-with-underscores {"job-id" 123 "project-name" "abc" "results-dir" {1 "/x/y/z"}})
  ;; => {"job_id" 123, "project_name" "abc", "results_dir" {1 "/x/y/z"}}

  ;;
  )
