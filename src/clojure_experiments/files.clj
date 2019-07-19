(ns clojure-experiments.files
  "Various File IO examples.
  See
  - https://docs.oracle.com/javase/8/docs/api/java/util/jar/JarEntry.html
  - https://stackoverflow.com/questions/23139331/how-to-get-list-of-classpath-resources-in-nested-jar"
  (:require [clojure.java.io :as io]))


;;; List directory files, especially those packaged inside an uberjar
(defmulti list-files (fn [dir] (some-> dir (io/resource) .getProtocol)))

(defmethod list-files "file" [dir]
  (-> dir io/resource io/file file-seq))

(defn list-files-in-jar-path
  [filter-fn dir-path-in-jar]
  (let [jar-path (subs dir-path-in-jar 5 (.indexOf dir-path-in-jar "!"))
        jar-file (java.util.jar.JarFile. jar-path)]
    ;; finally some Clojure!
    (->> jar-file
         .entries
         enumeration-seq
         (filter filter-fn)
         ;; we call JarEntry#getName -> this is inconsistent with `list-files "file"` implementation
         ;; which returns java.io.File instances
         (map #(.getName %)))))

(defmethod list-files "jar" [dir]
  (list-files-in-jar-path
   (fn [entry] (and (clojure.string/starts-with? (.getName entry)
                                                 "my-test-dir")))
   (-> dir (io/resource) .getPath)))

(comment 
  (list-files "my-test-dir" )

  (list-files-in-jar-path
   (fn [entry] (and (clojure.string/starts-with? (.getName entry)
                                                 "my-test-dir")))
   "file:/Users/jumar/workspace/clojure/leiningen/test-app/target/uberjar/test-app-0.1.0-SNAPSHOT-standalone.jar!/my-dir")

  ;; end
  )


