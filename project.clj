(defproject clojure-experiments "0.2.0-SNAPSHOT"
  :description "My clojure REPL experiments."
  :url "https://github.com/jumarko/clojure-experiments"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 ;; leads to "No matching method maybeSpecialTag" error - may be connected with virgil?
                 [org.clojure/clojure "1.10.0" :classifier "sources"]
                 [org.clojure/tools.deps.alpha "0.5.460"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 [org.apache.pdfbox/pdfbox "2.0.7"]
                 [net.java.dev.jna/jna "4.4.0"]
                 [org.flatland/useful "0.11.5"]
                 [org.apache.poi/poi-ooxml "3.17"]
                 [org.clojure/core.async "0.4.474"]
                 [funcool/cats "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.julienxx/clj-slack "0.5.5"]
                 ;; Does this conflict with oz because of aleph/sente conflict?
                 ;;   Syntax error (FileNotFoundException) compiling at (server.clj:1:1).
                 ;;   Could not locate taoensso/sente/server_adapters/aleph__init.class, taoensso/sente/server_adapters/aleph.clj or taoensso/sente/server_adapters/aleph.cljc on classpath. Please check that namespaces with dashes use underscores in the Clojure file name.
                 ;; [hswick/jutsu "0.1.1"]
                 [clojure-complete "0.2.4"]
                 [com.rpl/specter "1.1.0"]
                 [quil "2.8.0"]
                 [inspector-jay "0.3"]
                 [org.clojure/tools.reader "1.1.1"]
                 [net.cgrand/sjacket "0.1.1"]
                 [jonase/eastwood "0.2.5"]
                 [ns-graph "0.1.2"]
                 [com.climate/claypoole "1.1.4"]
                 [etaoin "0.1.9"]
                 ;; Java Object Layout tool - see `jdk` namespace
                 [org.openjdk.jol/jol-core "0.9"]
                 [org.clojure/java.jmx "0.3.4"]
                 [me.raynes/conch "0.8.0"]
                 ;; tupelo library with lots of useful functionsl ike `spy`, `lazy-cons`, `with-exception-default`
                 [tupelo "0.9.71"]
                 [com.clojure-goes-fast/clj-java-decompiler "0.2.1"]
                 [org.clojars.pntblnk/clj-ldap "0.0.15"]
                 [vvvvalvalval/scope-capture "0.1.4"]
                 [org.clojure/tools.trace "0.7.9"]
                 [nodisassemble "0.1.3"]
                 ;; http://clojure-goes-fast.com/blog/profiling-tool-async-profiler/
                 [com.clojure-goes-fast/clj-async-profiler "0.3.0"]

                 ;; Problem with old tools.analyzer.jvm version (transitive dep of emmiter):
                 ;; 2. Unhandled clojure.lang.Compiler$CompilerException
                 ;;    Error compiling clojure/tools/analyzer/jvm.clj at (1:1)
                 ;; 1. Caused by java.lang.IllegalAccessError
                 ;;    resolve-var does not exist
                 #_[org.clojure/tools.emitter.jvm "0.1.0-beta5"]
                 [com.gfredericks/test.chuck "0.2.9"]
                 [phrase "0.3-alpha3"]
                 [net.n01se/clojure-jna "1.0.0"]
                 [com.taoensso/truss "1.5.0"]
                 [net.cgrand/xforms "0.18.2"]
                 ;; this can break cider?! - dev profile plugins: https://github.com/Clojure2D/clojure2d/blob/master/project.clj
                 [clojure2d "1.1.0"]
                 [datascript "0.16.6"]
                 [lambdaisland/deep-diff "0.0-8"]
                 ;; http://clojure-goes-fast.com/blog/latency-tool-jvm-hiccup-meter/
                 [com.clojure-goes-fast/jvm-hiccup-meter "0.1.1"]
                 [bocko "1.0.0"]
                 ;; OZ - powerful data visualizations https://github.com/metasoarous/oz
                 [metasoarous/oz "1.6.0-alpha1"]
                 [aerial.hanami "0.2.0"]
                 ;; modern version of sente required by oz
                 #_[com.taoensso/sente "1.13.1"]
                 ;; and sente requires newer transit version
                 #_[com.cognitect/transit-clj  "0.8.313"]
                 #_[com.cognitect/transit-cljs "0.8.256"]

                 [org.clojure/java.data "0.1.1"]
                 [fn-fx/fn-fx-javafx "0.5.0-SNAPSHOT"]
                 [rewrite-clj "0.6.1"]
                 [amperity/greenlight "0.1.2"]
                 [bronsa/tools.decompiler "0.1.0-alpha1"]

                 ;; REBL: https://github.com/cognitect-labs/REBL-distro
                 ;; It has to be installed in the local repository first
                 ;; - download it here: http://rebl.cognitect.com/download.html
                 ;; - mvn install:install-file -Dfile=/Users/jumar/tools/clojure/rebl/REBL-0.9.109/REBL-0.9.109.jar -DgroupId=com.cognitect -DartifactId=rebl -Dversion=0.9.109 -Dpackaging=jar -DgeneratePom=true
                 [com.cognitect/rebl "0.9.109"]

                 ;; statistics functions - e.g. TTest
                 [org.apache.commons/commons-math3 "3.6.1"]

                 ;; kixi.stats: https://github.com/mastodonC/kixi.stats
                 ;; see also lambdaisland: https://lambdaisland.com/episodes/clojure-data-science-kixi-stats
                 [kixi/stats "0.4.4"] 
                 [redux "0.1.4"]
                 [net.cgrand/xforms "0.19.0"]


                 [thi.ng/geom "1.0.0-RC3"]
                 ;; added explicitly otherwise I was getting "namespace 'cheshire.factory' not found error"
                 [cheshire "5.8.1"]
                 [org.flatland/ordered "1.5.7"]
                 ]
  :java-source-paths ["src/java"]
  :jvm-opts ["-Djdk.attach.allowAttachSelf=true"]
  :main ^:skip-aot clojure-experiments.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             ;; notice sources for development!
             })
