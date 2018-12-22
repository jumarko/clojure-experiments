(ns clojure-experiments.1.10.symbol)

;;; symbol - able to get namespaced symbol from Var now:
;;; https://github.com/clojure/clojure/blob/master/changes.md#34-other-enhancements
;;; > schmee speaking of, how does one go from a var to the namespaced symbol of the var?
;;; > alexmiller well, as of latest 1.10, with `symbol` :)
;;; > but otherwise you have to reach into the private fields of clojure.lang.Var
;;; ----------------------------------------------------------------------------

(symbol #'prn)
;; => clojure.core/prn

;; previously (in Clojure 1.9) you'd get following error
;; 1. Unhandled java.lang.ClassCastException
;;    clojure.lang.Var cannot be cast to java.lang.String
;;                   core.clj:  579  clojure.core/symbol
;;                   core.clj:  574  clojure.core/symbol

(symbol :hello)
;; => hello
