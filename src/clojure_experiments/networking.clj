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


;;; java.net and thread interruptions
;;; => most methods don't respond to thread interrupts
;;; so you will need to use timeouts
(comment
  (def httpf (future (do
                       (println (java.util.Date.) "started")
                       (try (clj-http.client/get "http://10.0.1.71")
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
