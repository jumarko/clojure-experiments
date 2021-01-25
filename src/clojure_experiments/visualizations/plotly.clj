(ns clojure-experiments.visualizations.plotly
  "https://github.com/findmyway/plotly-clj
  Recommended by David Pham on clojurians slack (#data-science)."
  (:require [plotly-clj.core :as pc]))

;; (pc/offline-init)

;; (-> (pc/plotly [2 1 3])
;;     (pc/add-scatter)
;;     pc/iplot)
