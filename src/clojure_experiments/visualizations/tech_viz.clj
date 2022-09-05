(ns clojure-experiments.visualizations.tech-viz
  "https://github.com/techascent/tech.viz
  See also https://github.com/scicloj/viz.clj"
  (:require
   [clojure.data.json :as json]
   [clojure-experiments.clipboard :as clip]
   [clojure.java.shell :as sh]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [tech.v3.dataset :as ds]
   [tech.viz.vega :as vega]))

(def my-scatterplot (vega/scatterplot [{:a 1 :b 2} {:a 2 :b 3}] :a :b))

;; inspired by `desktop-view-vega` from a comment inside `tech.viz.vega`:
(defn macos-view-vega
  [vega-spec filename]
  (vega/vega->svg-file vega-spec filename)
  (sh/sh "open" filename))

;; visualize it!
(comment
  (macos-view-vega my-scatterplot "scatterplot.svg")

.)


;;; Iris pairs plot: https://github.com/techascent/tech.viz#iris-pairs-plot
;;; Iris Data Set: https://archive.ics.uci.edu/ml/datasets/iris
;;; - This is perhaps the best known database to be found in the pattern recognition literatur
(comment

  (def iris-raw-str* (delay (slurp "https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data")))

  (def iris-mapseq
    (->> @iris-raw-str*
         (str/split-lines)
         (map #(str/split % #"\,"))
         (map (fn [[sl sw pl pw class]]
                {:sepal-length (Double. sl)
                 :sepal-width (Double. sw)
                 :petal-length (Double. pl)
                 :petal-width (Double. pw)
                 :class class}))))

  ;; create the pairs graph: https://en.wikipedia.org/wiki/Scatter_plot#Scatter_plot_matrices
  (let [spec (vega/pairs iris-mapseq
                         [:sepal-length :sepal-width :petal-length :petal-width]
                         {:label-key :class
                          :background :#f8f8f8})]
    (pp/pprint spec)
    (macos-view-vega spec "iris-pairs.svg")
    #_(vega/vega->svg-file spec "iris-pairs.svg"))


  .)

;;; visualizing datasets is easier with `tech.v3.dataset`
;;; - see test/data: https://github.com/techascent/tech.viz/tree/master/test/data
(comment
  ;; create scatterplot with `ds/mapseq-reader`
  (def desktop-default-options {:background "#FFFFFF"})
  (def example-scatterplot
    (-> (ds/->dataset "resources/datasets/spiral-ds.csv")
        (ds/mapseq-reader)
        (vega/scatterplot "x" "y"
                     (merge {:title "Spriral Dataset"
                             :label-key "label"}
                            desktop-default-options))))

  ;; create histogram with `ds/->dataset`
  (def example-histogram
    (-> (slurp "https://vega.github.io/vega/data/cars.json")
        (json/read-str :key-fn keyword)
        (ds/->dataset)
        (ds/column :Displacement)
        (vega/histogram "Displacement" (merge desktop-default-options
                                         {:bin-count 15
                                          :title "Displacement Histogram"}))))

  ;; ... you can then view the graphs as SVGs
  (macos-view-vega example-histogram "histgoram.svg")

  ;; ... or paste them into online vega editor: https://vega.github.io/editor/#/
  ;; => https://vega.github.io/editor/?#/url/vega/N4IghgrgLg9gzgSwF4FMQC5RQJ4Ac3ogBmCUIANCAE4qKoZRUQqUDGMAdlGAh3BiFxgAJsN4BzEAF9KYAB60MAbVAwqCFFwEAjGFFgBbCiDiswAGwIg5pi2kpQErANYBhGBC3oArA9KWBABEEOFxzMFYUA00yGVV1GIFLIjJKWwDCbHT7EEcXd08yHykAXUptXgBadkKMAEZfEAB3BGEoAAsMAA4ABh7yiOdxKg8OYQEAYgAxGdnjbP50FRBhGAMeDmUANi7yABZvbzLqMA5xKxa2zsoOMGiBGzMAylQRjCILOBYQDicCD-MX0oI0843QjGYDjwVnMvBQYCo0nIoFW614GBRYG4Ol4HBQ40oJBQ5jBIBqWhkJzOVnaKAQ4naqR+dysWSeOV+kQYTG+ILG3MhuWhSThCOMrxgApQpT8UAyIGCoXCkWiXAABAAJEKwYZ3YzrKjORbLTTsYQEUAQXDCLEW4gIczmDH24mk9jmNRIkyMGDOO0ANwszEmACYw165M6iSSkigUvN2Q9ssYYEQiF8iqBA+Zg+gegA6bxSSlyENRjQxwjqBlM5OER52FNpjPO7O5gtFynYcuugTk2uJzLJyip9MoTMgNsEDvFyjYMuYSdB6dpQcgNmNkfN8et5cYGfFylEEZGRc27GECocPHjSk4fACGisWLHAAkplp6wEjKguDg6AAegA-0UHEMB83EUh2ggbR8wQGAAI-KIwGA0CUP9bx8wAKzgThjFpelGQwA5+hWLEwGUUBbnuS9cXxYwp2NUB+wwbwtkoZIih2YFCKKABOABmfNdjJGAPURQgJkCVxAj4wIeiRZjRiKLoBI4uN+KEkTqyI9A6jqPj83Y0TxMmABRMyACFXAEqZFNE2p0C2UjOPqAyjJ4mt6gOfM9jYMTPUksy9mk1w+Pslj0BDYzXL0ny-OoXj6gAdjqfMQ380zJOSvZkrCuoIuU1j1PjPTUvSzzdIM5LMsCkAJgE7wBLMprCscuo1JAWLqsqoowxDYTaok+q9gAQW8aLwriByvAEjKuo0jB+sGxKvKivYug8ky6omLowoE0atjarwQxE2KQw2radL6nLfKGyZksCLo9j4qbkRmooXMWqLboS66MAEvp0r6EHQZBxp3R2kMzIErY9ks46ij2TrYsBgsQzBzGekaf70Dm4zIeGiZLPhvKy2myKQz4kqinx3qAe8NKRMJyZVK6Ua+LMxHioW0rGqZ+m8eS5KtpZyTGua1qKaKsqaYB4WrqS9A9h6ISVax0HOrF+q2O8Mzku8bm8bl5XVd8jXNcFvYqYq7aiYEh3XEZo2Yu+63DPm3GDghgL7b4vYGmS6QSlKKQgA
  (-> example-histogram
      json/write-str
      clip/spit-clipboard)

  ;; ... you can also visualize it with Portal: see `clojure-experiments.portal`
  ;; - TODO: this doesn't work as is
  (tap> #_{:data
         {:values
          [{:a "A", :b 28}
           {:a "B", :b 55}
           {:a "C", :b 43}
           {:a "D", :b 91}
           {:a "E", :b 81}
           {:a "F", :b 53}]}
         :mark "bar"
         :encoding
         {:x
          {:field "a"
           :type "nominal"
           :axis {:labelAngle 0}}
          :y {:field "b", :type "quantitative"}}}
       example-histogram)



  )


(defn my-f [x]
  (meta x))

(my-f ^{:meta1 "meta"} {})
