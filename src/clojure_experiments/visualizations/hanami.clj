(ns clojure-experiments.visualizations.hanami
  (:require [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [aerial.hanami.core :as hmi]))


#_(hc/xform ht/point-chart
          :UDATA "data/cars.json"
          :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
