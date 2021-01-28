(ns clojure-experiments.books.joy-of-clojure.ch12-java-next
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.string :as str])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]
           (javax.swing Box BoxLayout JTextField JPanel
                        JSplitPane JLabel JButton JOptionPane)
           (java.awt BorderLayout Component Container FlowLayout GridLayout)
           (java.awt.event ActionListener)))

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


;;; gen-class and gui programming (p. 286 - 292)

;; must be standalone namespace?
#_(ns joy.gui.DynaFrame
  (:gen-class
   :name joy.gui.DynaFrame
   :extends javax.swing.JFrame
   :implements [clojure.lang.IMeta]
   :prefix "df-" ; must be string
   :state state
   :init init
   ;; mapping of the class constructors to the superclass constructors
   :constructors {[String] [String]
                  [] [String]}
   :methods [[display [java.awt.Container] void]
             ^{:static true} [version [] String]])
  (:import (javax.swing JFrame JPanel JComponent)
           (java.awt BorderLayout Container)))

(compile 'clojure-experiments.books.joy-of-clojure.DynaFrame)
;; => clojure-experiments.books.joy-of-clojure.DynaFrame
;; notice that the actual name of the class is `joy.gui.DynaFrame` as stated in `:name` 
#_(joy.gui.DynaFrame. "First try")
;; Execution error (UnsupportedOperationException) at joy.gui.DynaFrame/<init> (REPL:-1).
;; clojure-experiments.books.joy-of-clojure.DynaFrame/df-init not defined

#_(joy.gui.DynaFrame. "Second try")
;; meta (clojure-experiments.books.joy-of-clojure.DynaFrame/df-meta not defined?)

(meta (joy.gui.DynaFrame. "Third try"))
;; => #:clojure-experiments.books.joy-of-clojure.DynaFrame{:title "Third try"}

;; note that this doesn't work - it's because my ns is named differently from the generated class
;; (joy.gui.DynaFrame/version)
;; No such var: joy.gui.DynaFrame/version

;; but using dot-special operator works
;; and notice that the function name must be with prefix too, that is `df-version`
(. joy.gui.DynaFrame version)
;; => "1.0"

(def gui (joy.gui.DynaFrame. "4th"))

#_(.display gui (doto (JPanel.)
                (.add (JLabel. "Charlemagne and Pippin"))))


;;; Let's build 3 GUI containers - shelves, stacks, splitters
(defn shelf [& components]
  (let [shelf (JPanel.)]
    (.setLayout shelf (FlowLayout.))
    (doseq [c components]
      (.add shelf c))
    shelf))

(defn stack [& components]
  (let [stack (Box. BoxLayout/PAGE_AXIS)]
    (doseq [c components]
      (.setAlignmentX c Component/CENTER_ALIGNMENT)
      (.add stack c))
    stack))

(defn splitter [top bottom]
  (doto (JSplitPane.)
    (.setOrientation JSplitPane/VERTICAL_SPLIT)
    (.setLeftComponent top)
    (.setRightComponent bottom)))

;; let's now provide basic widgets: buttons, labels, text boxes
(defn button [text f]
  (doto (JButton. text)
    ;; use reify instead of proxy: https://stackoverflow.com/questions/5821892/why-should-i-use-reify-instead-of-proxy-in-clojure
    #_(.addActionListener (proxy [ActionListener] [] (actionPerformed [_] (f))))
    (.addActionListener
     (reify ActionListener
       (actionPerformed [this _] (f))))))

(defn txt [cols t]
  (doto (JTextField.)
    (.setColumns cols)
    (.setText t)))

(defn label [txt] (JLabel. txt))

(defn alert
  ([msg] (alert nil msg))
  ([frame msg]
   (JOptionPane/showMessageDialog frame msg)))

#_(.display gui (splitter
               (button "Procrastinate" #(alert "Eat Cheetos"))
               (button "Move It" #(alert "Couch to 5k"))))

;; let's add one more widget builder
(defn grid [x y f]
  (let [g (doto (JPanel.)
            (.setLayout (GridLayout. x y)))]
    (dotimes [_ (* x y)]
      (.add g (f)))
    g))

(defn rich-ui [dyna-frame]
  (.display
   dyna-frame
   (let [g1 (txt 10 "Charlemagne")
         g2 (txt 10 "Pippin")
         r (txt 3 "10")
         d (txt 3 "5")]
     (splitter
      (stack
       (shelf (label "Player 1") g1)
       (shelf (label "Player 2") g2)
       (shelf (label "Rounds ") r (label "Delay  ") d))
