(ns clojure-experiments.java
  "Anything related to java interop.

  Really cool talk is 'Java Made (Somewhat) Simple' by Ghadi Shayban: https://www.youtube.com/watch?v=-zszF8bbXM0")

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

