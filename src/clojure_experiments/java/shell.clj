(ns clojure-experiments.java.shell
  (:require [clojure.java.io :as io])
  (:import [java.io ByteArrayOutputStream StringWriter]
           java.nio.charset.Charset))

;;; Like clojure.java.shell but experimenting what happens if we do not read from the output stream
;;; - will the subprocess blocks?
(defn- stream-to-bytes
  [in]
  (with-open [bout (ByteArrayOutputStream.)]
    (clojure.java.io/copy in bout)
    (.toByteArray bout)))

(defn- stream-to-string
  ([in] (stream-to-string in (.name (Charset/defaultCharset))))
  ([in enc]
   (with-open [bout (StringWriter.)]
     (io/copy in bout :encoding enc)
     (.toString bout))))

(defn- stream-to-enc
  [stream enc]
  (if (= enc :bytes)
    (stream-to-bytes stream)
    (stream-to-string stream enc)))


(comment
  
  (let [proc (.exec (Runtime/getRuntime)
                    "git -C /Users/jumar/workspace/clojure/clojure-experiments log --stat")]
    ;; we do nothing with process's STDIN now so just close it
    (.close (.getOutputStream proc))
    (with-open [stdout (.getInputStream proc)
                stderr (.getErrorStream proc)]
      (let [out (future (stream-to-string stdout))
        ;; this is a tiny modification avoiding reading from process's STDOUT
        ;; => This prevents sub-process from completion (if the OS's buffer isn't large enough)
        ;; (let [out (future stdout)
            err (future (stream-to-string stderr))
            exit-code (.waitFor proc)]
        {:exit exit-code :out @out :err @err}))))
