(ns clojure-experiments.clojure-1-10.spec
  (:require [clojure.spec.alpha :as s]))


;;; Undefine specs via `(s/def ::spec nil)`
;;; https://dev.clojure.org/jira/browse/CLJ-2060
;;; Particularly useful wehn working on complicated specs
;;; ----------------------------------------------------------------------------

(s/def ::a int?)
(s/def ::m (s/keys :req-un [::a ::b]))
(s/valid? ::m {:a "ahoj" :b 10})
;; => false

(s/def ::a nil)
;; => :clojure-experiments.1.10.spec/a

(s/valid? ::m {:a "ahoj" :b 10})
;; in Clojure 1.10
;; => true
;; in Clojure 1.9
;; => NullPointerException   clojure.spec.alpha/spec-impl/reify--1987 (alpha.clj:875)



;;; Improved error printing
;;; ----------------------------------------------------------------------------

(comment
  (let [a 2 b ])
;;   Syntax error macroexpanding clojure.core/let at (spec.clj:5:1).
;;   () - failed: Insufficient input at: [:bindings :init-expr] spec: :clojure.core.specs.alpha/bindings

;; VS. error in 1.9
;;   CompilerException clojure.lang.ExceptionInfo: Call to clojure.core/let did not conform to spec:
;;   In: [0] val: () fails spec: :clojure.core.specs.alpha/bindings at: [:args :bindings :init-expr] predicate: any?,  Insufficient input
;;   #:clojure.spec.alpha{:problems [{:path [:args :bindings :init-expr], :reason "Insufficient input", :pred clojure.core/any?, :val (), :via [:clojure.core.specs.alpha/bindings :clojure.core.specs.alpha/bindings], :in [0]}], :spec #object[clojure.spec.alpha$regex_spec_impl$reify__2436 0x1036e1ce "clojure.spec.alpha$regex_spec_impl$reify__2436@1036e1ce"], :value ([a 2 b]), :args ([a 2 b])}, compiling:(*cider-repl codescene-cloud/web:localhost:62509(clj)*:1251:37) 

  ;; end
)
