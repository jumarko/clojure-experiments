(ns clojure-experiments.config-test
  "See https://github.com/tolitius/cprop for property formats,
  especially system properties: https://github.com/tolitius/cprop#system-properties-cprop-syntax"
  (:require [clojure-experiments.config :as c]
            [clojure.spec.alpha :as s]
            ;; use test.check generators directly since spec doesn't add any value here
            ;; and just makes it hard to explore
            [clojure.test.check.generators :as gen]
            [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defmacro with-system-property
  "Executes body while given system property is set to given value.
  Clears property's value afterwards.
  Example:
    (with-system-property \"username\" \"john\"
      (System/getProperty \"username\"))
  -> expands to:
  ```
  (do
  (java.lang.System/setProperty \"username\" \"john\")
  (try
    (System/getProperty \"username\")
    (finally (java.lang.System/clearProperty \"username\"))))
  ```"
  [property value & body]
  `(do
     (System/setProperty ~property ~value)
     (try
       ~@body
       (finally
         (System/clearProperty ~property)))))

(defn- check-custom-config-value [property set-value expected-value]
  (with-system-property property set-value
    (is (= expected-value
           ;; notice that we need to call `load-config` to reload config based on the new system property
           (get-in (c/load-config)
                   ;; '.' in system properties is replaced by '-' in cprop lib
                   ;; '_' is the separator of nested properties
                   (mapv keyword (-> property
                                     (str/replace #"\." "-")
                                     (str/split #"_"))))))))

(deftest custom-valid-config
  (testing "override string property"
    (check-custom-config-value "db.password" "changeme" "changeme"))
  (testing "override boolean property"
    (check-custom-config-value "db.run.migrations" "true" true))
  (testing "override boolean property incorrectly -> will be false"
    (check-custom-config-value "db.run.migrations" "TRUE" false))
  (testing "override integer property"
    (check-custom-config-value "conn-timeout" "3333" 3333))
  (testing "nested integer propery"
    ;; default for max-age-seconds is 14 days
    (check-custom-config-value "auth.session_max.age.seconds" "3600" 3600)))

(deftest custom-invalid-config
  (testing "invalid integer property"
    (check-custom-config-value "port" "333x" 3003))
  (testing "invalid positive integer property - negative number"
    (check-custom-config-value "port" "-1000" 3003))
  (testing "empty required value"
    ;; note: cannot set system property to nil because that throws an exception
    (check-custom-config-value "db-password" "" "changeme"))
  (testing "empty required nested integer propery is replace with proper default value"
    (check-custom-config-value "auth.session_max.age.seconds" "" 1209600))
  (testing "invalid nested integer propery is replace with proper default value"
    (check-custom-config-value "auth.session_max.age.seconds" "xyz" 1209600)))


(defn- check-missing-keys-error-handling [config-spec expected-keys]
  (assert (seq expected-keys) "Must specify at least one expected key")
  (let [missing-keys-pattern (re-pattern (str "Missing required configuration key\\(s\\): .*"
                                              ;; need to replace brackets because they have special meaning in regex
                                              (-> expected-keys
                                                  (#(str/join ".*" %))
                                                  ;; replacing [ and ] because they have special meaning in regexes
                                                  (.replace "[" "")
                                                  (.replace "]" ""))
                                              ".*"))]
    (log/info "Expected missing keys pattern: " missing-keys-pattern)
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         missing-keys-pattern
         (c/load-config config-spec)))))

(s/def ::missing-key int?)
;; must use existing top-level property otherwise it's ignored by cprop
(s/def ::auth-session (s/keys :req-un [::missing-key]))
(deftest missing-required-config-keys
  (testing "Should fail when required key is missing"
    (check-missing-keys-error-handling
     (s/merge ::c/config-spec
              (s/keys :req-un [::missing-required-key]))
     [:missing-required-key]))
  (testing "Should fail when multiple required keys are missing"
    (check-missing-keys-error-handling
     (s/merge ::c/config-spec
              (s/keys :req-un [::missing-required-key ::another-missing-required-key]))
     [[:missing-required-key] [:another-missing-required-key]]) )
  (testing "Should fail when nested required key is missing"
    (with-system-property "auth.session_missing.key" "set"
      (check-missing-keys-error-handling (s/merge ::c/config-spec
                (s/keys :req-un [::auth-session]))
       [[:auth-session :missing-key]]))))


(deftest get-missing-required-keys
  (testing "No missing keys reported if predicate doesn't match"
    (is (empty? 
         (c/get-missing-required-keys
          '({:path [ :auth-session :max-age-seconds ],
             :pred clojure.core/pos-int?,
             :val nil,
             :via [ :config/config-spec :config/auth-session :config/max-age-seconds ],
             :in [ :auth-session :max-age-seconds ] } )))))
  (testing "Missing required key reported"
    ;; result is a nested vector, because key paths can be nested, e.g. [[:top-level-key] [:a :nested-key]]
    (is (= [[:biomarkers-monitor-max-items-per-screen]]
           (c/get-missing-required-keys
            '({:path [],
               :pred (clojure.core/fn [%] (clojure.core/contains? % :biomarkers-monitor-max-items-per-screen)),
               :val {:google-analytics-tracking-id nil },
               :via [:config/config-spec], :in []})))))
  (testing "Nested required key reported"
    ;; result is a nested vector, because key paths can be nested, e.g. [[:top-level-key] [:a :nested-key]]
    (is (= [[:auth-session :max-age-seconds]]
           (c/get-missing-required-keys
            '({:path [:auth-session],
               :pred (clojure.core/fn [%] (clojure.core/contains? % :max-age-seconds)),
               :val {:encryption-key "12344567890abcdef"},
               :via [:config/config-spec :config/auth-session], :in [:auth-session]})))))
  (testing "Nothing is reported if predicate form doesn't match closely"
    (is (= []
           (c/get-missing-required-keys
            '({:path [:auth-session],
               :pred (clojure.core/fn [%] (clojure.core/get % :max-age-seconds)),
               :val {:encryption-key "12344567890abcdef"},
               :via [:config/config-spec :config/auth-session], :in [:auth-session]}))))))


;; This won't work with file paths that we require to exist.
(deftest generative-tests
  (testing "generate config and check"
    (log/info "Running generative tests for config")
    (time (let [test-count 100]
            ;; This would throw an exeption if there's something deeply wrong with the config
            (doseq [config (gen/sample (s/gen ::c/config-spec)
                                       test-count)]
              (c/check-and-fix-config config))))
    (log/info "Finished generative tests for config")))

