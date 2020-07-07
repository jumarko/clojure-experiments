(ns clojure-experiments.java.compilation
  (:import (javax.tools ToolProvider)))

;; see https://www.logicbig.com/tutorials/core-java-tutorial/java-se-compiler-api/java-compiler-api-intro.html
(defn compile-java [path]
  (let [compiler (ToolProvider/getSystemJavaCompiler)]
    (.run compiler nil System/out System/err (into-array [path]))))

(comment

  ;; except the first run, this usually takes ~300 ms
  (require '[clj-async-profiler.core :as prof])
  (prof/start)
  ;; YOUR CODE HERE
  (time (compile-java "src/java/net/curiousprogrammer/javaexperiments/JavaCompiler.java"))
  (prof/stop {:width 2400})

  )
