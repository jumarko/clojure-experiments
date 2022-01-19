(ns clojure-experiments.linters.clj-kondo
  "Experiments with the `clj-kondo` linter: https://github.com/borkdude/clj-kondo"
  (:require [clj-kondo.core :as clj-kondo]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

;;; Doesn't work properly with namespaced maps
(defn my-fn [db-spec id config]
  (println "Saving into db")
  (println "DONE."))
;; => reports 'Wrong number of args (4) ...'
(comment
  (my-fn
   {}
   1
   #:my-app{:timeout-ms 10000})
  ,)


;;; Find 'illegal' package usages
;;; This is structural test to check that no namespaces outside given package depends on anything inside it.
;;; This implementation uses clj-kondo [analysis data](https://github.com/clj-kondo/clj-kondo/tree/master/analysis#data).
;;; See also https://github.com/clj-kondo/clj-kondo/blob/master/doc/jvm.md#api"

;; Tip from borkdude to make this much faster
;;   You can also use tools.namespace directly if you're only interested in namespace dependencies.
;;   That will be much faster since it only reads the top level ns form
;; Something like: `(clojure.tools.namespace.find/find-ns-decls-in-dir (clojure.java.io/file "src"))`
(defn- analyse-sources!
  "Analysis given source directories (a sequence) with clj-kondo
    returning the :analysis key from the output."
  [source-dirs]
  (let [{:keys [analysis] :as _kondo-result}
        ;; this can take about 4 seconds
        (time (clj-kondo/run! {:lint source-dirs
                               :config {:output {:analysis true}}}))]
    analysis))

(defn- package-usages
  [{:keys [namespace-usages] :as _kondo-analysis}
   package-sym
   allowed-usages]
  (let [actual-usages (->> namespace-usages
                          ;; filter only usages of given package
                          (filter #(str/starts-with? (name (:to %))
                                                     (name package-sym)))
                          ;; remove usages that come from within the package
                          (remove #(str/starts-with? (name (:from %))
                                                     (name package-sym)))
                          (map #(select-keys % [:from :to :row :col])))
        forbidden-usages (remove (fn [{:keys [from to]}]
                                   (contains? (set (get allowed-usages from))
                                              to))
                                actual-usages)]
    {:actual-usages actual-usages
     :forbidden-usages forbidden-usages
     :allowed-usages allowed-usages}))


;; this test will likely take 4+ seconds - see `analyse-sources!`
;; It's supposed to fail with this message:
;;     There are namespaces that depend on the package but are not allowed to do so
;;     expected: (empty? forbidden-usages)
;;     actual: (not (empty? ({:from clojure-experiments.four-clojure.086-happy-numbers, :to clojure-experiments.purely-functional.puzzles.util, :row 6, :col 14})))
(deftest ^:slow ns-usages
  (let [analysis-result (analyse-sources! ["src"])]
    (testing "No namespaces should depend on our package"
      (let [whitelisted '{}
            {:keys [forbidden-usages]} (package-usages analysis-result
                                                       'clojure-experiments.purely-functional
                                                       whitelisted)]
        (is (empty? forbidden-usages)
            "There are namespaces that depend on the package but are not allowed to do so")))))


