(ns clojure-experiments.logging
  (:require [taoensso.timbre :as log]))

;;; Masking secrets in logs
;;; 
(def secrets-log-patterns
  "All patterns matching potentially sensitive data in logs that should be replaced by `secret-replacement-str`.


  These patterns should alway follow the format '(Whatever is the matching prefix) MYSCRET'.
  - note that the parens are required!
  This will match the log message 'Whatever is the matching prefix MYSCRET'
  and that message will be replaced with 'Whatever is the matching prefix ***'"
  [#"(Bearer )\w+" ;; OAuth access tokens sent in the Authorization header
   #"(client_secret=)\w+" ;; OAuth app client secret sent as the request form body param
   ])

(def secret-replacement "***")

(defn- mask-secrets [secrets-patterns replacement log-message]
  (reduce
   (fn mask-pattern [msg pattern]
     ;; the original prefix ($1) plus the secret replaced with '***'
     (clojure.string/replace msg pattern (str "$1" secret-replacement)))
   log-message
   secrets-patterns))

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
