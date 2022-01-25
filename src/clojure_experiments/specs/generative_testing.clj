(ns clojure-experiments.specs.generative-testing
    (:require
    [clojure.spec.alpha      :as s]
    [clojure.spec.test.alpha :as stest]
    [clojure.string          :as str]
    [clojure.test            :as test]
    [expound.alpha           :as expound]))

;;; Some helpers
(defn report-results [check-results]
  (let [checks-passed? (->> check-results (map :failure) (every? nil?))]
    (if checks-passed?
      (test/do-report {:type    :pass
                       :message (str "Generative tests pass for "
                                     (str/join ", " (map :sym check-results)))})
      (doseq [failed-check (filter :failure check-results)]
        (let [r       (stest/abbrev-result failed-check)
              failure (:failure r)]
          (test/do-report
           {:type     :fail
            :message  (binding [s/*explain-out* (expound/custom-printer {:theme :figwheel-theme})]
                        (expound/explain-results-str check-results))
            :expected (->> r :spec rest (apply hash-map) :ret)
            :actual   (if (instance? Throwable failure)
                        failure
                        (::stest/val failure))}))))
    checks-passed?))

(defmacro defspec-test
  ([name sym-or-syms] `(defspec-test ~name ~sym-or-syms nil))
  ([name sym-or-syms opts]
   (when test/*load-tests*
     `(defn ~(vary-meta name assoc :test
                        `(fn [] (report-results (stest/check ~sym-or-syms ~opts))))
        [] (test/test-var (var ~name))))))

