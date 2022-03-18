(ns clojure-experiments.compiler.decompiler
  "tools.decompiler: https://github.com/Bronsa/tools.decompiler"
  ;; Even the require doesn't work: 
  ;;   Syntax error compiling at (decompiler.clj:9:1).
  ;;   namespace 'clojure.tools.decompiler.sugar' not found
  #_(:require [clojure.tools.decompiler :as d]))


;; TODO: doesn't work => IndexOutOfBoundsException deep down in decompiler's code
;; (d/decompile-classfiles {:input-path "/Users/jumar/tools/clojure/rebl/REBL-0.9.108/classes"
;;                          :output-path "/Users/jumar/tools/clojure/rebl/REBL-0.9.108/src"})



;; http://clojure-goes-fast.com/blog/introspection-tools-java-decompilers/
(require '[clj-java-decompiler.core :refer [decompile disassemble] :as decompiler])
;; see https://insideclojure.org/2014/12/15/warn-on-boxed/

;; 1. unchecked math using specific operations
(decompile
 (loop [i 100, sum 0] (if (< i 0)
                        sum
                        (recur (unchecked-dec i) (unchecked-add sum i)))))

;; 2. checked math - notice using Numbers.dec, Numbers.add, and Numbers.num => wrappers!
(decompile
 (loop [i 100, sum 0] (if (< i 0)
                        sum
                        (recur (dec i) (+ sum i)))))

;; 3. Compiling using unchecked-math is the same as the first case
(set! *unchecked-math* true)
(decompile
 (loop [i 100, sum 0] (if (< i 0)
                        sum
                        (recur (dec i) (+ sum i)))))
(set! *unchecked-math* false)

