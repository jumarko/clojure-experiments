(ns clojure-repl-experiments.visualizations.oz
  (:require
   [cheshire.core :as json]
   [oz.core :as oz]
   [oz.server :as oz-server]))

#_(oz/start-plot-server!)

#_(oz-server/stop)

;;;; Examples from here: https://github.com/metasoarous/oz
;;;; 

(defn play-data [& names]
  (for [n names
        i (range 20)]
    {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))

;;; line plot
(def line-plot
  {:data {:values (play-data "monkey" "slipper" "broom")}
   :encoding {:x {:field "time"}
              :y {:field "quantity"}
              :color {:field "item" :type "nominal"}}
   :mark "line"})

;; Render the plot to the 
#_(oz/v! line-plot)

;;; Bar
(def stacked-bar
  {:data {:values (play-data "munchkin" "witch" "dog" "lion" "tiger" "bear")}
   :mark "bar"
   :encoding {:x {:field "time"
                  :type "ordinal"}
              :y {:aggregate "sum"
                  :field "quantity"
                  :type "quantitative"}
              :color {:field "item"
                      :type "nominal"}}})

#_(oz/v! stacked-bar)


;;; See https://github.com/metasoarous/oz/blob/master/resources/contour-lines.vega.json

(def contour-plot (json/parse-string (slurp (clojure.java.io/resource "contour-lines.vega.json")))) 
#_(oz/v! contour-plot :mode :vega)



;;; let's combine them together:
(def viz
  [:div
   [:h1 "Look ye and behold"]
   [:p "A couple of small charts"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega-lite line-plot]
    [:vega-lite stacked-bar]]
   [:p "A wider, more expansive chart"]
   [:vega contour-plot]
   [:h2 "If ever, oh ever a viz there was, the vizard of oz is one because, because, because..."]
   [:p "Because of the wonderful things it does"]])

#_(oz/view! viz)
