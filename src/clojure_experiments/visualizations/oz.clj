(ns clojure-experiments.visualizations.oz
  "Experiments with Oz visualiation library.

  See https://github.com/metasoarous/oz.
  Check http://ozviz.io/ for sharing visualizations
  https://www.youtube.com/watch?v=9uaHRWj04D4 (Vega Lite: A Grammar of Interactive Graphics - Wongsuphasawat, Moritz, and Satyanarayan)


  Look also at polis: https://github.com/pol-is/polisMath"
  (:require
   [cheshire.core :as json]
   [oz.core :as oz]
   [oz.server :as oz-server]))

(comment

  ;; the easy api function
  (oz/start-plot-server!)

  ;; custom port
  (oz-server/start! 10000)

  ;; stop when needed
  (oz-server/stop)

  (oz/live-reload!)

  )

;; my primitive example
#_(oz/v!
 {:data {:values [{:city "PB" :citizens 45000}
                  {:city "Brno" :citizens 400000}
                  {:city "Bratislava" :citizens 450000}
                  {:city "Praha" :citizens 1200000}
                  ]}
  :encoding {:x {:field "city"
                 :type "ordinal"}
             :y {:field "citizens"
                 :type "quantitative"}}
  :mark "bar"})

;;; histogram
;;; https://vega.github.io/vega-lite/tutorials/explore.html
;;; https://vega.github.io/vega-lite/docs/bin.html
;;; Histogram + color: https://youtu.be/9uaHRWj04D4?t=439
;;; - also https://vega.github.io/vega-lite/docs/bar.html#stack
;;; Interval selections and Scale domains: https://vega.github.io/vega-lite-v2/docs/selection.html#scale-domains
(defn histogram [values
                 field
                 {:keys [step scale width height title x-title y-title color]
                  :or {step 50
                       ;; see Scale: https://vega.github.io/vega-lite/docs/scale.html
                       scale "linear" ; pow, sqrt, symlog, log, time, utc
                       width 1000
                       height 500}}]
  {:title (or title "")
   :data {:values values}
   :mark "bar"
   :encoding (cond->
              {:x (cond-> {:field field
                           :type "quantitative"
                           :bin {:step step}}
                    x-title (assoc-in [:axis :title] x-title))
               :y (cond-> {:aggregate "count"
                           :type "quantitative"
                           :scale {:type scale}}
                    y-title (assoc-in [:axis :title] y-title))}
               color (assoc :color color))
   :width width
   :height height})

;; TODO: https://vega.github.io/vega-lite-v2/docs/selection.html#scale-domains
;; https://vega.github.io/vega-lite-v2/docs/bind.html
(defn scaled-histogram [values
                 field
                 {:keys [step scale width height title x-title y-title color]
                  :or {step 50
                       ;; see Scale: https://vega.github.io/vega-lite/docs/scale.html
                       scale "linear" ; pow, sqrt, symlog, log, time, utc
                       width 1000
                       height 500}}]
  (let [encoding (cond->
                  {:x (cond-> {:field field
                               :type "quantitative"
                               :bin {:step step}}
                        x-title (assoc-in [:axis :title] x-title))
                   :y (cond-> {:aggregate "count"
                               :type "quantitative"
                               :scale {:type scale}}
                        y-title (assoc-in [:axis :title] y-title))}
                   color (assoc :color color))
        common-props {:mark "bar"
                      :encoding encoding
                      :width width
                      :height height}
        selection-name "select interval"
        selection {selection-name {:type "interval"}}
        scale-selection {:scale {:domain {:selection selection-name}}}]
    {:title (or title "")
     :data {:values values}
     :vconcat [(assoc common-props :selection selection)
               (assoc-in common-props [:encoding :x] scale-selection)]}))


(defn boxplot
  "https://vega.github.io/vega-lite/docs/boxplot.html."
  ([values field]
   (boxplot values field nil))
  ([values field mark-options]
   {:data {:values values}
    :mark
    (merge
     {:type "boxplot"}
     mark-options)
    :encoding {:x {:field field
                   :type "quantitative"}}}))

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

#_(def contour-plot (json/parse-string (slurp (clojure.java.io/resource "contour-lines.vega.json")))) 
#_(oz/v! contour-plot :mode :vega)



;;; let's combine them together:
#_(def viz
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


;;; Share your cool plot with someone else
;;; This requires personal access token stored in ~/.oz/github-creds.edn
;;; See https://github.com/metasoarous/oz#authentication

#_(oz/publish! viz)
;; =>
;; Gist url: https://gist.github.com/fb9572a38d29806dab6675ea995fb2cd
;; Raw gist url: https://api.github.com/gists/fb9572a38d29806dab6675ea995fb2cd
;; Ozviz url: http://ozviz.io/#/gist/fb9572a38d29806dab6675ea995fb2cd






