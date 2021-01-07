(ns clojure-experiments.books.joy-of-clojure.ch12-java-next
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]))

(def OK java.net.HttpURLConnection/HTTP_OK)

;;; proxy (p. 285)

;; server
(defn new-server [port path handler]
  (doto (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))

;; request handler
(defn respond
  ([exchange body]
   (respond identity exchange body))
  ([around exchange body]
   (.sendResponseHeaders exchange OK 0)
   (with-open [resp (around (.getResponseBody exchange))]
     (.write resp (.getBytes body)))))

(defn default-handler [txt]
  (proxy [HttpHandler] []
    (handle [exchange] (respond exchange txt))))


;; create the server
(comment

  ;; => go to http://localhost:8123/joy/hello
  (def server (new-server 8123, "/joy/hello" (default-handler "Hello Cleveland")))
  (.stop server 0)

  ;;
  )

;; now add more dynamism to be able to change the handler independently
(def p (default-handler "There's no problem that can't be solved with another level of indirection"))
(comment
  ;; http://localhost:8123/
  (def server (new-server 8123 "/" p))

  ;;
  )

;; now use `update-proxy` to avoid restarting the server
(update-proxy p {"handle"
                 (fn [this exchange] (respond exchange (str "this is " this)))})

(defn echo-handler [_ exchange]
  (let [headers (.getRequestHeaders exchange)]
    (respond exchange (prn-str headers))))
(update-proxy p {"handle" echo-handler})

;; `proxy-mappings` examine the existing mappings
(proxy-mappings p)
;; => {"handle" #function[clojure-experiments.books.joy-of-clojure.ch12-java-next/echo-handler]}

;;; This is cool! Simple browser of file system
;;; (p. 282 - 285)

;; `around` filter works with OutputStream (.getResponseBody returns that)
;; so if we want to implement a custom filter it must return a compatible object
(defn html-around [o]
  (proxy [FilterOutputStream] [o]
    (write [raw-bytes]
      ;; Note: proxy-super is NOT thread safe!
      ;;   If some other thread were to call this proxy instance while proxy-super was still running,
      ;;   the base class's method would be called directly, incorrectly skipping the proxy implementation!
      (proxy-super write (.getBytes (str "<html><body>" (String. raw-bytes) "</body></html>"))))))

(defn listing [file]
  (-> file .list sort))
(filter #(string/ends-with? % ".clj")
        (listing (io/file ".")))
;; => ("foo2.clj" "lein-profiles.clj" "project.clj" "read-line.clj")

(defn html-links [root filenames]
  (string/join (for [file filenames]
                 (str "<a href='"
                      (str root
                           (if (= "/" root) "" File/separator)
                           file)
                      "'>"
                      file "<a/><br>"))))

(->> (listing (io/file "."))
     (filter #(string/ends-with? % ".clj"))
     (html-links "."))
;; => "<a href='./foo2.clj'>foo2.clj<a/><br><a href='./lein-profiles.clj'>lein-profiles.clj<a/><br><a href='./project.clj'>project.clj<a/><br><a href='./read-line.clj'>read-line.clj<a/><br>"

;; ... we have a function to list a directory
;; but we also want to deal with files ...
(defn details [file]
  (format "%s is %d bytes" (.getName file) (.length file)))
(details (io/file "README.md"))
;; => "README.md is 215 bytes"

(defn uri->file [root uri]
  (->> uri str URLDecoder/decode (str root) io/file))

(uri->file "." (URI. "/project.clj"))
;; => #object[java.io.File 0x3cbb2f9f "./project.clj"]
(details (uri->file "." (URI. "/project.clj")))
;; => "project.clj is 13123 bytes"

(defn fs-handler [_ exchange]
  (let [uri (.getRequestURI exchange)
        file (uri->file "." uri)]
    (if (.isDirectory file)
      (do (.add (.getResponseHeaders exchange)
                "Content-Type" "text/html")
          (respond html-around exchange (html-links (str uri) (listing file))))
      (respond exchange (details file)))))
#_(update-proxy p {"handle" fs-handler})
