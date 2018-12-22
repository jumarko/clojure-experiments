(ns clojure-experiments.1.10.error-messages)

;;;; https://www.reddit.com/r/Clojure/comments/9nb80n/clojure_1100rc1/?st=joi9pk1q&sh=65782ca6


:::5
;;=> Syntax error reading source at (2:0). Invalid token: :::5
;; VS 1.9
;;   RuntimeException Invalid token: :::5  clojure.lang.Util.runtimeException (Util.java:221)


(let [x])
;;=> Syntax error macroexpanding clojure.core/let at (clojure-experiments:localhost:49677(clj)*:113:39).
;; () - failed: Insufficient input at: [:bindings :init-expr] spec: :clojure.core.specs.alpha/bindings
;; VS 1.9
;;   CompilerException clojure.lang.ExceptionInfo: Call to clojure.core/let did not conform to spec:
;;   In: [0] val: () fails spec: :clojure.core.specs.alpha/bindings at: [:args :bindings :init-expr] predicate: any?,  Insufficient input
;;   #:clojure.spec.alpha{:problems [{:path [:args :bindings :init-expr], :reason "Insufficient input", :pred clojure.core/any?, :val (), :via [:clojure.core.specs.alpha/bindings :clojure.core.specs.alpha/bindings], :in [0]}], :spec #object[clojure.spec.alpha$regex_spec_impl$reify__2436 0x3371618a "clojure.spec.alpha$regex_spec_impl$reify__2436@3371618a"], :value ([x]), :args ([x])}, compiling:(*cider-repl codescene-cloud/web:localhost:49931(clj)*:53:27) 


(cond 1)
;;=> Syntax error macroexpanding cond at (3:1). cond requires an even number of forms
;; VS 1.9
;;   IllegalArgumentException cond requires an even number of forms  clojure.core/cond (core.clj:600)


(def 5) 
;;=> Syntax error compiling def at (clojure-experiments:localhost:49677(clj)*:125:39).
;;   First argument to def must be a Symbol
;; VS 1.9
;;   Syntax error compiling def at (clojure-experiments:localhost:49965(clj)*:46:32).
;;   First argument to def must be a Symbol


(defmulti 5 class)
;;=> Unexpected error macroexpanding defmulti at (01_error_messages.clj:35:1).
;;   java.lang.Long cannot be cast to clojure.lang.IObj
;; VS 1.9
;;   ClassCastException java.lang.Long cannot be cast to clojure.lang.IObj  clojure.core/with-meta--5142 (core.clj:217)


(/ 1 0)
;;=> Execution error (ArithmeticException) at clojure_repl_experiments.1.10.datafy$eval19822/invokeStatic (form-init7798393875566821170.clj:128).
;;   Divide by zero
;; VS 1.9
;;   ArithmeticException Divide by zero  clojure.lang.Numbers.divide (Numbers.java:163)
 

(+ 1 :a)
;;=> Execution error (ClassCastException) at clojure_repl_experiments.1.10.error_messages$eval16249/invokeStatic (form-init1074562990802515819.clj:49).
;;   clojure.lang.Keyword cannot be cast to java.lang.Number
;; VS 1.9
;; ClassCastException clojure.lang.Keyword cannot be cast to java.lang.Number  clojure.lang.Numbers.add (Numbers.java:128)


(assert false)
;;=> Execution error (AssertionError) at clojure_repl_experiments.1.10.error_messages$eval16251/invokeStatic (form-init1074562990802515819.clj:55).
;;   Assert failed: false
;; VS 1.9
;;   AssertionError Assert failed: false clojure_repl_experiments.1.10.error_messages$eval16251/invokeStatic (form-init1074562990802515819.clj:55).
 

