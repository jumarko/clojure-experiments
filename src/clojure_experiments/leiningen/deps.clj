(ns clojure-experiments.leiningen.deps
  "
  See https://github.com/technomancy/leiningen/blob/master/src/leiningen/deps.clj.

  Try to walk dependencies and see if you can attach source jars to the :resource-paths."
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp ]
            [clojure.string :as string]))

;;; Run `lein deps :tree-data > source-deps.edn` first to capture project's dependencies in EDN format
;;; Then read that file.

(defn read-deps-from-file [edn-file]
  (-> edn-file
      slurp
      (clojure.edn/read-string)))

(def sample-deps (read-deps-from-file "src/clojure_experiments/leiningen/deps.edn"))
(def enterprise-deps (read-deps-from-file "src/clojure_experiments/leiningen/enterprise-deps.edn"))


(defn deps-flat-list
  "Walks the dependency tree data structure (expects format provided by `lein deps :tree-data`)
  in the depth-first walk manner and builds a flat sequence of dependencies
  where each element is a vector representing a single atomic depedency
  consisting of dependency name the first element,
  dependency version as the second element, optional exclusions, etc - the same format as in leiningen."
  [deps-tree]
  (reduce-kv
   (fn [deps-list dependency transitive-deps]
     (apply conj deps-list dependency (deps-flat-list transitive-deps)))
   []
   deps-tree))

(defn- dep-name->path [dep-name]
  (let [[group artifact & rst] (string/split dep-name #"/")]
    (cond
      rst (throw (ex-info "Wrong dependency name format. Expecting at most one '/' in the name." {:name dep-name}))
      ;; e.g. "org.clojure/clojure"
      artifact (str (string/replace group "." "/") "/" artifact)
      ;; e.g. "ring" has to be translated to "ring/ring" (group id and artifact id is the same in this case)
      group (str group "/" group)
      :else (throw (ex-info "Wrong dependency name format - must have at least one part" {:name dep-name})))))

(comment
  
  (dep-name->path  "org.clojure/clojure")
  ;; => "org/clojure/clojure"
  (dep-name->path  "ring")
  ;; => "ring/ring"
  (dep-name->path  "com.fingerhutpress.cljol_jvm_support/cljol_jvm_support")
  ;; => "com/fingerhutpress/cljol_jvm_support/cljol_jvm_support"
  )

;; safer for AOT compilation to make this defn instead of def
(defn m2-repo-path-prefix []
  (str (System/getProperty "user.home") "/.m2/repository/"))

(defn- dep->m2repo-path
  "Transforms depedency vector such as `[expound \"0.7.2\"]
  into an absolute path inside .m2 local repositorisy where Maven would save such an artifact;
  in this case it would be \"expound/expound/0.7.2/\""
  [[dep-name-sym dep-version]]
  (str
   (m2-repo-path-prefix)
   (dep-name->path (str dep-name-sym))
   "/"
   dep-version))

(comment
  (dep->m2repo-path '[org.clojure/tools.reader "1.3.2" ])
  ;; => "/Users/jumar/.m2/repository/org/clojure/tools.reader/1.3.2"
  (dep->m2repo-path '[nrepl "0.6.0" :exclusions [ [ org.clojure/clojure ] ] ])
  ;; => "/Users/jumar/.m2/repository/nrepl/nrepl/0.6.0"
  )

(defn- find-source-jar [m2-repo-dir]
  (let [dir-files (-> (io/file m2-repo-dir) .listFiles)]
    (->> dir-files
         (filter #(string/ends-with? (.getName %) "sources.jar"))
         first)))

(comment
  (find-source-jar "/Users/jumar/.m2/repository/org/clojure/tools.reader/1.3.2")
  ;; => #object[java.io.File 0x35d2feb5 "/Users/jumar/.m2/repository/org/clojure/tools.reader/1.3.2/tools.reader-1.3.2-sources.jar"]
  )

(defn source-jar-files
  [deps-tree]
  (->> deps-tree
       deps-flat-list
       (mapv dep->m2repo-path)
       (mapv find-source-jar)
       (filterv some?)))

(defn- build-resource-paths [source-files]
  (mapv str source-files))
    

(defn pprint-resource-paths!
  "Use this and add it as a value of the `:resource-paths` key in project.clj"
  [deps-tree]
  (pp/pprint (build-resource-paths (source-jar-files deps-tree))))

(comment

  (pprint-resource-paths! enterprise-deps)

  (pprint-resource-paths! sample-deps)
  ["/Users/jumar/.m2/repository/org/clojure/tools.namespace/0.3.1/tools.namespace-0.3.1-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/java.classpath/0.3.0/java.classpath-0.3.0-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/tools.reader/1.3.2/tools.reader-1.3.2-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/data.json/0.2.6/data.json-0.2.6-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/clojure/1.10.1/clojure-1.10.1-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/core.specs.alpha/0.2.44/core.specs.alpha-0.2.44-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/spec.alpha/0.2.176/spec.alpha-0.2.176-sources.jar"]
  

  )


