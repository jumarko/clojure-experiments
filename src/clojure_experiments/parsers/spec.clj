(ns clojure-experiments.parsers.spec
  "Experiment with 'static' analysis of function forms leveraging fspec-s.")

;;;===============================================
;;; TODO
;;; experiment checking of specs for all applicable function calls inside a ns
;;; via tools.analyzer.jvm
;;; - https://github.com/gfredericks/clj-usage-graph/blob/master/src/com/gfredericks/clj_usage_graph/usages.clj#L8
;;; - AST QuickRef: http://clojure.github.io/tools.analyzer.jvm/spec/quickref.html#instance-call
(require '[clojure.tools.analyzer.jvm :as a]
         '[clojure.tools.analyzer.ast :refer [nodes]])

(defn instrumentable-functions
  "Enumerates all given namespaces and returns symbols representing all functions
  that are instrumentable as per `stest/instrumentable-syms`."
  [ns-syms]
  (let [instrumentable-syms (st/instrumentable-syms)]
    (->> ns-syms
         (st/enumerate-namespace)
         (filterv #(contains? instrumentable-syms %))
         set)))

;;; utility functions for handy spec checking

(s/fdef check-spec
  :args (s/cat :fn ifn? :args vector?))
(defn check-fspec
  "Given an instrumentable function symbol and vector of args
  checks that arguments conform to the function's :args spec.
  Returns a vector where first element is fn symbol and second result of `s/explain-data`.

  Note: this is a low-level function -> prefer `check-fspecs` instead."
  [instrumentable-fn args]
  (let [fn-spec (s/get-spec instrumentable-fn)
        fn-args-spec (:args fn-spec)]
    [instrumentable-fn (s/explain-data fn-args-spec args)]))


(defn- instrumentable-fn? [instrumentable-fns fn-form]
  (let [fn-symbol (some-> fn-form :fn :var symbol)]
    (contains? (set instrumentable-fns) fn-symbol)))

(defn- fns-names-and-args [form]
  (let [fn-usages (->> (nodes form)
                    (filter #(= :invoke (:op %)))
                    (map (fn [ast-node]
                           (select-keys ast-node [:fn :args]))))]
    ;; for each arg `:op` can be :local, :var, or :binding
    ;; out of those, we can probably only use vars for spec validation,
    ;; all other symbols are basically unknown
    ;; => how could we do that??
    fn-usages))

;; TODO write spec when the code is ready
;; (s/def ::fn (s/keys :req-un [::var]))
;; (s/def ::args (s/keys :req-un [::var]))
;; (s/fdef parse-ns
;;   :args (s/cat :ns-sym symbol?)
;;   :ret (s/coll-of (s/keys :req-un [::fn ::args])))
(defn parse-ns
  [ns-sym]
  (let [instrumentable-fns-symbols (instrumentable-functions ns-sym)
        ;; get all function calls made anywhere in the namespace code
        ns-invoke-forms (mapcat fns-names-and-args (ana/analyze-ns ns-sym))
        instrumentable-invocations (filter (partial instrumentable-fn? instrumentable-fns-symbols)
                                            ns-invoke-forms)]
    instrumentable-invocations
    ))
          

(comment

  (def instrumentable-invocations (parse-ns 'clojure-experiments.aws.access-logs))

  (def my-instrumentable-fns (instrumentable-functions 'clojure-experiments.aws.access-logs))
  (def my-ns-analyzed (time (ana/analyze-ns 'clojure-experiments.aws.access-logs)))

  ;; here we get all of the invocations - e.g. for `parse-access-log` we get `vec`, `remove`, `mapv`:
  ;; (defn parse-access-log [access-log-lines]
  ;;   (->> access-log-lines
  ;;        (mapv parse-access-log-line)
  ;;        (remove nil?)
  ;;        vec))
  (def parse-access-log-nodes (-> (nth my-ns-analyzed 11) fns-names-and-args))

  (def all-ns-nodes (mapcat fns-names-and-args
                            my-ns-analyzed))

  (map (comp :form :fn) all-ns-nodes)

  (nth all-ns-nodes 56)

  
  (def matching-fn-symbols (filterv instrumentable-fn?
                                   all-ns-nodes))


  )

(defn check-fspecs
  [ns-syms]
  (let [instrumentable-fns (instrumentable-functions ns-syms)]
    (mapv #(check-fspec % []) instrumentable-fns)))

(comment

  (def my-ns 'cacsremoteservice.features.vcs.remote-cloner)
  (instrumentable-functions my-ns)
  (check-fspecs )


  
  ;;end
  )


