(defproject clojure-repl-experiments "0.1.0-SNAPSHOT"
  :description "My clojure REPL experiments."
  :url "https://github.com/jumarko/clojure-repl-experiments"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta1"]
                 [org.clojure/clojure "1.9.0-beta1" :classifier "sources"]
                 [org.apache.pdfbox/pdfbox "2.0.7"]
                 [net.java.dev.jna/jna "4.4.0"]
                 [org.flatland/useful "0.11.5"]

                 [org.clojure/core.async "0.3.443"]]
  :main ^:skip-aot clojure-repl-experiments.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
