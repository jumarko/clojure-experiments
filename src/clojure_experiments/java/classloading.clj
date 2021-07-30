(ns clojure-experiments.java.classloading)

;;; Once upon a class: https://danielsz.github.io/blog/2021-05-12T13_24.html
(->> (.. java.lang.Thread currentThread getContextClassLoader)
     (iterate #(.getParent %))
     (take-while identity))


;; - When you create foo at the REPL, Clojure’s compiler emits bytecode for consumption by DynamicClassLoader. It will create a new class with the defineClass method before linking it.
;; - Once a class loader links a class, it is final. Attempting to link a new definition of the class does nothing.
;; - To work around this limitation, a new DynamicClassLoader is created for each evaluation.


;; core classes are loaded by 'Primordial' classloader manifested as nil
(every? #(nil? (.getClassLoader (class %))) [4 "hello" (java.lang.Object.) (java.util.Collections/EMPTY_LIST)])
;; => true


;; java.util.* classes loaded by 'Platform' class loader?
(.getName (.getClassLoader (class (java.sql.Time. 1 1 1))))
;; => "platform"
(.getClassLoader (class (java.util.Date. 1 1 1)))
;; => nil

;; clojure.core classes should be loaded by 'Application' class loader
(str (.getName (class {})) " was loaded by " (.getName (.getClassLoader (class {}))))
;; => "clojure.lang.PersistentArrayMap was loaded by app"


(defn foo [x] (+ x x))
(.getClassLoader (class foo))
;;=> #object[clojure.lang.DynamicClassLoader 0x227119dc "clojure.lang.DynamicClassLoader@227119dc"]

;; when you redefine it a new classloader is created
(defn foo [x] (+ x x))
;;=> #object[clojure.lang.DynamicClassLoader 0x4760597d "clojure.lang.DynamicClassLoader@4760597d"]

;; let's inspect class hierarchy
(->> (class foo)
     (iterate #(.getSuperclass %))
     (take-while identity))
;; => (clojure_experiments.java.classloading$foo
;;     clojure.lang.AFunction
;;     clojure.lang.AFn
;;     java.lang.Object)

;; The compiler tracks the current instance of DynamicClassLoader in clojure.lang.Compiler/LOADER,
;; while DynamicClassLoader tracks its classes via a cache.
;; The latter is backed by a reference queue, helping the garbage collector do its job.
;; We can peek into it via the Reflection API.
(defn inspect-cache
  "This will reveal a mapping between the names of the generated classes and soft references.
  If you redefine foo at the REPL, the soft reference associated with foo in the cache will be updated."
  []
  (let [cache (.getDeclaredField clojure.lang.DynamicClassLoader "classCache")]
    (.setAccessible cache true)
    (.get cache nil)))
;; Can be huge!
#_(inspect-cache)


;; check parent - in nREPL these are not the same
;; - in default clojure repl they should be the same
(= (.getContextClassLoader (Thread/currentThread)) (.getParent (.getClassLoader (class foo))))
;; => false


;;; Adding dependencies dynamically
(def json-jar "https://repo1.maven.org/maven2/org/clojure/data.json/2.3.0/data.json-2.3.0.jar")
;; tools.deps.alpha: https://github.com/clojure/tools.deps.alpha/blob/add-lib3/src/main/clojure/clojure/tools/deps/alpha/repl.clj#L75
;;  -> includes transitive dependencies!
(defn add-lib
  "Attempts to add given jar to the classpath.
  Search https://mvnrepository.com to find a URL for your jar.
  Note that you have to add any required transitive dependencies manually.

  Example: (add-lib \"https://repo1.maven.org/maven2/org/clojure/data.json/2.3.0/data.json-2.3.0.jar\")"
  [jar]
  (-> (Thread/currentThread)
      .getContextClassLoader
      ;; this is possible because `DynamicClassLoader` extends `URLClassLoader`
      (.addURL (java.net.URL. jar))))
(comment

  ;; try amazonica (shouldn't be on the classpath)
  ;;   https://mvnrepository.com/artifact/amazonica/amazonica/0.3.156
  (def amazonica-jar "https://clojars.org/repo/amazonica/amazonica/0.3.156/amazonica-0.3.156.jar")

  (add-lib amazonica-jar)
  ;; try require it...
  (require '[amazonica.aws.s3 :as s3])  
  ;; ...notice that while it's loaded you cannot really use it =>  you would need to add transitive deps too
  ;; => Syntax error (FileNotFoundException) compiling at (amazonica/core.clj:1:1).
  ;;    Could not locate clojure/algo/generic/functor__init.class, clojure/algo/generic/functor.clj or clojure/algo/generic/functor.cljc on classpath.


  ,)


(require '[cheshire.core :as json]
         )
(def github-log (mapv json/parse-string (line-seq (clojure.java.io/reader "/Users/jumar/export-empear-analytics-1623296433.json"))))

(first github-log)
;; => {"org" "empear-analytics", "user" "", "repository" "empear-analytics/codescene-cloud-ui-tests", "protocol_name" "http", "business" "", "@timestamp" 1623210098271, "action" "git.fetch", "repo" "empear-analytics/codescene-cloud-ui-tests", "repository_public" false, "actor" "emiride"}


(def ui-push-actions
  (->> github-log
       (filter (fn [{:strs [action repository]}]
                 (and (= repository "empear-analytics/codescene-ui")
                      (= action "git.push"))))))
