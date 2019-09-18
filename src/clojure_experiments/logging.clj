(ns clojure-experiments.logging
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as logging]
            [taoensso.timbre :as log])
  (:import (org.apache.logging.log4j Level LogManager)
           (org.apache.logging.log4j Level)))

;;;; clojure.tools.logging
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; TODO: this whole thing requires proper configuration first!
;;; perhaps there's some conflict with SLF4j?

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
(s/def ::log-level (set (keys (log-levels))))
(s/fdef set-level!
  :args (s/cat :level ::log-level)
  :ret nil?)
(defn set-level!
  "Sets new log level for the Root logger. "
  [level]
  (if-let [log-level (get (log-levels) level)]
    ;; Using log4j2 API for setting log level: https://stackoverflow.com/a/23434603/1184752
    (let [logger-context (LogManager/getContext false)
          logger-config  (-> logger-context
                             .getConfiguration
                             (.getLoggerConfig LogManager/ROOT_LOGGER_NAME))]
      (.setLevel logger-config log-level)
      ;; This causes all Loggers to refetch information from their LoggerConfig.
      (.updateLoggers logger-context)
      ;; finally, we need to update logger-factory used internally by clojure.tools.logging
      ;; otherwise it would cache the log level set when it was initialized
      (alter-var-root #'logging/*logger-factory* (fn [_] (clojure.tools.logging.impl/find-factory))))
    (throw (IllegalArgumentException. (str "Invalid log level: " level)))))

(comment

  ;; nothing should be printed
  (logging/debug "ahoj")
  ;; this should be printed
  (logging/error "ahoj")

  ;; and now it should
  ;; BUT it's not (there's some problem with SLF4J bridge?; when clojure.tools.logging doesn't autodetect
  ;; any slf4j dependencies and picks log4j2 instead it works)
  (set-level! :debug)
  (logging/debug "ahoj")

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
  (log/merge-config!
   {:output-fn (comp
                mask-secrets-in-log
                log/default-output-fn)}))

(comment
  ;; Compare
  (log/info "Bearer XYZ")
  
  (mask-secrets-log-config)
  (log/info "Bearer XYZ")
  )

