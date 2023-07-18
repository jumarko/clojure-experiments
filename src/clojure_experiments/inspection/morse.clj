(ns clojure-experiments.inspection.morse
  (:require
   [dev.nu.morse :as morse]))

(comment
  (launch)
  (morse/inspect {:a 1 :b 2})

  (tap> {:a 10 :b 20})

  .)
