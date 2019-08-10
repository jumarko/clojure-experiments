(ns clojure-experiments.lisp-in-clojure.core)

(defn eval-sexp [expr env]
  (cond
    (symbol? expr)
    (env expr)

    (list?)))


;;; Russ Olsen' eval implementation from the book Getting Clojure
;;; see also repl.clj
;;; see also https://github.com/kanaka/mal
(defn eval-symbol
  "Just lookup symbols in current namespace."
  [expr]
  (.get (ns-resolve *ns* expr)))

(defn eval-vector
  "Recursively evaluates vector's content."
  [expr]
  (vec (map reval expr)))

(defn eval-list
  "Recursively evaluates the content of the list and call it as a function."
  [expr]
  (let [evaled-items (map reval expr)
        f (first evaled-items)
        args (rest evaled-items)]
    (apply f args)))

(defn reval [expr]
  (cond
    (string? expr) expr
    (keyword? expr) expr
    (number? expr) expr
    (symbol? expr) (eval-symbol expr)
    (vector? expr) (eval-vector expr)
    (list? expr) (eval-list expr)
    :else :completely-confused))
