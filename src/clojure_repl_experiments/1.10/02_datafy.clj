(ns clojure-repl-experiments.1.10.02_datafy
  (:require [clojure.core.protocols :as p]
            [clojure.datafy :as d]
            [clojure.java.data :as java-data]
            [clojure.java.io :as io])
  (:import java.io.File))

;;;; datafy
;;;; https://clojureverse.org/t/what-use-cases-do-you-see-for-clojure-datafy/3143
;;;; https://www.birkey.co/2018-10-26-datafy-and-tap%3E-in-clojure-1.10.html
;;;; https://github.com/clojure/clojure/commit/93841c0dbf9db2f358474408d5e21530f49ef8b3
;;;; ===========================================================================

;; let us write an fn to give use any member that we would like to find more about:
(defn member-lookup [class member]
  (->> class
       d/datafy
       :members
       (filter (fn [[k v]] (= (symbol member) k)))))

(member-lookup String "intern")
;; => ([intern
;;      [#clojure.reflect.Method{:name intern,
;;                               :return-type java.lang.String,
;;                               :declaring-class java.lang.String,
;;                               :parameter-types [],
;;                               :exception-types [],
;;                               :flags #{:public :native}}]])



;;; try with files
;;; For now, we have to extend File

(defn datafy-node
  [^File f]
  {:name (.getName f)
   :path (.getPath f)
   :modified (.lastModified f)})
  
(def mega (* 1024 1024))

(defn bounded-slurp "like slurp, but only up to approximately limit chars."
  [f chars-limit]
  (let [sw (java.io.StringWriter.)]
    (with-open [rdr (io/reader f)]
      (let [^"[C" buffer (make-array Character/TYPE 8192)]
        (loop [n 0]
          (when (< n chars-limit)
            (let [size (.read rdr buffer)]
              (when (pos? size)
                (do
                  (.write sw buffer 0 size)
                  (recur (+ n size)))))))))
    (.toString sw)))

(defn datafy-file
  ([^File f] (datafy-file f mega))
  ([^File f chars-limit]
   (with-meta
     (assoc (datafy-node f)
            :length (.length f)
            :contents "<elided>")
     {`p/nav (fn [f k v]
               (if (= :contents k)
                 (bounded-slurp (:path f) chars-limit)
                 v))})))

(defn datafy-directory
  [^File f]
  ;; notice that original definition in the talk duplicated `:name` and `:path`,
  ;; probably for no reason
  (assoc (datafy-node f)
         ;; note that we don't want to recursively call `datafy` on children
         ;; because this would be eager traversal of the whole directory tree
         ;; :files (map d/datafy (into [] (.listFiles f)))
         :files (into [] (.listFiles f))))


(extend-protocol p/Datafiable
  java.io.File
  (datafy [f]
    (cond
      (.isFile f) (datafy-file f)
      (.isDirectory f) (datafy-directory f)
      :default f)))
(io/file "/Users/jumar/tools/clojure/")
;; VS:
(d/datafy (io/file "/Users/jumar/tools/clojure/"))

;; let's try `nav` now:
;; quite a lot of work just to navigate to file :)
#_(let [my-file
      (->> "/Users/jumar/tools/clojure/"
           io/file
           d/datafy
           :files
           (filter #(= "rebl" (:name (d/datafy %))))
           first

           d/datafy
           :files
           (filter #(= "deps.edn" (:name (d/datafy %))))
           first

           d/datafy
           )]
  (d/nav my-file :contents (:contents my-file)))
