(ns clojure-experiments.clojure-1.10.04-prepl
  (:require [clojure.core.server :refer [prepl io-prepl remote-prepl]:as server]
            [clojure.java.io :as io])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.net ServerSocket])) 

;;; read from socket
;;; https://github.com/clojure-cookbook/clojure-cookbook/blob/master/05_network-io/5-10_tcp-server.asciidoc
(defn receive
  "Read a line of textual data from the given socket"
  [socket]
  (.readLine (io/reader socket)))

(defn send-msg
  "Send the given string message out over the given socket"
  [socket msg]
  (let [writer (io/writer socket)]
    (.write writer msg)
    (.flush writer)))

(defn serve-single-client [port prepl-out-fn]
  (future
    (with-open [server-sock (ServerSocket. port)
                sock (.accept server-sock)]
      (send-msg sock "CONNECTED.\n> ")
      (prepl (LineNumberingPushbackReader. (io/reader sock))
             (partial prepl-out-fn sock)))))


(defmulti respond (fn [_socket {:keys [tag]}] tag))
(defmethod respond :tap [socket v]
  (println "TAP: not sending to client yet...."))
(remove-method respond :tap)

(defmethod respond :default [socket v]
  (send-msg socket (str v "\n> ")))
  

;;; run prepl
(comment
  (serve-single-client 3333 respond)

  ;; send some messages
  ;; notice that taps are usually delivered after the return value
  ;; because they are delivered in separate thread
  (map (fn [n] (tap> n) (inc n)) (range 10))

  (letfn [(fibonacci [n]
            (when (> n 30)
              (tap> n))
            (cond
              (= n 0) 0
              (= n 1) 1
              (> n 1) (+ (fibonacci (- n 1)) (fibonacci (- n 2)))))]
    (fibonacci 37))

  )




(defn serve-multi-clients
  ([port prepl-out-fn]
   (serve-multi-clients port prepl-out-fn {:max-clients 4}))
  ([port prepl-out-fn {:keys [max-clients]}]
   (with-open [server-sock (ServerSocket. port)]
     (let [clients-futures
           (mapv 
            (fn [_]
              (future
                (with-open
                  [sock (.accept server-sock)]
                  (send-msg sock "CONNECTED.\n")
                  (let [prepl-input-stream (LineNumberingPushbackReader. (io/reader sock))]
                    (prepl prepl-input-stream
                           (partial prepl-out-fn sock))))))
            (range max-clients))]
       (doseq [f clients-futures]
         @f)))))

(comment

  (serve-multi-clients 3333 respond)

  )


;;; Try remote (io-)prepl
;;; just launch socket server with io-prepl function
;;; clojure -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.10.0-beta7"}}}' -J-Dclojure.server.repl="{:port 0 :accept clojure.core.server/io-prepl}"
(defn out-fn [v]
  (prn "data: " v))
;; Don't use :repl/quit ! (just EOF)
;; (remote-prepl "localhost" 5555 *in* out-fn )
