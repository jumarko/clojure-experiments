(ns clojure-experiments.stats.kixi.example-graphs
  (:require [clojure-experiments.stats.kixi.graphs :as g]
            [clojure-experiments.stats.kixi.abalone :as abalone]
            [redux.core :as redux]
            [kixi.stats.core :as kixi]
            [net.cgrand.xforms :as x]
            [thi.ng.geom.viz.core :as viz]))

(def data (filter #(< 0 (:height %) 100) abalone/data))

;; Redefine this with your preferred method of viewing the graphs
(defn show [spec]
  (g/open spec)
  #_(g/open-with spec "firefox")
  #_(g/render-to-file "graph.svg"))

(comment



  ;; Histogram clearly showing the log-normal shape of the data. The area of 1
  ;; standard deviation from the median is given a darker color. Some jittering is
  ;; added to make the discrete ring counts more amenable to binning.


  (let [[sd mean] (transduce (map (comp #(Math/log %) :rings))
                             (redux/juxt kixi/standard-deviation
                                         kixi/median)
                             data)]
    (-> {:data      data
         :x         (comp g/ln (g/jitter 0.5) :rings)
         :bin-count 28
         :stroke    (fn [[rings count]]
                      (if (<= (- mean sd) rings (+ mean sd))
                        "#3385FF"
                        "#77CCFF"))}
        g/histogram
        show))

  ;; Barcode plot of the height data. The two highest measurements are extreme
  ;; outliers, we'll assume they're mistakes and drop them. Different sexes are
  ;; represented with colors.
  (-> {:data           (sequence (comp (x/sort-by :height) (x/drop-last 2)) data)
       :x              (comp (g/jitter 0.5) :height)
       :width          1000
       :height         150
       :stroke-width   "1px"
       :stroke         #(case (:sex %)
                          "M" "rgb(20,50,150)"
                          "F" "rgb(150,20,50)"
                          "I" "rgb(20,150,50)")
       :stroke-opacity 0.1}
      g/barcode-plot
      show)

  ;; A scatterplot of rings vs shell-weight on logarithmic axes, including major
  ;; and minor grid lines.

  (-> {:data data
       :x (comp (g/jitter 0.5) :rings)
       :y :shell-weight
       :x-axis-fn viz/log-axis
       :y-axis-fn viz/log-axis
       :grid-minor-x? true
       :grid-minor-y? true}
      g/scatter-plot
      show)

  ;; Box plots of all numeric variables. The extreme outliers for height
  ;; immediately stick out.

  (show
   (g/hbox
    (apply g/vbox (map #(g/box-plot {:data abalone/data :x %})
                       [:rings
                        :length
                        :diameter
                        :height]))
    (apply g/vbox (map #(g/box-plot {:data abalone/data :x %})
                       [:whole-weight
                        :shucked-weight
                        :viscera-weight
                        :shell-weight]))))



  ;; Barcode plots for the different variables. These make it really clear that
  ;; the length measurements only have a milimeter precision.


  (show
   (g/hbox
    (apply g/vbox (map #(g/barcode-plot {:data data :x %})
                       [:rings
                        :length
                        :diameter
                        :height]))
    (apply g/vbox (map #(g/barcode-plot {:data data :x %})
                       [:whole-weight
                        :shucked-weight
                        :viscera-weight
                        :shell-weight]))))

  ;; Perform a linear regression, show a scatterplot of the original data with
  ;; some jitter applied, plus the line of the regression.
  ;;
  ;; Add some opacity to better show the density.

  (let [log-rings    (comp g/ln :rings)
        regress      (kixi/simple-linear-regression :height log-rings)
        [b a]        (transduce identity regress data)
        linear-model (fn [x]
                       (+ b (* a x)))]
    (show (g/overlay (g/scatter-plot {:data           data
                                      :x              (comp (g/jitter 0.5) :height)
                                      :y              (comp g/ln (g/jitter 0.5) :rings)
                                      :fill-opacity   0.3
                                      :radius         4
                                      :stroke         "#fda"
                                      :stroke-opacity 0.1})
                     (g/function-plot {:data data
                                       :x    :height
                                       :y    log-rings
                                       :fx   linear-model}))))


  ;; A plot of the residuals


  (let [log-rings    (comp g/ln :rings)
        regress      (kixi/simple-linear-regression :height log-rings)
        [b a]        (transduce identity regress data)
        linear-model (fn [x]
                       (+ b (* a x)))]
    (show (g/overlay (g/scatter-plot {:data           data
                                      :x              (comp (g/jitter 0.5) :height)
                                      :y              #(- (g/ln ((g/jitter 0.5) (:rings %))) (linear-model (:height %)))
                                      :fill-opacity   0.3
                                      :radius         4
                                      :stroke         "#fda"
                                      :stroke-opacity 0.1})
                     (g/function-plot {:data data
                                       :x    :height
                                       :y-domain [0 1]
                                       :fx   (constantly 0)}))))


  ;; Abuse a scatter-plot to show the correlation matrix


  (let [positions (into {} (map vector abalone/numeric-variables (drop 1 (range))))]
    (show (g/scatter-plot {:data     (transduce identity (kixi/correlation-matrix (into {} (x/for [k %] [k k]) abalone/numeric-variables)) data)
                           :x-domain [0 (count abalone/numeric-variables)]
                           :y-domain [0 (count abalone/numeric-variables)]
                           :x        #(positions (first (key %)))
                           :y        #(positions (second (key %)))
                           :radius   #(* (val %) (val %) (val %) 10) ;; Third power to make the differences a bit more pronounced
                           :x-label  (viz/default-svg-label #(try (nth abalone/numeric-variables (dec (long %)))
                                                                  (catch Throwable _))) ;; too lazy to bother about index out of bounds

                           :y-label  (viz/default-svg-label #(try (nth abalone/numeric-variables (dec (long %)))
                                                                  (catch Throwable _)))
                           :left-margin 100
                           :right-margin 50})))

  ;; A QQ-plot (https://en.wikipedia.org/wiki/Q%E2%80%93Q_plot)
  ;; of rings vs height, and log(rings) vs height

  (let [hr (transduce (map :rings) kixi/histogram data)
        hh (transduce (map :height) kixi/histogram data)]
    (show (g/function-plot {:data data
                            :x-domain [0 29]
                            :y-domain [0 50]
                            :fx (fn [x]
                                  (kixi.dist/quantile hh (kixi.dist/cdf hr x)))})))

  (let [hr (transduce (map (comp g/ln :rings)) kixi/histogram data)
        hh (transduce (map :height) kixi/histogram data)]
    (show (g/function-plot {:data data
                            :x-domain [0 (g/ln 29)]
                            :y-domain [0 50]
                            :fx (fn [x]
                                  (kixi.dist/quantile hh (kixi.dist/cdf hr x)))}))))
