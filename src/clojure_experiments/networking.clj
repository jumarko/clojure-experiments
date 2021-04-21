(ns clojure-experiments.networking
  (:require [clj-http.client :as http]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [ring.util.codec :as codec]
            [vcr-clj.cassettes :as vcr-cassettes]
            [vcr-clj.cassettes.serialization :as vcr-serialization]
            [vcr-clj.clj-http :as vcr-http]))

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


;;; java.net and thread interruptions
;;; => most methods don't respond to thread interrupts
;;; so you will need to use timeouts
(comment
  (def httpf (future (do
                       (println (java.util.Date.) "started")
                       (try (http/get "http://10.0.1.71")
                            (println (java.util.Date.) "finished")
                            (catch Exception e
                              (println (java.util.Date.) "Interrupted?")
                              (.printStackTrace e))))))
  (realized? httpf)
  ;; => false

  ;; this doesn't really interrupt the thread - it will stay blocked in the waiting for the connection
  ;; until the system-level timeout (75 seconds on my MacOS) is reached
  (future-cancel httpf)

  ;; the code block above will print something like this:
;;   #inst "2021-04-21T06:25:33.159-00:00" started
;;   #inst "2021-04-21T06:26:49.085-00:00" Interrupted?
;; java.net.ConnectException: Operation timed out (Connection timed out)
;; 	at java.base/java.net.PlainSocketImpl.socketConnect(Native Method)
;; ...
;; 	at java.base/java.net.Socket.connect(Socket.java:591)
;; 	at org.apache.http.conn.socket.PlainConnectionSocketFactory.connectSocket(PlainConnectionSocketFactory.java:75)
;; 	at org.apache.http.impl.conn.DefaultHttpClientConnectionOperator.connect(DefaultHttpClientConnectionOperator.java:142)
;; 	at org.apache.http.impl.conn.BasicHttpClientConnectionManager.connect(BasicHttpClientConnectionManager.java:313)
;; ...
;; 	at clj_http.client$get.doInvoke(client.clj:1167)


  ,)

;;; capture IO with vcr-clj: https://github.com/gfredericks/vcr-clj
(comment

 (vcr-http/with-cassette :record
   (http/get "https://www.stats.govt.nz/assets/Uploads/Business-price-indexes/Business-price-indexes-September-2020-quarter/Download-data/business-price-indexes-september-2020-quarter-corrections-to-previously-published-statistics.csv"))
 
    ;; check where the cassete file is stored:
 (vcr-clj.cassettes/cassette-file :record)  
;; => #object[java.io.File 0x2a586674 "/Users/jumar/workspace/clojure/clojure-experiments/cassettes/record.edn"]

 (defn decode-body [base64-encoded-strings]
   (-> base64-encoded-strings
       vcr-serialization/maybe-join
       codec/base64-decode
       (String.)))

 ;; 
  (with-open [r (java.io.PushbackReader. (io/reader (vcr-cassettes/cassette-file :record)))]
   (let [as-edn (edn/read {:readers  (assoc vcr-serialization/data-readers
                                            'vcr-clj/input-stream decode-body)}
                          r)
         body (-> as-edn :calls (get 0) :return :body)]
     (println body)))
    ,)
