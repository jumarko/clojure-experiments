(ns clojure-experiments.performance.boxed-math
  (:require [clj-java-decompiler.core :refer [decompile disassemble] :as decompiler]))

;;; question on CLojurians slack

;; https://insideclojure.org/2014/12/15/warn-on-boxed/
(set! *unchecked-math* :warn-on-boxed)
(defn my-add-long
  [a  b]
  (+ a b))
;; Boxed math warning, /Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/performance/performance.clj:280:3 - call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(java.lang.Object,java.lang.Object).


;; this doesn't work as expected, at least not when running in the REPL with deps.edn
(comment
  *compile-path*
  (compile 'clojure-experiments.performance.boxed-math)
  ,)

;; but no warning when using proper type hints
(defn my-add-long
  [^long a ^long b]
  (+ a b))
(my-add-long 2 3)
;; => 5

(comment
  (decompile (defn my-add-long
     [^long a ^long b]
     (+ 1 a b)))
  ;; // Decompiling class: clojure_experiments/performance/performance$my_add_long
  ;; package clojure_experiments.performance;

  ;; import clojure.lang.*;

  ;; public final class performance$my_add_long extends AFunction implements LLO
  ;; {
  ;;  public static Object invokeStatic(final long a, final long y) {
  ;;                                                                 return Numbers.num(Numbers.add(Numbers.add(1L, a), y));
  ;;                                                                 }
   
  ;;  @Override
  ;;  public Object invoke(final Object o, final Object o2) {
  ;;                                                         return invokeStatic(RT.longCast(o), RT.longCast(o2));
  ;;                                                         }
   
  ;;  @Override
  ;;  public final Object invokePrim(final long a, final long n) {
  ;;                                                              return invokeStatic(a, n);
  ;;                                                              }
  ;;  }

  (decompile (fn [] (my-add-long 2 3)))
  ;; // Decompiling class: clojure_experiments/performance/performance$fn__29007
  ;; package clojure_experiments.performance;

  ;; import clojure.lang.*;

  ;; public final class performance$fn__29007 extends AFunction
  ;; {
  ;;  public static final Var const__0;
   
  ;;  public static Object invokeStatic() {
  ;;                                       return ((LLO)performance$fn__29007.const__0.getRawRoot()).invokePrim(2L, 3L);
  ;;                                       }
   
  ;;  @Override
  ;;  public Object invoke() {
  ;;                          return invokeStatic();
  ;;                          }
   
  ;;  static {
  ;;          const__0 = RT.var("clojure-experiments.performance.performance", "my-add-long");
  ;;          }
  ;;  }



  )

