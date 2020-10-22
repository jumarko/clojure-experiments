(ns clojure-experiments.books.joy-of-clojure.ch08-macros
  (:require [clojure.walk :as walk]))

;;; eval (p. 175)

(eval 42)
;; => 42

(eval '(list 1 2))
;; => (1 2)

;; This will fail with ClassCastException
#_(eval (list 1 2))


;; Now something more exciting - evaluate a form that is a function call
(eval (list (symbol "+") 1 2))
;; => 3
;; it may help to see that this returns the list
(list (symbol "+") 1 2)
;; => (+ 1 2)
;; vs. just using this which is basically calling `(eval 3)`
(eval (+ 1 2))
;; => 3


;; contextual-eval -> local bindings
;; to mitigate issues with `eval` which uses global bindings
(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `'~v])
                    ctx)]
      ~expr)))
;; here the let bindings will be: [a (quote 1) b (quote 2)]
(contextual-eval '{a 1 b 2} '(+ a b))
;; => 3


;;; Control structures (p. 178)

;; do-until macro
(defmacro do-until
  [& clauses]
  (when clauses
    ;; Don't be confused: this `when` will get macroexpanded to `if` via `macroexpand-all`
    (list 'clojure.core/when (first clauses)
          ;; ... and this `if` will disappear because it's evaluated when the macro executes at compile time
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException. "do-until requires an even number of forms")))
          (cons 'do-until (nnext clauses)))))

(macroexpand-1 '(do-until true (prn 1) false (prn 2)))
;; => (clojure.core/when true (prn 1) (do-until false (prn 2)))

;; just for fun you can try decompile to Java code
(require '[clj-java-decompiler.core :refer [decompile disassemble] :as decompiler])
#_(decompile (do-until true (prn 1) false (prn 2)))

(walk/macroexpand-all '(do-until true (prn 1) false (prn 2)))
;; => (if true (do (prn 1) (if false (do (prn 2) nil))))

(do-until
 (even? 2) (println "Even")
 (odd? 3) (println "Odd")
 (zero? 1) (println "You'll never see me")
 :lollipop (println "Truthy thing"))
;; Even
;; Odd
;; => nil

(macroexpand-1 
 '(do-until
   (even? 2) (println "Even")
   (odd? 3) (println "Odd")
   (zero? 1) (println "You'll never see me")
   :lollipop (println "Truthy thing")))
;; => (clojure.core/when (even? 2) (println "Even") (do-until (odd? 3) (println "Odd") (zero? 1) (println "You'll never see me") :lollipop (println "Truthy thing")))

(walk/macroexpand-all
 '(do-until
   (even? 2) (println "Even")
   (odd? 3) (println "Odd")
   (zero? 1) (println "You'll never see me")
   :lollipop (println "Truthy thing")))
;; => (if (even? 2) (do (println "Even") (if (odd? 3) (do (println "Odd") (if (zero? 1) (do (println "You'll never see me") (if :lollipop (do (println "Truthy thing") nil))))))))


