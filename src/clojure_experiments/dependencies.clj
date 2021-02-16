(ns clojure-experiments.dependencies
  (:import (clojure.lang Var)
           (java.lang.reflect Field
                              Modifier)))


;;; Analyze fn-var usages
;;; Similar to REBL
;;; https://metaredux.com/posts/2019/05/04/discovering-runtime-function-references-in-clojure.html
;;; This exact same thing is used in cider-nrepl/orchard: https://github.com/clojure-emacs/orchard/blob/master/src/orchard/xref.clj#L16


(defn fdeps
  "Returns the 'dependencies' of given fval (a dereferenced var representing a function).
  The dependencies are the clojure functions it calls."
  [fval]
  (set (some->> fval class .getDeclaredFields
                (keep (fn [^java.lang.reflect.Field f]
                        (or (and (identical? clojure.lang.Var (.getType f))
                                 (java.lang.reflect.Modifier/isPublic (.getModifiers f))
                                 (java.lang.reflect.Modifier/isStatic (.getModifiers f))
                                 (-> f .getName (.startsWith "const__"))
                                 (.get f fval))
                            nil))))))

(defn namespaces
  ;; ns-query is currently ignored
  [ns-query]
  (all-ns))

;; cider uses more elaborate version of this: https://github.com/clojure-emacs/orchard/blob/master/src/orchard/query.clj#L43
(defn all-vars
  "Returns a list of all currently loaded vars."
  [{:keys [ns-query private?] :as var-query}]
  ;; https://github.com/clojure-emacs/orchard/blob/master/src/orchard/query.clj#L68
  (let [ns-vars (if private? ns-interns ns-publics)
        nss (namespaces ns-query)]
    (mapcat (comp vals ns-vars) nss)))

(defn frefs
  "Returns functions which call the `fn-var`.
  Note: input arg must be a fn-var not a dereferenced value!"
  [fn-var]
  (let [all-vars (all-vars {:private true?})
        all-vals (map var-get all-vars)
        deps-map (zipmap all-vars (map fdeps all-vals))]
    (map first (filter (fn [[k v]] (contains? v fn-var)) deps-map))))

(require '[cider.nrepl.inlined-deps.orchard.v0v6v1.orchard.xref :as xref])
  ;; `as-var` copied from `xref` ns
(defn as-var
  "Convert `thing` to a var."
  [thing]
  (let [fn->sym (fn [f] (symbol (Compiler/demunge (.getName (type f)))))]
    (cond
      (var? thing) thing
      (symbol? thing) (find-var thing)
      (fn? thing) (find-var (fn->sym thing)))))
(defn call-graph
  "Creates a 'call graph' of given symbol representing a function.
    Uses `(xref/fn-refs sym)` to find all callers of the function recursively
    until there are no more 'parent's higher up in the stack or `max-depth` is reached.

    Preconditions and gotchas:
    - Only considers code that has been loaded:
      - if a caller hasn't been loaded yet it won't be found (often tests)
    - Only deals with Vars:
      - calls inside macros (such as compojure's GET), multimethods and protocol functions aren't recognized
    "
  ([sym]
   (call-graph 10 sym))
  ([max-depth sym]
   (call-graph xref/fn-refs max-depth sym))
  ([frefs-fn max-depth sym]
   (call-graph frefs-fn max-depth sym [sym]))
  ([frefs-fn max-depth  sym graph]
   (let [sym-var (as-var sym)]
     (if (and (pos? max-depth) (not-empty graph))
           ;; here we filter out references to the same var to exclude self-references
           ;; inside recursive and multi-arity functions
       (let [sym-refs (remove #(= sym-var %) (frefs-fn sym-var))]
         (concat graph (map #(call-graph frefs-fn (dec max-depth) %)
                            sym-refs)))
       graph))))

(comment

  (require '[clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements :as sicp])
  (call-graph sicp/square)
  ;; Tip: if your editor supports it, you can easily jump to the definition of the functions/vars
  ;; - e.g. in Spacemacs, just press `, g d`
;; => (#function[clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements/square]
;;     (#'clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations/magnitude-rectangular)
;;     (#'clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements/sum-of-squares
;;      (#'clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements/f))
;;     (#'clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements/good-enough?
;;      (#'clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements/sqrt-iter
;;       (#'clojure-experiments.books.sicp.ch1-abstractions-procedures.s1-elements/sqrt)))
;;     (#'clojure-experiments.books.sicp.ch2-abstractions-data.s4-multiple-representations/make-from-real-imag-polar))

  (defn foo []
    (map inc (range 10)))

  (fdeps foo)
  ;; => #{#'clojure.core/map #'clojure.core/inc #'clojure.core/range}

  (defn bar []
    (foo))

  (defn baz []
    (let [x (bar)]
      (conj 100 x)))
  ;; => #'user/bar

  (frefs #'foo)
  ;; => (#'user/baz #'user/bar)

  )
