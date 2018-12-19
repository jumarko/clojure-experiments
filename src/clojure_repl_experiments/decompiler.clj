(ns clojure-repl-experiments.decompiler
  "tools.decompiler: https://github.com/Bronsa/tools.decompiler"
  (:require [clojure.tools.decompiler :as d]))


;; TODO: doesn't work => IndexOutOfBoundsException deep down in decompiler's code
#_(d/decompile-classfiles {:input-path "/Users/jumar/tools/clojure/rebl/REBL-0.9.108/classes"
                         :output-path "/Users/jumar/tools/clojure/rebl/REBL-0.9.108/src"})


