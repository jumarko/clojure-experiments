(ns clojure-experiments.config
  "
  THIS IS JUST AN EXAMPLE OF A CONFIGURATION PROCESSING - NOT REALLY USED FOR ANYTHING USEFUL IN THIS PROJECT!

  Application configuration.

  See `config-test` for basic test cases and usage examples.

  Configuration is logged when the app is being started but passwords are filtered;

  The `resources/config.edn` file contains default values for all required configuration properties.
  User can override those values with system properties or environment variables.
  Alternatively, they can pass path to the custom edn file via system property: `-Dconf=my-config.edn`.

  Spec is used to define valid data for each configuration property.
  If the user fails to provide a valid value for any configuration property
  then the corresponding default value (as specified in config.edn) will be used.

  Don't forget to add a proper spec and a default value when you add a new configuration property!
  (`resources/config.edn`)
  All configurable properties must be in `config.edn` too (even if using nil as their default value)
  otherwise they won't be recognized by the `cprop` library
  and the user won't be able to override them via system props or env vars.
  This means that you should use `s/nilable` when you deal with optional config keys
  otherwise the user will get error log during the starup, e.g.:
    ERROR [clojure-experiments.config:144] - Config property '[:auth-session :encryption-key]' with invalid value nil of type nil replaced with default value nil

  Almost all config keys should be required with sensible default values.
  One exception is `:auth-session {:encryption-key...}` where the absence of `:encryption-key`
  means usage of in-memory session store (instead of persistent cookie store).

  If any required key is missing than runtime error is thrown (fail fast)
  and user has to fix the config.
  
  Note that cprop lib will automatically convert strings to appropriate data types.

  Following are the equivalents of `{:auth-session {:max-age 3600}}`
  - env variable: `AUTH_SESSION__MAX_AGE`
  - system property: `auth.session_max.age`

  Check cprop documentation for more information:
  - https://github.com/tolitius/cprop
  - http://www.luminusweb.net/docs/environment.md
  - https://github.com/tolitius/cprop#types"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [cprop.core :as prop]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [clojure.spec.gen.alpha :as gen]))

;;; Specs for application configuration properties
;;; See also config.edn

(s/def :config/port pos-int?)
(s/def :config/db-password string?)
(s/def :config/db-run-migrations boolean?)
(s/def :config/conn-timeout pos-int?)

;; encryption key has to be 16 bytes long
(def encryption-key-length 16)
(s/def :config/encryption-key (s/nilable
                          (s/with-gen (s/and string? #(= (count %) encryption-key-length))
                                      ;; see also `clojure.test.check.generators/string-ascii`
                                      ;; must be wrapped in thunk otherwise it fails when calling `gen/sample` et al.
                                      #(gen/fmap str/join
                                                 (gen/vector (gen/char-ascii) encryption-key-length)))))
(s/def :config/max-age-seconds pos-int?)
(s/def :config/auth-session (s/keys :req-un [:config/max-age-seconds]
                              ;; no encryption-key means using in-memory session store
                              :opt-un [:config/encryption-key]))

(s/def :config/spec
  (s/keys :req-un [:config/port
                   :config/db-password
                   :config/db-run-migrations
                   :config/conn-timeout
                   :config/auth-session
                   ]))

;; read default config values only from resource file to have a safe fallback
;; in case user supplies invalid values in system props or env variables
(def default-config (edn/read-string
                     (slurp (io/resource "config.edn"))))

(defn- get-required-key
  "Parses required key from the internals of spec problem reported via `explain-data`.
  Note: this is implementation specific and may change in future versions of spec
  (or there might be a better way how to get missing required keys).
  See `config-test/get-missing-required-keys`"
  [spec-problem]
  ;; let's parse required key name from the weird-looking predicate - sth. like:
  ;; `(clojure.core/fn [%] (clojure.core/contains? % :missing-required-key)`
  (when (seq? (:pred spec-problem))
    (let [[core-fn _arg [core-contains _map key-name]] (:pred spec-problem)
          key-path (vec (:path spec-problem))]
      ;; extra checks to make sure we don't end up using something that's not really
      ;; a failing predicate reported by missing map keys
      (when (and (= core-fn `fn)
                 (= core-contains `contains?))
        ;; must concatenate path - if it's a top-level map key, then path is empty,
        ;; otherwise it's a path to the missing key's parent
        (conj key-path key-name)))))

(defn get-missing-required-keys
  [spec-problems]
  (->> spec-problems
       (map get-required-key)
       (remove nil?)))

(defn- replace-with-default-values
  "Replace values of all given keys in `config` by values from `default-config`.
  Calls `key-reporter-fn` with 3 args: key path, original value, and default-value (replacing the original)
  for every key in `key-paths`."
  [key-reporter-fn default-config config key-paths]
  ;; this is a bit more complex than using straight `select-keys` but it handles nested keys/maps properly
  (reduce (fn [cfg key-path]
            (let [default-value (get-in default-config key-path)]
              (key-reporter-fn key-path (get-in config key-path) default-value)
              (assoc-in cfg key-path default-value)))
          config
          key-paths))

(defn- report-invalid-key [key-path original-value default-value]
  (log/errorf (str "Config property '%s' with invalid value %s of type %s replaced with default value %s ")
              key-path
              original-value
              (some-> original-value class .getSimpleName)
              default-value))

(defn- invalid-spec-key-path
  "This extract path to the configuration map key with invalid value from the path reported by spec
  which can be something like `[:auth-session :max-age-seconds]`
  but also [:file-root :clojure.spec.alpha/pred]"
  [spec-problem]
  (->> (:path spec-problem)
       (remove (fn [path-key]
                 (or
                  (some-> path-key keyword namespace
                          (str/starts-with? "clojure.spec"))
                  ;; to get rid of `s/or` related path when dealing with file paths
                  (= path-key :no-path)
                  (= path-key :potential-path))))
       (into [])))

(defn- fix-config
  "Fix provided config replacing invalid data with defaults
  as specified in config.edn resource file."
  [default-config config spec-problems]
  (let [;; keys present in the config but with invalid value 
        ;; a path can be sth. like [:auth-session :max-age-seconds]
        invalid-keys (->> spec-problems
                          (map invalid-spec-key-path)
                          ;; sometimes keys are repeated because multiple spec problems
                          ;; for a single key are reported; e.g. for an invalid file path you could get:
                          ;; ({:path [:file-root :clojure.spec.alpha/pred], :pred config/existing-file-path?, :val "/123", :via [:config/spec :config/file-root], :in [:file-root]}
                          ;; {:path [:file-root :clojure.spec.alpha/nil], :pred nil?, :val "/123", :via [:config/spec :config/file-root], :in [:file-root]})
                          distinct 
                          ;; empty path means missing required key -> handled in `check-and-fix-config` (fail fast)
                          (remove empty?))
        fixed-config (replace-with-default-values report-invalid-key default-config config invalid-keys)]
    fixed-config))

(defn check-and-fix-config
  "Checks if there are any fatal configuration errors and terminate early if so.
  Otherwise replace invalid values with the defaults (resources/config.edn).

  Note: this exploits spec error reporting and may rely on implementation details for required keys."
  ([bare-config]
   (check-and-fix-config bare-config :config-spec))
  ([bare-config config-spec]
   (let [spec-problems (:clojure.spec.alpha/problems (s/explain-data config-spec bare-config))]
     (when spec-problems
       (log/debug "Configurations spec problems: " (vec spec-problems)))
     (when-let [missing-required-keys (seq (get-missing-required-keys spec-problems))]
       ;; this shouldn't happen because all required keys should be set in default resources/config.edn
       ;; and from that picked up by cprop library
       (throw (ex-info
               (str "Missing required configuration key(s): " (str/join ", " missing-required-keys))
               {:missing-required-keys missing-required-keys})))
     (if spec-problems
       (fix-config default-config bare-config spec-problems)
       bare-config))))

(defn load-config
  ([] (load-config :config/spec))
  ;; 1-arity version is handy for testing -> see config_test.clj
  ([config-spec]
   (check-and-fix-config (prop/load-config) config-spec)))

(def
  ^{:doc "Complete application configuration as a map. See also config.edn file."}
  env (load-config))

(comment
  ;; example of config spec problem
  {:clojure.spec.alpha/problems
   '({:path [:conn-timeout], :pred pos-int?, :val "5000",
      :via [:config/spec :config/conn-timeout],
      :in [:license-check-conn-timeout]})})



