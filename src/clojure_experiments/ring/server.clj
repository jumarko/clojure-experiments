(ns clojure-experiments.ring.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [compojure.core :refer [GET POST defroutes]]))

(defonce ^:private server (atom nil))


(defn wrap [handler]
  (-> handler
      wrap-json-response
      (wrap-json-body {:keywords? true})))


(defn translate
  [json]
  "Translated JSON")

(defn process-translate
  [req]
  (println "In process-translate:" req)
  (if-let [body (:body req)]
    {:status 200 :headers {"Content-Type" "application/text" "Character-Encoding" "utf8"} :body (translate body)}
    "Bad body"))

(defroutes app
  (GET "/" [] "mercator-server version 0.0.1")
  (POST "/translate" req (process-translate req))
  (POST "/translate/" req (process-translate req)))

(defn stop-server []
  (when @server
    (.stop @server))
  )

(defn start-server
  [port]
  (stop-server)
  (println "Starting server at port: " port)
  (let [jetty-server (run-jetty
                      (wrap app)
                      {:port  port
                       :join? false})]
    (reset! server jetty-server)))


(comment
  (start-server 3333)
  (stop-server))

