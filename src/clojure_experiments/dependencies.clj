(ns clojure-experiments.dependencies
  (:import (clojure.lang Var)
           (java.lang.reflect Field
                              Modifier)))


;;; Analyze fn-var usages
;;; Similar to REBL
;;; https://metaredux.com/posts/2019/05/04/discovering-runtime-function-references-in-clojure.html
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

(comment
  
  (defn foo []
    (map inc (range 10)))

  (fdeps foo)
  ;; => #{#'clojure.core/map #'clojure.core/inc #'clojure.core/range}

  (defn bar []
    (foo))

  (defn baz []
    (let [x (foo)]
      (conj 100 x)))
  ;; => #'user/bar

  (frefs #'foo)
  ;; => (#'user/baz #'user/bar)

  ;; end
  )
