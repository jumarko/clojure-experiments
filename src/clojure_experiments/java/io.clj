(ns clojure-experiments.java.io
  "Experiment with reading files, etc.
  Also try babashka/fs library"
  (:import
   (java.time Duration)
   (org.apache.commons.io.input Tailer TailerListenerAdapter))
  (:require
   [clojure.java.io :as io]
   [babashka.fs :as fs]
   ))



;;; `tail -f` functionality
;;; https://stackoverflow.com/questions/557844/java-io-implementation-of-unix-linux-tail-f

;; Tailer class in commons-io: https://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/input/Tailer.html

(defn tailer [f handler-fn]
  (let [tailer (.get (doto (Tailer/builder)
                       (.setFile f)
                       (.setTailerListener (proxy [TailerListenerAdapter] []
                                             (handler [line]
                                               (handler-fn line))))
                       (.setDelayDuration (Duration/ofMillis 500))))]
    tailer))
;; this also doesn't work :(
(comment
  (def my-tailer (tailer "simple.log" println))
  (future (.run my-tailer))
  (.close my-tailer))

;; custom implementation
;; - it doesn't work very well, prints new lines multiple times!
(defn tailing
  [f keep-tailing-atom]
  (with-open [rdr (io/reader f)]
    (while @keep-tailing-atom
      (if-let [l (.readLine rdr)]
        (println "Read line: " l)
        (Thread/sleep 500)))
    (println "Tailing stopped.")))

(comment
  (def keep-tailing (atom true))
  (future (tailing "simple.log" keep-tailing))
  (reset! keep-tailing false)
  )

