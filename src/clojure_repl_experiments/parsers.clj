(ns clojure-repl-experiments.parsers
  (:require [clojure.tools.analyzer.jvm :as ana]
            [clojure.tools.emitter.jvm :as e]
            [clojure.tools.reader :as r]
            [clojure.tools.reader.edn :as re]
            [net.cgrand.sjacket.parser :as sjp]))

;;; tools.reader

;; very simple example
(r/read-string "(+ 1 2)")

;; more complex - read experiments.clj
#_(r/read-string (slurp "src/clojure_repl_experiments/experiments.clj"))

#_(re/read-string (slurp "src/clojure_repl_experiments/experiments.clj"))


;;; sjacket: https://github.com/cgrand/sjacket/blob/master/test/net/cgrand/sjacket/test.clj
#_(sjp/parser (slurp "/Users/jumar/workspace/clojure/clojure-repl-experiments/src/clojure_repl_experiments/experiments.clj"))



;;; tools.analyzer: https://github.com/clojure/tools.analyzer.jvm
;;; recommended by Bronsa for my use case
;;; great usage example is eastwood: https://github.com/jonase/eastwood/blob/master/src/eastwood/analyze_ns.clj#L320
;; unfortunatelly, `analyze-file` function is a reall mess and requires sensitive configuration
#_(eastwood.analyze-ns/analyze-file "src/clojure_repl_experiments/experiments.clj"
                                  :opt {:debug #{:none}
                                        :callback #(println %)})


;;; great ns deps analyzer: https://github.com/alexander-yakushev/ns-graph 
#_(ns-graph.core/depgraph* {:source-paths "src/"
                          :debug true
                          :format "svg"})


;;; tools.emitter
(e/eval '(+ 1 2) {:debug true})
