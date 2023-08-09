(ns clojure-experiments.performance.jetty-c10k-problem
  "Simple web server for benchmarking jetty with Java Virtual Threads.
  See https://clojurians.slack.com/archives/C03S1KBA2/p1691305650647229"
  (:require
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.dev.pretty :as pretty]
   [ring.adapter.jetty9]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [ring.middleware.params :as params])
  (:import
   (java.util.concurrent Executors)
   (org.eclipse.jetty.util VirtualThreads)
   (org.eclipse.jetty.util.thread QueuedThreadPool)))


(defn make-router [routes]
  (ring/router
    routes
    {:exception pretty/exception
     :data      {:coercion   reitit.coercion.malli/coercion
                 :muuntaja   m/instance
                 ;; strip away empty stuff!
                 :middleware [;; query-params & form-params
                              params/wrap-params
                              muuntaja/format-negotiate-middleware
                              muuntaja/format-response-middleware
                              muuntaja/format-request-middleware
                              coercion/coerce-response-middleware
                              coercion/coerce-request-middleware
                              ;; exception/exception-middleware
                              ]}}))

(def request-count (atom 0))

(def routes
  ["/api" {:get
           {:handler (fn [_]
                       (swap! request-count inc)
                       (println "Request #" @request-count)
                       (Thread/sleep 100)
                       {:status 200
                        :body   {:status :ok}})}}])

(defn start-server []
  (println (System/getProperty "java.vendor"))
  (println (System/getProperty "java.version"))
  (println (System/getProperty "java.home"))

  (ring.adapter.jetty9/run-jetty
   (ring/ring-handler (make-router routes) (reitit.ring/create-default-handler) nil)
   {:port         3000
    :join?        false
    :configurator (fn [^org.eclipse.jetty.server.Server s]
                    (doseq [^org.eclipse.jetty.server.Connector c (.getConnectors s)]
                      (.setAcceptQueueSize c 65535)
                      (.setReuseAddress c true))
                    s)

    :thread-pool (doto
                     ;; https://eclipse.dev/jetty/javadoc/jetty-11/org/eclipse/jetty/util/thread/QueuedThreadPool.html
                     (QueuedThreadPool.
                   200                                         ; max platform threads
                   0                                           ; min platform threads
                   60000                                       ; thread idle timeout milliseconds
                   (java.util.concurrent.LinkedBlockingQueue.) ; use disruptor?
                   )

                   (.setVirtualThreadsExecutor
                    #_(VirtualThreads/getDefaultVirtualThreadsExecutor)
                    ;; as shown here: https://eclipse.dev/jetty/documentation/jetty-11/programming-guide/index.html#pg-arch-threads-thread-pool-virtual-threads
                    (Executors/newVirtualThreadPerTaskExecutor)))}))


(defn -main [& _]
  (start-server))

(comment
  (def my-server (start-server))
  (.stop my-server)
  )

