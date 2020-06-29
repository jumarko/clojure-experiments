(ns clojure-experiments.visualizations.vega-repl
  "vega.repl is supposed to be the simplest way to go from Clojure data and vega/vega-lite spec
  to a rendered visualization.
  the simplest possible way to go from Clojure data and a Vega or Vega-Lite spec to a rendered visualizatioo https://github.com/zane/vega.repl"
  (:require 
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [aerial.hanami.core :as hmi]
            [zane.vega.repl :refer [vega]]))

;;; See vega-lite: https://vega.github.io/vega-lite/
;;; - Introduction to vega lite: https://vega.github.io/vega-lite/tutorials/getting_started.html
;;; - next tutorial: Exploring Data: https://vega.github.io/vega-lite/tutorials/explore.html
;;; - Examples: https://vega.github.io/vega-lite/examples/
;;; - Full docs (Overview): https://vega.github.io/vega-lite/docs/
;;;   - data types: https://vega.github.io/vega-lite/docs/type.html
;;;   - mark types: https://vega.github.io/vega-lite/docs/mark.html#types


;;; perhaps can be combined with hanami for even easier visualizations? https://github.com/jsa-aerial/hanami

(def hanami-chart
  (hc/xform ht/point-chart
            :UDATA "/Users/jumar/workspace/clojure/clojure-experiments/resources/cars.json"
            :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin"))
;;=> 
{:encoding
 {:y {:field "Miles_per_Gallon", :type "quantitative"},
  :color {:field "Origin", :type "nominal"},
  :x {:field "Horsepower", :type "quantitative"},
  :tooltip
  [{:field "Horsepower", :type "quantitative"} {:field "Miles_per_Gallon", :type "quantitative"}]},
 :mark {:type "circle"},
 :width 400,
 :background "floralwhite",
 :height 300,
 :data {:url "resources/cars.json"}}

#_(vega hanami-chart)


(comment


  ;; https://github.com/zane/vega.repl
  (require '[zane.vega.repl :refer [vega]])

  (vega {:$schema "https://vega.github.io/schema/vega-lite/v3.json"
         :description "A simple bar chart with embedded data."
         :data {:values [{:a "A" :b 28}
                         {:a "B" :b 55}
                         {:a "C" :b 43}
                         {:a "D" :b 91}
                         {:a "E" :b 81}
                         {:a "F" :b 53}
                         {:a "G" :b 19}
                         {:a "H" :b 87}
                         {:a "I" :b 52}]}
         :mark "bar"
         :encoding {:x {:field "a" :type "ordinal"}
                    :y {:field "b" :type "quantitative"}}})

  ;; points
  (vega {:$schema "https://vega.github.io/schema/vega-lite/v3.json"
         :description "Points"
         :data {:values [{:a "A" :b 28}
                         {:a "B" :b 55}
                         {:a "C" :b 43}
                         {:a "D" :b 91}
                         {:a "E" :b 81}
                         {:a "F" :b 53}
                         {:a "G" :b 19}
                         {:a "H" :b 87}
                         {:a "I" :b 52}]}
         :mark "point"
         :encoding {:x {:field "b" :type "quantitative"}
                    }})
  
  ;;
  )
