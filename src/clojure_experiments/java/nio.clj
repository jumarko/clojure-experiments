(ns clojure-experiments.java.nio
  "Experiments with new Java IO - java.nio.
  In particular java.nio.file.* - Path, Files, etc.

  See also `clojure-experiments.files`."
  (:require [babashka.fs :as fs])
  (:import
   (java.io File)
   (java.nio.file Files FileStore FileSystem FileSystems Path Paths)
   ))


;;; Useful resources:
;;; - https://blogs.oracle.com/javamagazine/post/path-files-input-output
;;; - https://www.baeldung.com/java-path-vs-file


;; deleting files - the old API doesn't give you any useful error message
(comment
  (.delete (File. "/tmp/cannot-delete"))
  ;; => false

  (Files/delete (Paths/get "/tmp/cannot-delete" (make-array String 0)))
  ;; Execution error (AccessDeniedException) at sun.nio.fs.UnixException/translateToIOException (UnixException.java:90).
  ;;   /tmp/cannot-delete

,)



(seq (.getFileStores (FileSystems/getDefault)))
;; => (#object[sun.nio.fs.BsdFileStore 0x3f269677 "/ (/dev/disk1s5s1)"]
;;     #object[sun.nio.fs.BsdFileStore 0x52eb3888 "/dev (devfs)"]
;;     #object[sun.nio.fs.BsdFileStore 0x7e6c2692 "/System/Volumes/VM (/dev/disk1s4)"]
;;     #object[sun.nio.fs.BsdFileStore 0x17cdc55a "/System/Volumes/Preboot (/dev/disk1s2)"]
;;     #object[sun.nio.fs.BsdFileStore 0x35f47abe "/System/Volumes/Update (/dev/disk1s6)"]
;;     #object[sun.nio.fs.BsdFileStore 0x3e8a9ee1 "/System/Volumes/Data (/dev/disk1s1)"]
;;     #object[sun.nio.fs.BsdFileStore 0x214d6714 "/System/Volumes/Data/home (map auto_home)"]
;;     #object[sun.nio.fs.BsdFileStore 0x58d39172 "/Volumes/Keybase (jumar) (kbfs@kbfuse0)"]
;;     #object[sun.nio.fs.BsdFileStore 0x4ee4315c "/Volumes/Keybase (keybase-redirector)"]
;;     #object[sun.nio.fs.BsdFileStore 0x2473d3f7 "/Volumes/Poly Lens 1.1.6 (/dev/disk2s1)"])


;;  Listing directories
(comment
(with-open [ds (Files/newDirectoryStream
                (Path/of "/Users/jumar/workspace/clojure/clojure-experiments/" (make-array String 0))
                "*.clj")]
  (run! println ds))
;; prints:
;; #object[sun.nio.fs.UnixPath 0x71248419 /Users/jumar/workspace/clojure/clojure-experiments/read-line.clj]
;; #object[sun.nio.fs.UnixPath 0x6f46f4b0 /Users/jumar/workspace/clojure/clojure-experiments/lein-profiles.clj]
,)


;; FileSystem and FileStore
(defn gbs [bytes]
  (double (/ bytes 1024 1024 1024)))

(defn fs-info [fs]
  {:name (.name fs)
   :space [(gbs (.getTotalSpace fs))
           (gbs (.getUsableSpace fs))
           (gbs (.getUnallocatedSpace fs))]})
(comment
  (->> (FileSystems/getDefault)
       .getFileStores
       (mapv fs-info))
;; => [{:name "/dev/disk1s5s1", :space [931.546989440918 92.3420295715332 917.2651901245117]}
;;     {:name "devfs", :space [2.374649047851562E-4 0.0 0.0]}
;;     {:name "/dev/disk1s4", :space [931.546989440918 92.3420295715332 925.5463256835938]}
;;     {:name "/dev/disk1s2", :space [931.546989440918 92.3420295715332 931.0945472717285]}
;;     {:name "/dev/disk1s6", :space [931.546989440918 92.3420295715332 931.5440559387207]}
;;     {:name "/dev/disk1s1", :space [931.546989440918 92.3420295715332 113.8534889221191]}
;;     {:name "map auto_home", :space [0.0 0.0 0.0]}
;;     {:name "kbfs@kbfuse0", :space [250.0 249.9998822212219 249.9998822212219]}
;;     {:name "keybase-redirector", :space [0.0 0.0 0.0]}
;;     {:name "/dev/disk2s1", :space [0.3238029479980469 0.03251266479492188 0.03251266479492188]}]
  )


;;; babashka/fs: https://github.com/babashka/fs


