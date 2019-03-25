(ns clojure-experiments.jdk)

;;; jol - Java Object Layout tool: http://openjdk.java.net/projects/code-tools/jol/
;;; http://central.maven.org/maven2/org/openjdk/jol/jol-cli
;;; org.openjdk.jol/jol-core

(import '(org.openjdk.jol.info ClassLayout))

(.toPrintable (ClassLayout/parseInstance []))
