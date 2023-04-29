(ns clojure-experiments.visualizations.vega-view
  "See https://github.com/applied-science/emacs-vega-view")


;;; https://github.com/applied-science/emacs-vega-view#clojure
{:data {:values (map hash-map
                     (repeat :a)
                     (range 1 20)
                     (repeat :b)
                     (repeatedly #(* 100 (Math/random))))}
 :mark "bar",
 :width 800
 :height 600
 :encoding {:x {:field :a, :type "ordinal", :axis {"labelAngle" 0}},
            :y {:field :b, :type "quantitative"}}}



;;; Vega Lite intro: https://vega.github.io/vega-lite/tutorials/getting_started.html

(def my-data [{:a "C" :b "2"}
              {:a "C" :b "7"}
              {:a "C" :b "4"}
              {:a "D" :b "1"}
              {:a "D" :b "2"}
              {:a "D" :b "6"}
              {:a "E" :b "8"}
              {:a "E" :b "4"}
              {:a "E" :b "7"}])
;; visualize my-data with Vega
;; - this isn't enough!
{:data {:values my-data}}
;; Error: Invalid specification ....
;; Make sure the specification includes at least one of the following properties: "mark", "layer", "facet", "hconcat", "vconcat", "concat", or "repeat".

;; ... you need to tell it HOW it should be visualized
;; we do that with "marks": https://vega.github.io/vega-lite/docs/mark.html
;; We could e.g. say it's a point
{:data {:values my-data}
 :mark "point"}
;; => this is boring, because it only shows a single point (all the points are overlapping because we haven not specified their position)

;; ... to separate the points, we can map the data to visual properties of the mark
;; we can, for instance, _encode_ :a to "x" channel (represents x-position of the points)
;; - notice we also specify field's data type: https://vega.github.io/vega-lite/docs/type.html
{:data {:values my-data}
 :mark "point"
 :encoding {:x {:field :a :type :nominal}}}

;; ... 3 points in each category are still overlapping
;; => let's map field :b to 'y' too
{:data {:values my-data}
 :mark "point"
 :encoding {:x {:field :a :type :nominal}
            :y {:field :b :type :quantitative}}}


;; Aggregations - show average in each category
{:data {:values my-data}
 :mark "point"
 :encoding {:x {:field :a :type :nominal}
            :y {:aggregate :average :field :b :type :quantitative}}}
;; ... but typically, we show averages as a bar chart
{:data {:values my-data}
 :mark "bar"
 :encoding {:x {:field :a :type :nominal}
            :y {:aggregate :average :field :b :type :quantitative}}}
;; ... what about horizontal bar chart?
{:data {:values my-data}
 :mark "bar"
 ;; notice we swapped `:y` and `:x`
 :encoding {:y {:field :a :type :nominal}
            :x {:aggregate :average :field :b :type :quantitative}}}


;; let's now customize our visualization - providing custom axis titles
{:data {:values my-data}
 :mark "bar"
 ;; notice we swapped `:y` and `:x`
 :encoding {:y {:field :a :type :nominal}
            :x {:aggregate :average :field :b :type :quantitative
                :title "Mean of b"}}}



;;; Vega Lite Intro 2 - Exploring Data: https://vega.github.io/vega-lite/tutorials/explore.html
{:data {:url "seattle-weather.csv"}
 :mark "tick"
 :encoding {:x {:field :precipitation :type :quantitative}}}


;; to create a histogram, we need to add encoding channel for "y"
;; that shows aggreggated 'count'
{:data {:url "seattle-weather.csv"}
 :mark "bar"
 :encoding {:x {:field :precipitation :type :quantitative
                :bin true} ; NOTICE `:bin true`
            :y {:aggregate "count"}}}


;; Changes over time
{:data {:url "seattle-weather.csv"}
 :mark "line"
 :encoding {:x {:timeUnit :month :field :date}
            :y {:aggregate :mean :field :precipitation}}}

;; ... let's look at avg temperatures
;; - notice I plot both of them simultaneously using `:layer`
{:data {:url "seattle-weather.csv"}
 :mark "line"
 :layer [{:mark {:type "line" :color "red"}
          :encoding {:x {:timeUnit :month :field :date}
                     :y {:aggregate :mean :field :temp_max :title "avg-max-temp"}}}
         {:mark {:type "line" :color "blue"}
          :encoding {:x {:timeUnit :month :field :date}
                     :y {:aggregate :mean :field :temp_min :title "avg-min-temp"}}}]}


;; Let's look at seasonal trends for each year separately => use :yearmonth 
{:data {:url "seattle-weather.csv"}
 :mark "line"
 :encoding {:x {:timeUnit :yearmonth :field :date}
            :y {:aggregate :max :field :temp_max}}}
;; ... let's see if the max temperatur is really increasing from year to year
{:data {:url "seattle-weather.csv"}
 :mark "line"
 :encoding {:x {:timeUnit :year :field :date}
            :y {:aggregate :mean :field :temp_max}}}

{:data {:url "seattle-weather.csv"}
 :transform [{:calculate "datum.temp_max - datum.temp_min"
              :as :temp-range}]
 :mark "line"
 :encoding {:x {:timeUnit :month :field :date}
            :y {:aggregate :mean :field :temp-range}}}


;; Finally, let's look at how different kind of weather is distributed throughout the year
;; We'll use :month as a time unit and count number of records on the y-axis
;; We'll assign different colors to different values of 'weather' field
;; - when use map a field to colors in a bar chart, Vega Lite automatically
;;   stacks the bars atop each other
{:data {:url "seattle-weather.csv"}
 :mark "bar"
 :encoding {:x {:timeUnit :month :field :date #_#_:type :ordinal}
            :y {:aggregate :count :type :quantitative}
            :color {:field :weather :type :nominal}}}

;; ... BUT the default colors don't fit well => let's customize the colors
{:data {:url "seattle-weather.csv"}
 :mark "bar"
 :encoding {:x {:timeUnit :month :field :date #_#_:type :ordinal}
            :y {:aggregate :count :type :quantitative}
            :color {:field :weather :type :nominal
                    :scale {:domain ["sun" "fog" "drizzle" "rain" "snow"]
                            :range ["#e7ba52" "#c7c7c7" "#aec7e8" "#1f77b4" "#9467bd"]}
                    :title "Weather type"}}}
