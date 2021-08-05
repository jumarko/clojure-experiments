(ns clojure-experiments.clojure-inside-out.v01-introduction
  "Clojure Inside Out video course: https://learning.oreilly.com/videos/clojure-inside-out/9781449368647/9781449368647-video152786
  Functional composition: https://github.com/ctford/functional-composition

  Make sure to start SuperCollider.app first.
  "
  (:require [overtone.core :as oc]))


;;; Functional Composition
;;; https://github.com/ctford/functional-composition/blob/master/src/goldberg/variations/canone_alla_quarta.clj


;; try this instead
(comment
  
  (oc/boot-internal-server)

  (oc/boot-external-server)
  (oc/connect-external-server))


;; this doesn't work => see my issue: https://github.com/overtone/overtone/issues/474
;;; Should I try `connect-external-server instead? https://github.com/overtone/overtone/issues/349
#_(require '[overtone.live :as ol])
;; 1. Caused by java.lang.NullPointerException
;;    Cannot invoke
;;    "org.eclipse.aether.RepositorySystem.newLocalRepositoryManager(org.eclipse.aether.RepositorySystemSession,
;;    org.eclipse.aether.repository.LocalRepository)" because "system" is null

;;                  maven.clj:  197  clojure.tools.deps.alpha.util.maven/make-session
;;                  maven.clj:  194  clojure.tools.deps.alpha.util.maven/make-session
;;                  maven.clj:   94  clojure.tools.deps.alpha.extensions.maven/eval18401/fn/fn
;;                session.clj:   23  clojure.tools.deps.alpha.util.session/retrieve
;;                session.clj:   14  clojure.tools.deps.alpha.util.session/retrieve
;;                  maven.clj:   94  clojure.tools.deps.alpha.extensions.maven/eval18401/fn
;;               MultiFn.java:  244  clojure.lang.MultiFn/invoke
;;                  alpha.clj:  192  clojure.tools.deps.alpha/expand-deps/fn
;;                  alpha.clj:  191  clojure.tools.deps.alpha/expand-deps
;;                  alpha.clj:  169  clojure.tools.deps.alpha/expand-deps
;;                  alpha.clj:  237  clojure.tools.deps.alpha/resolve-deps
;;                  alpha.clj:  217  clojure.tools.deps.alpha/resolve-deps
;;                  alpha.clj:  231  clojure.tools.deps.alpha/resolve-deps
;;                  alpha.clj:  217  clojure.tools.deps.alpha/resolve-deps
;;                 bundle.clj:  198  badigeon.bundle/extract-native-dependencies
;;                 bundle.clj:  182  badigeon.bundle/extract-native-dependencies
;;               jna_path.clj:   38  overtone.jna-path/eval19081
;;               jna_path.clj:   38  overtone.jna-path/eval19081
;;              Compiler.java: 7177  clojure.lang.Compiler/eval
;;              Compiler.java: 7636  clojure.lang.Compiler/load
;;                    RT.java:  381  clojure.lang.RT/loadResourceScript
;;                    RT.java:  372  clojure.lang.RT/loadResourceScript
;;                    RT.java:  459  clojure.lang.RT/load


