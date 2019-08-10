(ns clojure-experiments.decompiler
  "tools.decompiler: https://github.com/Bronsa/tools.decompiler"
  ;; Even the require doesn't work: 
  ;;   Syntax error compiling at (decompiler.clj:9:1).
  ;;   namespace 'clojure.tools.decompiler.sugar' not found
  #_(:require [clojure.tools.decompiler :as d]))


;; TODO: doesn't work => IndexOutOfBoundsException deep down in decompiler's code
;; (d/decompile-classfiles {:input-path "/Users/jumar/tools/clojure/rebl/REBL-0.9.108/classes"
;;                          :output-path "/Users/jumar/tools/clojure/rebl/REBL-0.9.108/src"})


