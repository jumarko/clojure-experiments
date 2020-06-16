(defproject clojure-experiments "0.2.0-SNAPSHOT"
  :description "My clojure REPL experiments."
  :url "https://github.com/jumarko/clojure-experiments"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.10.1"]
                 ;; leads to "No matching method maybeSpecialTag" error - may be connected with virgil?
                 [org.clojure/clojure "1.10.1" :classifier "sources"]
                 ;; why would I need this in projects dependencies?
                 #_[org.clojure/tools.deps.alpha "0.5.460"]
                 [org.clojure/test.check "0.10.0"]
                 [com.gfredericks/test.chuck "0.2.10"]
                 [org.apache.pdfbox/pdfbox "2.0.7"
                  ;; we prefer log4j2
                  :exclusions [commons-logging]]
                 [net.java.dev.jna/jna "4.4.0"]
                 [org.flatland/useful "0.11.5"]
                 [org.apache.poi/poi-ooxml "3.17"]
                 [org.clojure/core.async "1.2.603"]
                 [funcool/cats "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.julienxx/clj-slack "0.5.5"]
                 ;; Does this conflict with oz because of aleph/sente conflict?
                 ;;   Syntax error (FileNotFoundException) compiling at (server.clj:1:1).
                 ;;   Could not locate taoensso/sente/server_adapters/aleph__init.class, taoensso/sente/server_adapters/aleph.clj or taoensso/sente/server_adapters/aleph.cljc on classpath. Please check that namespaces with dashes use underscores in the Clojure file name.
                 ;; jutsu is no longer maintained! https://github.com/hswick/jutsu
                 ;; [hswick/jutsu "0.1.3"]
                 ;; https://github.com/findmyway/plotly-clj
                 ;; not sure how to use this:
                 ;; [plotly-clj "0.1.1"]
                 ;; libpython-clj allows me to use plotly in Clojure tools
                 ;; [clj-python/libpython-clj "1.44"]
                 [clojure-complete "0.2.4"]
                 [com.rpl/specter "1.1.0"]
                 [quil "2.8.0"]
                 [inspector-jay "0.3"]
                 [org.clojure/tools.reader "1.1.1"]
                 [net.cgrand/sjacket "0.1.1"]
                 [jonase/eastwood "0.3.8"]
                 [ns-graph "0.1.2"]
                 [com.climate/claypoole "1.1.4"]
                 [etaoin "0.1.9"]
                 ;; Java Object Layout tool - see `jdk` namespace
                 [org.openjdk.jol/jol-core "0.9"]
                 [org.clojure/java.jmx "0.3.4"]
                 [me.raynes/conch "0.8.0"]
                 ;; tupelo library with lots of useful functionsl ike `spy`, `lazy-cons`, `with-exception-default`
                 [tupelo "0.9.71"]

                 ;; https://github.com/clojure-goes-fast/clj-java-decompiler
                 ;; 0.3.0-SNAPSHOT fixes the issue with JDK 11: https://github.com/clojure-goes-fast/clj-java-decompiler/pull/4
                 [com.clojure-goes-fast/clj-java-decompiler "0.3.0-SNAPSHOT"]
                 ;; http://clojure-goes-fast.com/blog/profiling-tool-async-profiler/
                 [com.clojure-goes-fast/clj-async-profiler "0.4.0"]
                 ;; http://clojure-goes-fast.com/blog/latency-tool-jvm-hiccup-meter/
                 [com.clojure-goes-fast/jvm-hiccup-meter "0.1.1"]
                 [com.clojure-goes-fast/clj-memory-meter "0.1.2"]
                 [com.clojure-goes-fast/jvm-alloc-rate-meter "0.1.3"]

                 [org.clojars.pntblnk/clj-ldap "0.0.15"]
                 [vvvvalvalval/scope-capture "0.1.4"]
                 [org.clojure/tools.trace "0.7.9"]
                 [nodisassemble "0.1.3"]

                 ;; Problem with old tools.analyzer.jvm version (transitive dep of emmiter):
                 ;; 2. Unhandled clojure.lang.Compiler$CompilerException
                 ;;    Error compiling clojure/tools/analyzer/jvm.clj at (1:1)
                 ;; 1. Caused by java.lang.IllegalAccessError
                 ;;    resolve-var does not exist
                 #_[org.clojure/tools.emitter.jvm "0.1.0-beta5"]
                 [com.gfredericks/test.chuck "0.2.10"]
                 [phrase "0.3-alpha3"]
                 [net.n01se/clojure-jna "1.0.0"]
                 [com.taoensso/truss "1.5.0"]
                 [net.cgrand/xforms "0.18.2"]
                 ;; this can break cider?! - dev profile plugins: https://github.com/Clojure2D/clojure2d/blob/master/project.clj
                 [clojure2d "1.1.0"
                  ;; we use log4j2 logging preferably
                  :exclusions [org.slf4j/slf4j-simple
                               ;; this is included as extra dep for clindex (see below)
                               org.clojure/tools.namespace
                               cider.nrepl]]
                 [datascript "0.16.6"]
                 [lambdaisland/deep-diff "0.0-8"]
                 [bocko "1.0.0"]
                 ;; OZ - powerful data visualizations https://github.com/metasoarous/oz
                 [metasoarous/oz "1.6.0-alpha1"
                  :exclusions [
                               ;; this is included as extra dep for clindex (see below)
                               org.clojure/tools.namespace
                               ]]
                 ;; even easier Vega lite visualizations: https://github.com/jsa-aerial/hanami
                 ;; -> installed from local repo: ~/workspace/clojure/hanami
                 [aerial.hanami "0.10.11"]
                 ;; modern version of sente required by oz
                 #_[com.taoensso/sente "1.13.1"]
                 ;; and sente requires newer transit version
                 #_[com.cognitect/transit-clj  "0.8.313"]
                 #_[com.cognitect/transit-cljs "0.8.256"]

                 [org.clojure/java.data "0.1.1"]
                 [fn-fx/fn-fx-javafx "0.5.0-SNAPSHOT"]
                 ;; cool alternative to fn-fx: https://github.com/cljfx/cljfx
                 [cljfx "1.5.1"]
                 [rewrite-clj "0.6.1"]
                 [amperity/greenlight "0.1.2"]
                 ;; cool experiment but not actively developed and obsolete
                 ;; also includes old rrb.vector version transitvely which makes it fail for JDK 11
                 ;; [bronsa/tools.decompiler "0.1.0-alpha1"]

                 ;; REBL: https://github.com/cognitect-labs/REBL-distro
                 ;; It has to be installed in the local repository first
                 ;; - download it here: http://rebl.cognitect.com/download.html
                 ;; - mvn install:install-file -Dfile=/Users/jumar/tools/clojure/rebl/REBL-0.9.109/REBL-0.9.109.jar -DgroupId=com.cognitect -DartifactId=rebl -Dversion=0.9.109 -Dpackaging=jar -DgeneratePom=true
                 ;; I DON'T USE THIS!
                 ;; [com.cognitect/rebl "0.9.109"]

                 ;; statistics functions - e.g. TTest
                 [org.apache.commons/commons-math3 "3.6.1"]

                 ;; https://github.com/generateme/fastmath
                 [generateme/fastmath "1.5.2"]

                 ;; kixi.stats: https://github.com/mastodonC/kixi.stats
                 ;; see also lambdaisland: https://lambdaisland.com/episodes/clojure-data-science-kixi-stats
                 [kixi/stats "0.4.4"] 
                 [redux "0.1.4"]
                 [net.cgrand/xforms "0.19.0"]


                 [thi.ng/geom "1.0.0-RC3"]
                 ;; added explicitly otherwise I was getting "namespace 'cheshire.factory' not found error"
                 [cheshire "5.8.1"]
                 [org.flatland/ordered "1.5.7"]

                 [spec-provider "0.4.14"]

                 [tesser.core "1.0.3"]

                 [clj-http "3.10.0"]

                 ;; shell scripting experiments
                 [com.palletops/stevedore "0.8.0-beta.7"]

                 ;; simple profiling
                 [com.taoensso/tufte "2.0.1"]

;;; logging with clojure.tools.logging and log4j2
;;; http://brownsofa.org/blog/2015/06/14/clojure-in-production-logging/
;;; https://github.com/clojure/tools.logging
;;; https://logging.apache.org/log4j/2.x/manual/configuration.html
                 [org.clojure/tools.logging "0.5.0"]
                 ;; Note: this is really needed https://logging.apache.org/log4j/2.x/maven-artifacts.html
                 ;; Otherwise you'd get "ERROR StatusLogger No Log4j 2 configuration file found. " 
                 [org.apache.logging.log4j/log4j-api "2.12.1"]
                 [org.apache.logging.log4j/log4j-core"2.12.1"]
                 ;; also this slf4j bridge
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.12.1"]
                 ;; https://github.com/RickMoynihan/lein-tools-deps
                 [lein-tools-deps "0.4.5"]

                 ;; https://github.com/zane/vega.repl
                 ;;  the simplest possible way to go from Clojure data and a Vega or Vega-Lite spec to a rendered visualization
                 ;; built from custom ~/workspace/clojure/vega.repl/project.clj
                 ;; [vega.repl/vega.repl "0.1.0-SNAPSHOT"]

                 ;; to demo spec-based configuration checking -> see config.clj
                 [cprop "0.1.14"]

                 ;; spectrum for static-type checks based on spec
                 [spectrum "0.2.5"]

                 ;; Note: this is also in ~/.lein/profiles.clj !!!
                 ;; clindex is a cool tool for indexing project dependencies: https://github.com/jpmonettas/clindex
                 ;; check also clograms: https://github.com/jpmonettas/clograms
                 [clindex "0.4.3"]
                 ;; custom tools.namespace version required for clindex
                 ;; -> see https://groups.google.com/forum/?utm_medium=email&utm_source=footer#!msg/clojure/cV6rvgR9Pfs/Z_kOOAwwBAAJ
                 [jpmonettas/tools.namespace "0.3.2"]

                 ;; cognitect's aws-api: https://github.com/cognitect-labs/aws-api
                 ;; => search on mvnrepository.com http://mvnrepository.com/search?q=com.cognitect.aws&ref=opensearch
                 [com.cognitect.aws/api "0.8.456"] 
                 [com.cognitect.aws/endpoints "1.1.11.789"] 
                 ;; logs: https://mvnrepository.com/artifact/com.cognitect.aws/logs
                 [com.cognitect.aws/logs "798.2.672.0"]
                 ;; core.cache and core.memoize deps explicitly stated to not conflict with other libs
                 [org.clojure/core.cache "1.0.207"]
                 [org.clojure/core.memoize "1.0.236"]

                 ;; tech.ml.dataset requires higher smile-* libs versions than fastmath
                 [techascent/tech.ml.dataset "2.0"]
                 [com.github.haifengl/smile-core "2.4.0"]
                 [com.github.haifengl/smile-netlib "2.4.0"]
                 [com.github.haifengl/smile-io "2.4.0"]
                 [com.github.haifengl/smile-math "2.4.0"]
                 [com.github.haifengl/smile-graph "2.4.0"]
                 [com.github.haifengl/smile-data "2.4.0"]
                 ]

  ;; https://github.com/RickMoynihan/lein-tools-deps
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}

  :java-source-paths ["src/java"]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
             "-Djdk.attach.allowAttachSelf=true"
             ;; just as an example of overriding default configuration values
             "-Dconf=my-config.edn"]
  :main ^:skip-aot clojure-experiments.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             ;; notice sources for development!
             })
