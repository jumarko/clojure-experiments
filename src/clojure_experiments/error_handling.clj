(ns clojure-experiments.error-handling
  "Various error handling approaches. Exceptions, explicit error codes, monads, etc."
  (:require
   [taoensso.timbre :as log]
   [slingshot.slingshot :as sling]
   [clj-commons.slingshot :as sling-common]))

;;; Promenade: https://github.com/kumarshantanu/promenade
;;; Elegant Error handling for a More Civilized Age: Varun Sharma: https://www.youtube.com/watch?v=FsyPQG_IuaY


;;; Slingshot
(sling/try+
  (log/infof "%s")
  (catch Object _
    nil))

(try
  (log/infof "%s")
  (catch Exception e
    nil))

(sling-common/try+
  (log/infof "%s")
  (catch Object _
    nil))


(try
  (log/infof "Find ongoing batch jobs - SLOW: %t msecs.")
  (catch java.lang.Throwable &throw-context
    (let [&throw-context (-> &throw-context
                             clj-commons.slingshot.support/get-context
                             clj-commons.slingshot.support/*catch-hook*)]
      (:object &throw-context)
      #_(cond
        (instance? java.lang.Exception (:object &throw-context))
        (let [^Exception e (:object &throw-context)] nil)

        :else (sling-common/throw+)))))
