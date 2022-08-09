(ns clojure-experiments.java
  "Anything related to java interop.

  Really cool talk is 'Java Made (Somewhat) Simple' by Ghadi Shayban: https://www.youtube.com/watch?v=-zszF8bbXM0"
  (:import (java.util.zip ZipFile)))

;;; instace of two classes with the same name
;;; aren't generally equal because they came from different classloaders!
(defrecord MyInfo [a b c])
(def a (->MyInfo 1 2 3))
(def b (->MyInfo 1 2 3))
(= a b)
;; => true

(defrecord MyInfo [a b c])
(def c (->MyInfo 1 2 3))
(= a c)
;; => false


;; finalize called twice?
(defn test1 []
  (reify Object
    (toString [this] (println "ahoj") (str this))
    (finalize [this]
      (prn this)
      (println "collected"))))
#_(do (test1) (System/gc))


;; https://gist.github.com/semperos/3835392
(defn scaffold [iface]
  (doseq [[iface methods] (->> iface .getMethods 
                               (map #(vector (.getName (.getDeclaringClass %)) 
                                             (symbol (.getName %))
                                             (count (.getParameterTypes %))))
                               (group-by first))]
    (println (str "  " iface))
    (doseq [[_ name argcount] methods]
      (println 
       (str "    " 
            (list name (into ['this] (take argcount (repeatedly gensym)))))))))
(scaffold clojure.lang.IPersistentMap)
;; prints something like this:
;;  clojure.lang.IPersistentMap
;;    (assoc [this G__441 G__442])
;;    (without [this G__443])
;;    (assocEx [this G__444 G__445])
;;  java.lang.Iterable
;;    (iterator [this])
;;  clojure.lang.Associative
;;    (containsKey [this G__446])
;;    (assoc [this G__447 G__448])
;;    (entryAt [this G__449])
;;  clojure.lang.IPersistentCollection
;;    (count [this])
;;    (cons [this G__450])
;;    (empty [this])
;;    (equiv [this G__451])
;;  clojure.lang.Seqable
;;    (seq [this])
;;  clojure.lang.ILookup
;;    (valAt [this G__452 G__453])
;;    (valAt [this G__454])
;;  clojure.lang.Counted
;;    (count [this])


;;; "override" one method of an object with `proxy`
;;; java.util.zip.ZipFile is kinda random choise
;;; but it implements Closeable and is a non-final class
;;; with non-final close method implementation
(let [filename "empty.zip"
      original-zip (ZipFile. filename)
      proxy-zip (proxy [ZipFile] [filename]
                  ;; overriding close method
                  (close [] (println "Ignored!"))
                  ;; random method implementation passing the call to the wrapper
                  ;; Note: you don't have to do this at all! (see .getName call below)
                  (size [] (.size original-zip)))]
  [[(.size proxy-zip) (.size original-zip)]
   ;; check the REPL - you will see "Ignored!" printed one time
   ;; - note that we cannot return "Ignored" as a value
   ;; because close method is 'void'
   [(.close proxy-zip) (.close original-zip)]

   ;; now try a method which we didn't specify at all
   [(.getName proxy-zip) (.getName original-zip)]

   ;; finally check the instances and their classes
   [(instance? ZipFile proxy-zip) (instance? ZipFile original-zip)]
   [(class proxy-zip) (class original-zip)]])
;; => [[1 1]
;;     [nil nil]
;;     ["empty.zip" "empty.zip"]
;;     [true true]
;;     [clojure_experiments.java.proxy$java.util.zip.ZipFile$ff19274a java.util.zip.ZipFile]]

;; the minimal implementation would be
(let [filename "empty.zip"
      original-zip (ZipFile. filename)
      proxy-zip (proxy [ZipFile] [filename]
                  ;; overriding close method
                  (close [] (println "Ignored!")))]
  [[(.getName proxy-zip) (.getName original-zip)]
   ;; check the REPL - you will see "Ignored!" printed one time
   [(.close proxy-zip) (.close original-zip)]])


;; ... or use lesser-known `get-proxy-class`, `construct-proxy`, and `init-proxy`
;; (later you could also use `update-proxy`)
;; https://clojuredocs.org/clojure.core/construct-proxy
(def MyZipFile (get-proxy-class ZipFile))
(let [filename "empty.zip"
      original-zip (ZipFile. filename)
      proxy-zip (-> (construct-proxy MyZipFile filename)
                    (init-proxy {"close" (fn [_this] (doto "Ignored!" println))}))]
  proxy-zip
  [[(.size proxy-zip) (.size original-zip)]
   ;; check the REPL - you will see "Ignored!" printed one time
   ;; - note that we cannot return "Ignored" as a value
   ;; because close method is 'void'
   [(.close proxy-zip) (.close original-zip)]
   [(.getName proxy-zip) (.getName original-zip)]])
;; => [[1 1] [nil nil] ["empty.zip" "empty.zip"]]

