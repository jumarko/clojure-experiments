(ns clojure-experiments.logging-and-monitoring.logging
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as log-impl]
            [taoensso.timbre :as log-timbre])
  (:import (org.apache.logging.log4j Level LogManager)
           (org.apache.logging.log4j.core.config Configurator)))

;;;; clojure.tools.logging and dynamic log level setting
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn log-levels []
  (->> (Level/values)
       seq
       (map (fn [log-level]
              [(-> log-level str clojure.string/lower-case keyword)
               log-level]))
       (into {}))
  #_{:fatal Level/FATAL
     :error Level/ERROR
     :warn Level/WARN
     :info Level/INFO
     :debug Level/DEBUG
     :trace Level/TRACE
     :off Level/OFF
     :all Level/ALL}
  )

(defn to-log4j-level [level-keyword]
  (get (log-levels) level-keyword))

;; copied from `clojure.tools.logging.impl/find-factory` and making `log4j2-factory
;; highest priority (rather than cl-factory and slf4j-factory - commons-logging e.g. is included in apache http client)
(defn find-factory
  "Returns the first non-nil value from slf4j-factory, cl-factory,
   log4j2-factory, log4j-factory, and jul-factory."
  []
  (or (log-impl/log4j2-factory)
      (log-impl/slf4j-factory)
      (log-impl/cl-factory)
      (log-impl/log4j-factory)
      (log-impl/jul-factory)
      (throw ; this should never happen in 1.5+
       (RuntimeException.
        "Valid logging implementation could not be found."))))

(s/def ::log-level (set (keys (log-levels))))
(s/fdef set-level!
  :args (s/cat :level ::log-level)
  :ret nil?)
(defn set-level!
  "Sets new log level for the Root logger. "
  [level]
  (if-let [log-level (to-log4j-level level)]
    ;; How do I set a logger's level programmaticaly?  https://logging.apache.org/log4j/log4j-2.4/faq.html#reconfig_level_from_code
    (do
      (Configurator/setRootLevel log-level)
      ;; finally, we need to update logger-factory used internally by clojure.tools.logging
      ;; otherwise it would cache the log level set when it was initialized
      (alter-var-root #'log/*logger-factory* (constantly  (find-factory))))
    (throw (IllegalArgumentException. (str "Invalid log level: " level)))))


(comment

  (Configurator/setRootLevel (to-log4j-level :debug))
  ;; alternatively you can try `setAllLevels`
  ;; (Configurator/setAllLevels LogManager/ROOT_LOGGER_NAME (to-log4j-level :info))

  (do 
    (set-level! :info)
    (log/info "PRINTED.")

    (log/debug "NOT PRINTED")

    (set-level! :debug)
    (log/debug "PRINTED?")

    (set-level! :info)
    (log/debug "NOT PRINTED")

    (set-level! :warn)
    (log/info "INFO NOT PRINTED"))


  ;; check
  (log-impl/get-logger log/*logger-factory* *ns*)
  (log-impl/get-logger (find-factory) *ns*)

  ;;
  )


;;;; Timbre
;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Masking secrets in logs
;;; 
(def secrets-log-patterns
  "All patterns matching potentially sensitive data in logs that should be replaced by `secret-replacement-str`.

  These patterns should always follow the format '(Whatever is the matching prefix) MYSCRET'.
  - note that the parens are required!
  This will match the log message 'Whatever is the matching prefix MYSCRET'
  and that message will be replaced with 'Whatever is the matching prefix ***'"
  [#"(Bearer )\S+" ;; OAuth access tokens sent in the Authorization header
   #"([:\"](?:access_token|access-token|refresh_token|refresh-token)(?:\":)? \")[^\"]+" ;; OAuth access/refresh tokens as used internally in Clojure maps or returned by OAuth provider "access_token" API
   #"(client_secret=)[^\s&]+" ;; OAuth app client secret sent in the request form body param
   #"(:client-secret \")[^\"]+" ;; OAuth app client secret as stored in a Clojure map
   ])

(def secret-replacement "***")

(defn- mask-secrets [secrets-patterns replacement log-message]
  ((reduce
    (fn mask-pattern [msg pattern]
      ;; the original prefix ($1) plus the secret replaced with '***'
      (clojure.string/replace msg pattern (str "$1" secret-replacement)))
    log-message
    secrets-patterns)))

(def mask-secrets-in-log (partial mask-secrets secrets-log-patterns secret-replacement))

(comment
  (def my-msg 
    "2019-06-12 08:41:34.115 Jurajs-MacBook-Pro-2.local DEBUG [org.apache.http.wire:73] - http-outgoing-30 >> \"Authorization: Bearer 882a64a1234567890abcdefghij1234567890dcb[\r][\n]\"")


  (mask-secrets-in-log my-msg)


  ;; criterium reports ~5 microseconds per `mask-secrets` call
  (crit/quick-bench (mask-secrets-in-log my-msg))
  )

(defn mask-secrets-log-config
  "Mask sensitive data in the logs - e.g. OAuth client secrets and access tokens.
  See https://github.com/ptaoussanis/timbre/issues/3 for an old example of masking middleware.
  However that is complex and not easy to do (one would have to 'mask' everything in the `:vargs` vector)
  so the easier option using the `:output-fn` was chosen."
  []
  (log-timbre/merge-config!
   {:output-fn (comp
                mask-secrets-in-log
                log-timbre/default-output-fn)}))

(comment
  ;; Compare
  (log-timbre/info "Bearer XYZ")
  
  (mask-secrets-log-config)
  (log-timbre/info "Bearer XYZ")
  )


;;; Timbre doesn't work with internal exception classes like `SunCertPathBuilderException`
;;; https://github.com/ptaoussanis/timbre/issues/365
;;; NOTE: clojure.tools.logging works just fine
(comment 
  ;; This throws
  ;; 1. Unhandled java.lang.IllegalAccessException
  ;; class clojure.core$bean$fn__7278$fn__7279 cannot access class
  ;; sun.security.validator.ValidatorException (in module java.base) because module java.base does not
  ;; export sun.security.validator to unnamed module @ac279a9
  (try
    (slurp "https://untrusted-root.badssl.com/")
    (catch Exception e
      (taoensso.timbre/error e "ssl error")))

  ;; clojure.tools.logging works
  (try
    (slurp "https://untrusted-root.badssl.com/")
    (catch Exception e
      (log/error e "ssl error")))

  )

