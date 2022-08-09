(ns clojure-experiments.specs.guardrails
  "See https://github.com/fulcrologic/guardrails#quick-start
  and https://www.fulcrologic.com/open-source#guardrails"
  (:require
   [clojure.spec.alpha :as s]
   [com.fulcrologic.guardrails.core :refer [>def >defn]]))

;; >def is spec that's only used in development and removed from production builds
;; see `com.fulcrologic.guardrails.noop`
(>def ::thing (s/or :i int? :s string?))
(>defn f
       [i]
       [::thing => int?]
       (if (string? i) 0 (inc i)))

(f 10)
;; => 11
(f "10")
;; => 0

;; now try it with an invalid argument type
(comment
  (f {:i 10})
  ;; WARNING: the error reporting might collide with stest/expound itself
  ;; - this happens when you call `(clojure.spec.test.alpha/instrument)`
  ;;   which my cider load buffer implementation does by default.
  ;; so if you load the whole buffer you will see this exception and it fails
  ;; without actually calling the body of `f`
  ;; Spec assertion failed.
  ;;
  ;; Spec: #object[clojure.spec.alpha$regex_spec_impl$reify__2503 0x173f13c8 "clojure.spec.alpha$regex_spec_impl$reify__2503@173f13c8"]
  ;; Value: ({:i 10})
  ;;
  ;; Problems: 
  ;;
  ;; val: {:i 10}
  ;; in: [0]
  ;; failed: int?
  ;; spec: :clojure-experiments.specs.guardrails/thing
  ;; at: [:i :i]
  ;;
  ;; val: {:i 10}
  ;; in: [0]
  ;; failed: string?
  ;; spec: :clojure-experiments.specs.guardrails/thing
  ;; at: [:i :s]


  ;; but if you only eval `f` and then run this again you see ClassCastException
  (f {:i 10})

  ;; => throws exception (by default, the failure doesn't stop the implementation)
  ;; 1. Unhandled java.lang.ClassCastException
  ;; class java.lang.Object cannot be cast to class java.lang.Number (java.lang.Object and
  ;;   java.lang.Number are in module java.base of loader 'bootstrap')
  ;;
  ;; ... and print's nice error on stdout:
  ;; .../src/clojure_experiments/specs/guardrails.clj:11 f's argument list
  ;; -- Spec failed --------------------
  ;;
  ;;   [#object[java.lang.Object 0x1bea1137 "java.lang.Object@1bea1137"]]
  ;;   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  ;;
  ;; should satisfy
  ;; 
  ;;   int?
  ;; 
  ;; or
  ;; 
  ;;   string?
  ;;
  ;; -- Relevant specs -------
  ;;
  ;; :clojure-experiments.specs.guardrails/thing:
  ;;   (clojure.spec.alpha/or :i clojure.core/int? :s clojure.core/string?)

  .)
