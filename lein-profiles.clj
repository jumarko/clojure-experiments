{
 :user {

        ;; to see reflection warnings
        ;; check http://clojure-goes-fast.com/blog/performance-nemesis-reflection/
        ;; and https://github.com/technomancy/leiningen/blob/master/sample.project.clj
        ;; there's also `lein check` command: https://groups.google.com/forum/#!topic/clojure/gFiyzeVowWY
        ;; Shows some cider-related things; does it make cider slower??
        :global-vars {*warn-on-reflection* true}

        :plugins      [;; since cider 0.11 no specific configuration is required
                       ;; doesn't work -> throwing: java.lang.IllegalArgumentException: No matching ctor found for class org.sonatype.aether.repository.Authentication

                       ;; ultra plugin is nice but it brings loads of shits onto the classpath
                       #_[venantius/ultra "0.5.1"]

                       ;; for downloading all transitive dependencies' sources into target/ubersource directory
                       ;; ubersource plugin doesn't work with newer maven/java versions
                       ;; (Caused by: java.lang.ClassNotFoundException: org.sonatype.aether.resolution.DependencyResolutionException)
                       ;; [lein-ubersource "0.1.1"]

                       ;;; Linters, code formatters
                       ;;; ------------------------

                       ;; `lein eastwood` https://github.com/jonase/eastwood - one of the most active
                       [jonase/eastwood "0.3.8"]
                       ;; `lein kibit`
                       [lein-kibit "0.1.8"]
                       ;; `lein nvd check` https://github.com/rm-hull/lein-nvd
                       [lein-nvd "1.3.1"]

                       ;; I DON'T USE THIS (it's noisy)
                       ;; `lein yagni` https://github.com/venantius/yagni - helps to find unused code in your app and libs
                       ;; [venantius/yagni "0.1.7"]

                       ;; I DON'T USE THIS
                       ;; `lein cljfmt check` https://github.com/weavejester/cljfmt
                       ;; [lein-cljfmt "0.6.4"]

                       ;; `lein cloverage` https://github.com/cloverage/cloverage
                       [lein-cloverage "1.1.1"]
                       ;; `lein ancient` https://github.com/xsc/lein-ancient
                       [lein-ancient "0.6.15"]


                       ;; I DON'T USE THIS
                       ;; `lein bikeshed -m 120` https://github.com/dakrone/lein-bikeshed - checks docstrings, trailing spaces/lines, etc.
                       ;; [lein-bikeshed "0.5.2"]
                       ;; OTHERS
                       ;; Joker! it's installed system-wide via homebrew => run it manually
                       ;;   `find src -name '*.clj' -exec joker --lint {} \; &>joker.out`
                       ;; slamhound looks unmaintained: https://github.com/technomancy/slamhound
                       ;; spectrum isn't ready yet: https://github.com/arohner/spectrum
                       ;; sonar-clojure combines various tools: https://github.com/fsantiag/sonar-clojure

                       ;; use nodisassemble only if needed
                       #_[lein-nodisassemble "0.1.3"]
                       ;; test html reports https://github.com/ruedigergad/test2junit
                       #_[test2junit "1.2.1"]

                       ;; ALTHOUGH I DON'T USE THIS IT SEEMS PRETTY USEFUL
                       ;; pretty print for inspecting leiningen profiles -> lein pprint
                       [lein-pprint "1.3.2"]

                       ;; I DON'T USE THIS
                       ;; visualization of leiningen dependencies
                       ;; [walmartlabs/vizdeps "0.1.4"]

                       ;; Sayid for debugging: http://bpiel.github.io/sayid/
                       ;; [com.billpiel/sayid "0.0.15"]

                       ;; I DON'T USE THIS
                       ;; documentation generation: https://github.com/weavejester/codox
                       ;; [lein-codox "0.10.5"]

                       ;; virgil for reloading java classes: https://github.com/ztellman/virgil
                       ;; my own version should be replaced by alpha!
                       ;; update: I don't really use this
                       ;; [lein-virgil "0.1.9-SNAPSHOT"]

                       ;; lein-auto is also useful for ANTLR grammars
                       ;; run it like this: `lein auto antlr`
                       ;; I'm using my own patched version until James releases a new version
                       ;; [lein-auto "0.1.4-SNAPSHOT"]

                       ;; I DON'T USE THESE:
                       ;; analyzing function calls hierachy/topology: https://github.com/testedminds/lein-topology
                       ;; doesn't work: `Caused by: java.lang.RuntimeException: Invalid token: ::specs/username`
                       #_[lein-topology "0.3.0-SNAPSHOT"]
                       ;; great tool for visualization of ns dependencies: https://github.com/alexander-yakushev/ns-graph
                       ;; [ns-graph "0.1.2"]
                       ;; similar to ns-graph but under active development: https://github.com/simon-katz/lein-nomis-ns-graph
                       ;; [lein-nomis-ns-graph "0.14.1"]
                       ;; explicit dependency on tools.namespace because `lein-nomis-ns-graph` requires the latest alpha
                       ;; run `lein deps :plugin-tree`
                       ;; [org.clojure/tools.namespace "0.3.1"]

                       ;; I DON'T USE THIS
                       ;; another ns dependency analyzer plugin: https://github.com/hilverd/lein-ns-dep-graph
                       ;; [lein-ns-dep-graph "0.2.0-SNAPSHOT"]
                       ;; lein-gossip for visualizing call graph: https://github.com/actsasgeek/lein-gossip
                       ;; cloned into ~/workspace/clojure/lein-gossip and installed manually

                       ;; to convert pom.xml to project.clj
                       ;; I DON'T USE THIS
                       ;; [lein-maven "0.1.0"]

                       ;; GraalVM native images: https://github.com/taylorwood/lein-native-image
                       ;; I DON'T USE THIS
                       ;; [io.taylorwood/lein-native-image "0.3.0"]

                       ]

        ;; config for `lein-native-image`
        :native-image {:graal-bin "/Users/jumar/tools/java/GraalVM/graalvm-ce-1.0.0-rc5/Contents/Home"}

        :dependencies [;; trace-forms doesn't work as expected - don't use tools.trace
                       #_[org.clojure/tools.trace "0.7.10"]
                       [pjstadig/humane-test-output "0.9.0"]
                       ;; tupelo library with lots of useful functionsl ike `spy`, `lazy-cons`, `with-exception-default`
                       ;; watch out - it has quite a lot of dependencies!
                       ;; [tupelo "0.9.71"]
                       ;; visualization of Clojure data structures
                       #_[walmartlabs/datascope "0.1.1"]
                       #_[slamhound "1.5.5"]

                       ;; adding ad-hoc dependencies: https://github.com/pallet/alembic
                       ;; problems with alembic: clojure.lang.ExceptionInfo: Alembic can not manipulate specified ClassLoader. {
                       ;; note here's a different version of alembic updated for clojure-emacs (new dynapath version - should work with Java 9)
                       ;; Right now, It doesn't work for me
                       ;; [clojure-emacs/alembic "0.3.3"]

                       ;; for benchmarks: https://github.com/hugoduncan/criterium
                       [criterium "0.4.5"]
                       ;; use `tufte` for profiling: see https://github.com/ptaoussanis/tufte#how-does-tufte-compare-to-hugoduncancriterium
                       ;; I DON'T USE THIS
                       ;; [com.taoensso/tufte "2.0.1"]
                       ;; TODO uncomment if required in other projects
                       #_[com.cemerick/piggieback "0.2.1"]

                       ;; for another way to add dependencies easily
                       ;; see also: https://github.com/vise890/pocketbook/blob/master/src/pocketbook/core.clj
                       ;;   for how to add both sources and javadocs
                       ;; UPDATE 06/2018: this is now the reliable way how to add dependencies
                       ;; alembic doesn't work with java 9+
                       ;; I DON'T USE THIS!
                       ;; DON'T BOTHER - it doesn't work properly; it seems loading the dependencies but you cannot require them afterwards!!!
                       ;; [com.cemerick/pomegranate "1.0.0"]
                       ;; [org.tcrawley/dynapath "1.0.0"]

                       ;; pocketbook plugin for automatically adding "source" jars to classpath
                       ;; EXPERIMENTAL!
                       ;; see https://github.com/clojure-emacs/cider-nrepl/issues/64#issuecomment-518412610
                       ;; https://github.com/vise890/lein-pocketbook
                       ;; PROBLEM: java.lang.ClassCastException: class clojure.lang.PersistentVector cannot be cast to class java.lang.String (
                       ;; [lein-pocketbook "0.1.4-SNAPSHOT"]

                       ;; I'M NOT USING THIS
                       ;; for visualizing clojure specs: https://github.com/jebberjeb/specviz
                       ;; mentioned by Stuart Halloway in REPL-driven development talk: 
                       ;;   https://github.com/stuarthalloway/presentations/wiki/REPL-Driven-Development
                       ;; WARNING: this works only for clojure 1.9.x !!!
                       ;; [specviz "0.2.3"]

                       ;; seesaw is a nice library for dealing with Java Swing
                       ;; recommended by Stuart Halloway in his talk: Chicago Clojure - 2017-06-21 - Stuart Halloway on Repl Driven Development
                       ;; I don't really use this
                       ;; [seesaw "1.4.5"]

                       ;; datawalk for interactive exploration of data structures
                       ;; I don't really use this
                       ;; eggsyntax, personally I use datawalk for that : https://github.com/eggsyntax/datawalk
                       [datawalk "0.1.12"]

                       ;; great tool for visualization of ns dependencies: https://github.com/alexander-yakushev/ns-graph
                       ;; use from REPL via `(ns-graph.core/depgraph* {:source-paths "src/"})`
                       ;; I don't really use this
                       ;; [ns-graph "0.1.2"]

                       ;; similar to ns-graph but under active development: https://github.com/simon-katz/lein-nomis-ns-graph
                       ;; I don't really use this
                       ;; [lein-nomis-ns-graph "0.14.1"]

                       ;; debux: for quick & easy debugging: https://github.com/philoskim/debux
                       ;; exclude clojure-future-spec because otherwise debux will bring
                       ;; it as a dependency
                       ;; would like to remove clojure-future-spec but cannot :()
                       ;; [philoskim/debux "0.5.6" :exclusions [clojure-future-spec]]
                       [philoskim/debux "0.6.3"]

                       ;; expound for human-readable clojure spec errors: https://github.com/bhb/expound
                       ;; highly recommended by many people
                       [expound "0.8.4"]
                       ;; also orchestra for checking :ret and :fn specs
                       ;; following version contains a small fix to show input args when :ret spec fails
                       [orchestra "2019.02.06-1"]

                       ;; https://github.com/clojure-goes-fast/clj-java-decompiler
                       ;; 0.3.0-SNAPSHOT fixes the issue with JDK 11: https://github.com/clojure-goes-fast/clj-java-decompiler/pull/4
                       [com.clojure-goes-fast/clj-java-decompiler "0.3.0"]

                       ;; I DON'T USE THIS
                       ;; zprint - cool pretty printer (e.g. configurable width)
                       ;; https://github.com/kkinnear/zprint#another-pretty-printer
                       ;; [zprint "0.4.16"]

                       ;; zpst to print additional data about exceptions, mostly params values
                       ;; https://github.com/kkinnear/zpst
                       ;; => not very useful since it doesn't capture java methods
                       ;; [zpst "0.1.6"]

                       ;; WARNING: both profiler and memory metter requre `-Djdk.attach.allowAttachSelf`
                       ;; clj-async-profiler is cool: http://clojure-goes-fast.com/blog/profiling-tool-async-profiler/
                       ;; http://clojure-goes-fast.com/blog/clj-async-profiler-tips/
                       [com.clojure-goes-fast/clj-async-profiler "0.4.0"]
                       ;; memory meter can be useful too: https://github.com/clojure-goes-fast/clj-memory-meter
                       [com.clojure-goes-fast/clj-memory-meter "0.1.2"]
                       ;; jol is also cool
                       [org.openjdk.jol/jol-core "0.9"]
                       ;; and jol's wrapper cljol for nice visualizations: https://github.com/jafingerhut/cljol
                       ;; installed locally from ~/workspace/clojure/cljol/
                       [cljol/cljol "0.3.0"]

                       ;; ghostwheel looks like a nice spec-enriching and tracing tool: https://github.com/gnl/ghostwheel#rationale
                       ;; I'm not using it and it potentially introduces deps conflicts (e.g. oz)
                       ;; [gnl/ghostwheel "0.2.1"]

                       ;; I don't really use this
                       ;; [lein-gossip "0.1.0-SNAPSHOT"]

                       ;; clj-usage-graph analyze call graphs! https://github.com/gfredericks/clj-usage-graph#user-profile-setup
                       ;; [com.gfredericks/clj-usage-graph "0.3.0"]
                       ;; conflicts with oz viz library:
                       ;; [com.gfredericks/clj-usage-graph "0.3.0"] -> [org.clojure/tools.analyzer.jvm "0.6.6"]
                       ;; overrides
                       ;; [metasoarous/oz "1.4.0"] -> [com.taoensso/sente "1.12.0"] -> [org.clojure/core.async "0.3.465"] -> [org.clojure/tools.analyzer.jvm "0.7.0"]
                       ;; and
                       ;; [metasoarous/oz "1.4.0"] -> [org.clojure/core.async "0.4.474"] -> [org.clojure/tools.analyzer.jvm "0.7.0"]


                       ;; spyscope doesn't work for me
                       ;; (take 20 (repeat #spy/d (+ 1 2 3)))
                       ;; ClassCastException java.io.StringWriter cannot be cast to clojure.lang.Associative  clojure.lang.RT.assoc (RT.java:792)
                       ;; => see https://github.com/dgrnbrg/spyscope/issues/26
                       ;; it also slows down leiningen: https://github.com/dgrnbrg/spyscope/issues/12
                       ;; Make sure to use 0.1.7-SNAPSHOT until a new version is released
                       ;; -> See https://github.com/dgrnbrg/spyscope/issues/26
                       ;; spyscope doesn't give us any real advantage -> don't use it
                       #_[spyscope "0.1.7-SNAPSHOT"]

                       ;; https://github.com/vvvvalvalval/scope-capture - capture locals in global vars for easier debugging
                       ;; I don't use this anymore
                       ;; [vvvvalvalval/scope-capture "0.3.3"]

                       ;; spec-provider to infer specs from examples: https://github.com/stathissideris/spec-provider
                       ;; I don't really use this
                       ;; [spec-provider "0.4.14"]

                       ;; spectrum for staticly checking specs: https://github.com/arohner/spectrum
                       ;; TODO: doesn't work:
                       ;; clojure-repl-experiments.core=> (require '[spectrum.check :as check])
                       ;; Syntax error (FileNotFoundException) compiling at (check.clj:1:1).
                       ;; Could not locate clojure/spec__init.class, clojure/spec.clj or clojure/spec.cljc on classpath.
                       #_[spectrum "0.1.4"]

                       ;; oz visualizations: https://github.com/metasoarous/oz
                       ;; check http://ozviz.io/ too
                       ;; Unfortunately, it causes following issues with `lein repl` in the codescene-cloud/worker
                       ;; Caused by: java.lang.ClassNotFoundException: clojure.core.async.Mutex
                       ;;  OR Caused by: java.lang.NoClassDefFoundError: Could not initialize class clojure.core.async__init
                       ;;[metasoarous/oz "1.4.1"]

                       ;; explicit dependency on jaxb-api for java 9 compatibility
                       ;; see https://stackoverflow.com/questions/43574426/how-to-resolve-java-lang-noclassdeffounderror-javax-xml-bind-jaxbexception-in-j
                       ;; SOMETHING in profiles.clj relies on this!
                       ;; [javax.xml.bind/jaxb-api "2.3.0"]

                       ]

        :aliases      {
                       ;; aliases for clj-usage-graph: https://github.com/gfredericks/clj-usage-graph#user-profile-setup
                       "var-graph"
                       ["with-profile" "+clj-usage-graph" "run"
                        "-m" "com.gfredericks.clj-usage-graph/var-graph"]
                       "namespace-graph"
                       ["with-profile" "+clj-usage-graph" "run"
                        "-m" "com.gfredericks.clj-usage-graph/namespace-graph"]}

        ;; the same injections are copied to the :repl profile because otherwise the aliases aren't available in the REPL
        ;; if you want to use just a base profile remote the :repl :injections temporarily
        :injections [(require 'pjstadig.humane-test-output)
                     (pjstadig.humane-test-output/activate!)

                     ;; inject REBL
                     #_(require '[cognitect.rebl :as rebl])
                     ;; don't inject automatically - cause problems
                     #_(require '[clojure-repl.java :as java])

                     ;; required manually
                     ;; (require '[criterium.core :as crit])

                     ;; install expound printer
                     (require '[clojure.spec.alpha :as s]
                              '[expound.alpha :as expound])
                     ;; If you're doing this in the user ns for your repl...
                     ;; (alter-var-root #'s/*explain-out* (constantly expound/printer))
                     ;; Otherwise, you can use the method from the README.
                     ;; don't use `set!` for explain-out because it will only work in REPL and not the whole application
                     ;; (set! s/*explain-out* expound/printer)
                     (alter-var-root #'s/*explain-out* (constantly expound/printer))

                     ;; debux is nice for quick tracing -> useless I always require it manually
                     ;; (require '[debux.core :refer [dbg dbgn]])

                     ;; (require '[alembic.still :as alembic])
                     ;; (require '[com.billpiel.sayid.core :as sayid])

                     ;; required manually
                     ;; decompiling is fun:
                     ;; (require '[clj-java-decompiler.core :as decompiler :refer [decompile]])


                                          ;;; Analyze var usages
                     ;;; Similar to REBL
                     ;;; https://metaredux.com/posts/2019/05/04/discovering-runtime-function-references-in-clojure.html
                     ;; (defn fdeps [val]
                     ;;   (set (some->> val class .getDeclaredFields
                     ;;                 (keep (fn [^java.lang.reflect.Field f]
                     ;;                         (or (and (identical? clojure.lang.Var (.getType f))
                     ;;                                  (java.lang.reflect.Modifier/isPublic (.getModifiers f))
                     ;;                                  (java.lang.reflect.Modifier/isStatic (.getModifiers f))
                     ;;                                  (-> f .getName (.startsWith "const__"))
                     ;;                                  (.get f val))
                     ;;                             nil))))))

                     ;; (defn namespaces
                     ;;   ;; ns-query is currently ignored
                     ;;   [ns-query]
                     ;;   (all-ns))

                     ;; (defn all-vars
                     ;;   "Returns a list of all currently loaded vars."
                     ;;   [{:keys [ns-query private?] :as var-query}]
                     ;;   ;; https://github.com/clojure-emacs/orchard/blob/master/src/orchard/query.clj#L68
                     ;;   (let [ns-vars (if private? ns-interns ns-publics)
                     ;;         nss (namespaces ns-query)]
                     ;;     (mapcat (comp vals ns-vars) nss)))

                     ;; (defn frefs
                     ;;   ([var]
                     ;;    (frefs var true))
                     ;;   ([var private?]
                     ;;    (let [all-vars (all-vars {:private? private?})
                     ;;          all-vals (map var-get all-vars)
                     ;;          deps-map (zipmap all-vars (map fdeps all-vals))]
                     ;;      (map first (filter (fn [[k v]] (contains? v var)) deps-map)))))

                     ;; (comment 
                     ;;   (defn foo []
                     ;;     (map inc (range 10)))

                     ;;   (fdeps foo)
                     ;;   ;; => #{#'clojure.core/map #'clojure.core/inc #'clojure.core/range}

                     ;;   (frefs #'map))



                     ]

        ;; lein-auto plugin configuration
        ;; TODO juraj: fix this in lein-antlr-jumarko
        :antlr-file-types ["TSql.g4"]  ; notice we need to use vector, because sets are not allowed
        ;; automatically compile ANTLR grammars when they are saved
        ;; just run `lein auto antlr`
        :auto {:default {:file-pattern #"\.g4"
                         :wait-time 500}}

        ;; TODO: for some reason ultra doesn't want to accept  custom configuration
        ;; :ultra        {:repl {:color-scheme {:delimiter       [:blue]
        ;;                                      :symbol          [:green]
        ;;                                      :keyword         [:bold :yellow]
        ;;                                      :function-symbol [:bold :yellow]}}}

         }}

