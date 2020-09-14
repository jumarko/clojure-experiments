(ns clojure-experiments.codescene.api
  "Experiments with CodeScene's Open API.
  Check docs at http://localhost:3003/api/v1/docs/index.html"
  (:require [clj-http.client :as http]))


(comment
  (http/get
   "http://localhost:3003/api/v1/projects/1/analyses/latest/files"
   {:as :json
    :basic-auth ["api-user" "restapi"]
    :query-params {:page 1
                   :page_size 2}}
   ))
