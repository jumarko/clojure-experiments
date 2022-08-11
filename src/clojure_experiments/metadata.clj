(ns clojure-experiments.metadata
  "Playing with metadata in Clojure.")


;;; get the meta information of functions from another namespace.
;;; https://clojurians.slack.com/archives/C053AK3F9/p1660201303219879
(defn meta-info [namespaces]
  (let [meta-information (for [namespace namespaces
                               :let [all-functions (vals (ns-publics namespace))]]
                           (for [fn-var all-functions]
                             (meta fn-var)))]
    (prn "meta-info!" meta-information)
    meta-information))
;; this is a bit meta :) - call meta-info on this namespace
(meta-info [*ns*])
;; => (({:arglists ([namespaces]),
;;       :line 7,
;;       :column 1,
;;       :file "/Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/metadata.clj",
;;       :name meta-info,
;;       :ns #namespace[clojure-experiments.metadata]}))
