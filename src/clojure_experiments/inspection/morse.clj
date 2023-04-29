(ns clojure-experiments.inspection.morse
  (:require
   [dev.nu.morse :as morse]))

(defn launch []
  (morse/launch-in-proc))

(comment
  (launch)
  (morse/inspect {:a 1 :b 2})

  .)
