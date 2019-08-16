(ns clojure-experiments.leiningen.deps
  "
  See https://github.com/technomancy/leiningen/blob/master/src/leiningen/deps.clj.
  Related cider issue: https://github.com/clojure-emacs/cider/issues/1666

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

#_(def sample-deps (read-deps-from-file "/Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/leiningen/deps.edn"))
#_(def enterprise-deps (read-deps-from-file "/Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/leiningen/enterprise-deps.edn"))
#_(def lein-nvd-deps (read-deps-from-file "/Users/jumar/workspace/clojure/lein-nvd/deps.edn"))

(defn deps-with-paths-flat-list
  "Walks the dependency tree data structure (expects format provided by `lein deps :tree-data`)
  in the depth-first walk manner and builds a flat sequence of dependencies
  _including the whole path to that dependency_.
  This is important for *transitive dependencies*.
  That is if you only have a single top-level depedency A which in turn depends on AA and AB,
  where AB depends on ABA, then you'll get following as a result:
  ```
  [
   [[A]]
   [[A] [AA]]
   [[A] [AB]]
   [[A] [AB] [ABA]]
  ]
  ```

  where each element is a vector of depedencies a full path from a top-level dependency
  (the first element of the vector) to the \"final\" dependency (the last element of the vector)."
  [deps-tree tree-root-path]
  (reduce-kv
   (fn [deps-list dependency transitive-deps]
     (apply conj
            deps-list
            (conj tree-root-path dependency)
            (deps-with-paths-flat-list transitive-deps (conj tree-root-path dependency))))
   []
   deps-tree))

(defn deps->paths
  "Returns a map from a depedency (top-level or a transitive one)
  to a full path to that dependency.
  The path is empty for a top-level dependencies or non-empty for transitive dependencies.
  The path for a transitive dependency starts from the top-level dependency going towards
  the transitive dep.

  This is a convenient wrapper for `deps-with-paths-flast-list` with alternative representation
  which can provide better performance/ergonomics where you need to lookup path for a particular dependency.

  Note: if you don't want the exact shape of the dependency vector (including the version number
  and optional :exlusions!)
  than it's perhaps better to use the `deps-flat-list` instead."
  [deps-tree]
  (let [deps-with-paths (deps-with-paths-flat-list deps-tree [])]
    (reduce
     (fn [acc dep-with-path]
       (assoc acc
              (peek dep-with-path)
              (pop dep-with-path)))
     {}
     deps-with-paths)))

(defn deps-flat-list
  "Walks the dependency tree data structure (expects format provided by `lein deps :tree-data`)
  in the depth-first walk manner and builds a flat sequence of dependencies
  where each element is a vector representing a single atomic depedency
  consisting of dependency name the first element,
  dependency version as the second element, optional exclusions, etc - the same format as in leiningen."
  [deps-tree]
  (let [deps-with-paths (deps-with-paths-flat-list deps-tree [])]
    (mapv peek deps-with-paths)))

(comment
  (deps-flat-list sample-deps)
  ;; => [[nrepl "0.6.0" :exclusions [[org.clojure/clojure]]]
  ;;     [clojure-complete "0.2.5" :exclusions [[org.clojure/clojure]]]
  ;;     [expound "0.7.2"]
  ;;     [criterium "0.4.4"]
  ;;     [org.clojure/tools.namespace "0.3.1"]
  ;;     [org.clojure/java.classpath "0.3.0"]
  ;;     [org.clojure/tools.reader "1.3.2"]
  ;; ...

  (deps-with-paths-flat-list sample-deps [])
  ;;[[[nrepl "0.6.0" :exclusions [[org.clojure/clojure]]]]
  ;; [[clojure-complete "0.2.5" :exclusions [[org.clojure/clojure]]]]
  ;; [[expound "0.7.2"]]
  ;; [[criterium "0.4.4"]]
  ;; [[org.clojure/tools.namespace "0.3.1"]]
  ;; [[org.clojure/tools.namespace "0.3.1"] [org.clojure/java.classpath "0.3.0"]]
  ;; [[org.clojure/tools.namespace "0.3.1"] [org.clojure/tools.reader "1.3.2"]]
  ;; ...


  (deps->paths sample-deps)
  ;; {[nrepl "0.6.0" :exclusions [[org.clojure/clojure]]] [],
  ;;  [clojure-complete "0.2.5" :exclusions [[org.clojure/clojure]]] [],
  ;;  [org.clojure/tools.reader "1.3.2"] [[org.clojure/tools.namespace "0.3.1"]],
  ;;  [expound "0.7.2"] [],
  ;;  [criterium "0.4.4"] [],
  ;;  [org.clojure/google-closure-library "0.0-20170809-b9c14c6b"] [[datawalk "0.1.12"] [org.clojure/clojurescript "1.9.908"]],
  ;; ...
   


  )

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
  (pp/pprint (into ["resources"] ; "resources" are the default and should be always present
                   (build-resource-paths (source-jar-files deps-tree)))))

(comment

  (pprint-resource-paths! enterprise-deps)

  (pprint-resource-paths! sample-deps)
  ["resources"
   "/Users/jumar/.m2/repository/org/clojure/tools.namespace/0.3.1/tools.namespace-0.3.1-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/java.classpath/0.3.0/java.classpath-0.3.0-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/tools.reader/1.3.2/tools.reader-1.3.2-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/data.json/0.2.6/data.json-0.2.6-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/clojure/1.10.1/clojure-1.10.1-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/core.specs.alpha/0.2.44/core.specs.alpha-0.2.44-sources.jar"
   "/Users/jumar/.m2/repository/org/clojure/spec.alpha/0.2.176/spec.alpha-0.2.176-sources.jar"]
  

  )


