(defproject optimizing-clojure "0.1.0-SNAPSHOT"
  :description "Examples for the blog post series Notes on Optimizing Clojure Performance"
  :url "https://cuddly-octo-palm-tree.com/posts/2022-01-16-opt-clj-1/"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :main ^:skip-aot p01-overview
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
