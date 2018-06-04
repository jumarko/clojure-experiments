(defproject clojure-repl-experiments "0.1.0-SNAPSHOT"
  :description "My clojure REPL experiments."
  :url "https://github.com/jumarko/clojure-repl-experiments"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0-alpha4"]
                 [org.clojure/clojure "1.10.0-alpha4" :classifier "sources"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 [org.apache.pdfbox/pdfbox "2.0.7"]
                 [net.java.dev.jna/jna "4.4.0"]
                 [org.flatland/useful "0.11.5"]
                 [org.apache.poi/poi-ooxml "3.17"]
                 [org.clojure/core.async "0.4.474"]
                 [funcool/cats "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.julienxx/clj-slack "0.5.5"]
                 [hswick/jutsu "0.1.1"]
                 [clojure-complete "0.2.4"]
                 [com.rpl/specter "1.1.0"]
                 [quil "2.6.0"]
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
                 [com.clojure-goes-fast/clj-java-decompiler "0.1.0"]
                 [org.clojars.pntblnk/clj-ldap "0.0.15"]
                 [vvvvalvalval/scope-capture "0.1.4"]
                 [org.clojure/tools.trace "0.7.9"]
                 [nodisassemble "0.1.3"]

                 [org.clojure/tools.emitter.jvm "0.1.0-beta5"]
                 [com.gfredericks/test.chuck "0.2.9"]
                 [phrase "0.3-alpha3"]]
  :java-source-paths ["src/java"]
  :main ^:skip-aot clojure-repl-experiments.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             ;; notice sources for development!
             })