;;; Filtering and logging useful config info during app startup
(defn- mask-passwords [env]
  (map (fn [[k v]]
         (if (.contains (name k) "password")
           [k "***"]
           [k v]))
       env))

(defn- filtered-configuration-properties [env]
  (->> env
        ;; don't print passwords in plaintext!
       mask-passwords
       (sort-by first)
       (into [])))

(defn- get-current-direct-memory-usage
  "Reports current direct memory usage via JMX.
  This isn't usually reported by tools like jcmd.
  See https://hub.jmonkeyengine.org/t/monitor-direct-memory-usage-in-your-app/25422"
  []
  (try 
    (let [mbeans (java.lang.management.ManagementFactory/getPlatformMBeanServer)
          direct-pool (javax.management.ObjectName. "java.nio:type=BufferPool,name=direct")]
      (.getAttribute mbeans direct-pool "MemoryUsed"))
    (catch Exception e
      (log/error e "Cannot get direct memory usage"))))

(defn- runtime-system-info []
  (let [runtime (Runtime/getRuntime)]
    [["available processors" (.availableProcessors runtime)]
     ["free memory" (.freeMemory runtime)]
     ["max memory" (.maxMemory runtime)]
     ["total memory" (.totalMemory runtime)]
     ["direct memory used" (get-current-direct-memory-usage)]]))

