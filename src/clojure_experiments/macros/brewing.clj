(ns clojure-experiments.macros.brewing
  "Examples from https://www.abhinavomprakash.com/posts/macrobrew/")


(defn identity-fn [x]
  (println "finding the identity of x")
  x)


(defmacro identity-macro [x] 
  (println "finding the identity of x")
  x)

(macroexpand-1 '(identity-macro (println "I return nil")))


(def black-list #{(symbol "java") (symbol "is") (symbol "evil")})

(defmacro special-defn [name args body]
  (if-not (contains? black-list name)
    '(defn name args body)
    "you can't define this function"))

(comment
  (macroexpand '(special-defn coffee [a] a))
  ;; Syntax error macroexpanding clojure.core/defn at (src/clojure_experiments/macros/brewing.clj:26:3).
  ;; args - failed: vector? at: [:fn-tail :arity-1 :params] spec: :clojure.core.specs.alpha/param-list
  ;; args - failed: (or (nil? %) (sequential? %)) at: [:fn-tail :arity-n :bodies] spec: :clojure.core.specs.alpha/params+body

  (macroexpand-1 '(special-defn coffee [a] a))
;; => (defn name args body)

  ,)


(defmacro print-els [coll]
  `(do ~(map println coll)
       ~coll)) 
#_(print-els [1 2 3])
;; 1
;; 2
;; 3
;; Syntax error (IllegalArgumentException) compiling at (src/clojure_experiments/macros/brewing.clj:39:1).
;; Can't call nil, form: (nil nil nil)
(macroexpand-1 '(print-els [1 2 3]))
;; => (do (nil nil nil) [1 2 3])
(print-els [1 2 3])

;; try my version
(defmacro print-els [coll]
  (doall (map println coll))
  `~coll)

(print-els [1 2 3]);; => [1 2 3]

(defmacro print-els [coll]
  `(do ~@(map println coll)
       ~coll)) 
(print-els [1 2 3])
;; => [1 2 3]
