(defproject clojure-repl-experiments "0.1.0-SNAPSHOT"
  :description "My clojure REPL experiments."
  :url "https://github.com/jumarko/clojure-repl-experiments"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta2"]
                 [org.clojure/clojure "1.9.0-beta2" :classifier "sources"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 [org.apache.pdfbox/pdfbox "2.0.7"]
                 [net.java.dev.jna/jna "4.4.0"]
                 [org.flatland/useful "0.11.5"]
                 [org.apache.poi/poi-ooxml "3.17"]
                 [org.clojure/core.async "0.3.443"]
                 [funcool/cats "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.julienxx/clj-slack "0.5.5"]
                 [hswick/jutsu "0.1.1"]
                 [clojure-complete "0.2.4"]
                 [com.rpl/specter "1.0.4"]
                 [quil "2.6.0"]
                 [inspector-jay "0.3"]
                 [org.clojure/tools.reader "1.1.0"]
                 [net.cgrand/sjacket "0.1.1"]
                 [org.clojure/tools.analyzer.jvm "0.7.1"]
                 [jonase/eastwood "0.2.5"]
                 [ns-graph "0.1.2"]
                 [com.climate/claypoole "1.1.4"]
                 [etaoin "0.1.9"]
                 ;; Java Object Layout tool - see `jdk` namespace
                 [org.openjdk.jol/jol-core "0.9"]
                 [org.clojure/java.jmx "0.3.4"]
                 [me.raynes/conch "0.8.0"]]
  :java-source-paths ["src/java"]
  :main ^:skip-aot clojure-repl-experiments.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