;; list of system properties carrying useful system-level information for diagnostics
;; See https://www.roseindia.net/java/beginners/OSInformation.shtml
(def env-system-properties
  ["os.name"
   "os.version"
   "os.arch"
   "java.version"
   "java.class.version"
   "java.vendor"
   "java.vm.name"
   "java.vm.vendor"
   "java.vm.info"
   "java.home"
   "java.io.tmpdir"
   "user.name"
   "user.dir"
   "user.timezone"
   "user.language"
   "file.encoding"
   "file.separator"
   "path.separator"
   "line.separator"
   ])

(defn- environment-info []
  (vec (concat
        (mapv (fn [prop-name] [prop-name (System/getProperty prop-name)])
              env-system-properties)
        (runtime-system-info)
        ["configuration properties" (filtered-configuration-properties env)])))

(defn log-environment []
  (log/info "Starting with environment: " (environment-info)))


(comment

  (log-environment)
  ;; 19-09-27 20:56:10 Jurajs-MacBook-Pro-3.local INFO [clojure-experiments.config:272] - Starting with environment:  [["os.name" "Mac OS X"]
  ;; ["os.version" "10.14.6"] ["os.arch" "x86_64"] ["java.version" "11.0.2"] ["java.class.version" "55.0"] ["java.vendor" "AdoptOpenJDK"]
  ;; ["java.vm.name" "OpenJDK 64-Bit Server VM"] ["java.vm.vendor" "AdoptOpenJDK"] ["java.vm.info" "mixed mode"]
  ;; ["java.home" "/Library/Java/JavaVirtualMachines/jdk-11.0.2+9/Contents/Home"]
  ;; ["java.io.tmpdir" "/var/folders/hn/tgwyrdmj1tb5pmmbdkd1g_qc0000gn/T/"] ["user.name" "jumar"]
  ;; ["user.dir" "/Users/jumar/workspace/clojure/clojure-experiments"] ["user.timezone" "Europe/Prague"]
  ;; ["user.language" "en"] ["file.encoding" "UTF-8"] ["file.separator" "/"] ["path.separator" ":"] ["line.separator" "\n"]
  ;; ["available processors" 12] ["free memory" 907406976] ["max memory" 8589934592] ["total memory" 1738539008]
  ;; ["direct memory used" 826641] "configuration properties" [[:auth-session {:encryption-key "abcdefghijklmnop", :max-age-seconds 1209600}]
  ;; [:conn-timeout 5000] [:db-password "***"] [:db-run-migrations true] [:port 3003]]]

  ;;
  )
