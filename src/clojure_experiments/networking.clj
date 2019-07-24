(ns clojure-experiments.networking
  (:require [clojure.java.io :as io]
            [clj-http.client :as http])
  )


;; How to check TLS certificate expiration date with Clojure?
;; https://stackoverflow.com/questions/54612465/how-to-check-tls-certificate-expiration-date-with-clojure
(defn get-server-certs [from]
  (let [url (io/as-url from)
        conn (.openConnection url)]
    (with-open [_ (.getInputStream conn)]
      (.getServerCertificates conn))))
(.getNotAfter (first (get-server-certs "https://www.google.com")))
;; => #inst "2019-04-17T09:15:00.000-00:00"
    

;;; clj-http playground: https://github.com/dakrone/clj-http
;;; Check https://github.com/public-apis/public-apis for list of Public APIs 
(comment

  (def facts (http/get "https://cat-fact.herokuapp.com/facts" {:as :json}))
  
  ;; end
  )
