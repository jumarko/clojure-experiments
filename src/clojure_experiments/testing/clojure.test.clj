(ns clojure-experiments.testing.clojure.test
  (:require  [clojure.test :refer [deftest is testing assert-expr do-report]]
             [clojure-experiments.testing.ui-tests :as uit]))

;;; You can provide custom implementation for `assert-expr` to improve `is` macro reporting

(defn has-milk? [x]
  (int? (:milk x)))

(defmethod assert-expr 'has-milk? [msg form]
  ;; Test if x is an instance of y.
  `(let [with-milk# ~(nth form 1)]
     (let [result# (has-milk? with-milk#)]
       (if result#
         (do-report {:type :pass, :message ~msg,
                     :expected '~form, :actual (:milk with-milk#)})
         (do-report {:type :fail, :message ~msg,
                     :expected ":milk key with integral value" :actual (:milk with-milk#)}))
       result#)))

(deftest assert-expr-test
  (testing "Supported by default"
    (is (instance? Float 1)))
  (testing "Not nice with default implementation"
    (is (has-milk? {:milk "yes!"})))
  ;; will print:
  ;; 
  ;; expected: ":milk key with integral value"          
  ;; actual: "yes!"          
  )

