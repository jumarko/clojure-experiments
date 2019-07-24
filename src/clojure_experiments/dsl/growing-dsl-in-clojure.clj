(ns clojure-experiments.dsl.growing-dsl-in-clojure
  "This is an example of implementin simple DSL fro translating
  Clojure to a shell/batch script: https://pragprog.com/magazines/2011-07/growing-a-dsl-with-clojure.
  Simplified version of https://github.com/pallet/stevedore."
  (:require [pallet.script :as script]))


;;; We want something like this:
;; (if (= 1 2)
;;   (println "a")
;;   (println "b"))
;;; to be transformed to:
  ;; if [1 -eq 2 ]; then
  ;;   echo "a"
  ;; else
  ;;   echo "b"
  ;; fi


;; So we need to somehow "emit" bash form:
(defn emit-bash-form [clojure-form]
  (condp = (class clojure-form)
    String clojure-form
    ;; notice that we use `Number` to cover `Long` and other types too
    Long (str clojure-form)
    ))

(comment

  (emit-bash-form 1)

  )
