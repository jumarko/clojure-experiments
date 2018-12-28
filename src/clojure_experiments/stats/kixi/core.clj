(ns clojure-experiments.stats.kixi.core
  "Experiments with kixi.stats library: https://github.com/mastodonC/kixi.stats
  Documentation: https://cljdoc.org/d/kixi/stats/0.4.4/doc/readme
  Lambda Island ep.43 source: https://github.com/lambdaisland/ep43-data-science-kixi-stats"
  (:require [clojure-experiments.stats.kixi.abalone :as abalone]
            [clojure-experiments.stats.kixi.graphs :as g]
            [kixi.stats.core :as kixi]
            [kixi.stats.distribution :as kixi.dist]
            [kixi.stats.math :as m]
            [net.cgrand.xforms :as x]
            [redux.core :as redux]
            [bocko.core :as b])
  (:import (java.awt Desktop)))

;;; Part 1: https://lambdaisland.com/episodes/clojure-data-science-kixi-stats
;;; =========================================================================

(rand-nth abalone/data)
;; => {:rings 13,
;;     :sex "M",
;;     :shell-weight 54.0,
;;     :diameter 88.0,
;;     :whole-weight 223.2,
;;     :viscera-weight 46.3,
;;     :length 114.0,
;;     :shucked-weight 95.5,
;;     :height 31.0}

;; let's get better sense of data
;; count, max, min, mean, etc.
(count abalone/data)
;; => 4177

(transduce (map :rings) kixi/mean abalone/data)
;; => 9.933684462532918
;; => 9.933684462532918

;; noticie kixi/mean is transducer
;; here's how it behaves step-by-step
(kixi/mean)
;; => [0.0 0.0]

(-> (kixi/mean)
    (kixi/mean 5)
    (kixi/mean 7)
    (kixi/mean 12))
;; => [24.0 3.0]

(kixi/mean [24.0 3.0])
;; => 8.0

;; if we replace completing function with identity we get both sum and count
(transduce (map :rings) (completing kixi/mean identity) abalone/data )
;; => [41493.0 4177.0]


;; mean can be distorted by outliers
;; => let's use median
;; notice, that `kixi/median` returns slightly different answer every time
;; - this is a tradeoff for better performance (not having to sort and hold all data in memory)
(transduce (map :rings) kixi/median abalone/data)
;; => 9.255813953488373
;; => 9.255813953488373
;; => 9.2
;; => 9.285714285714286

;; let's look at "summary" - min, first quartile, median, third quartile
;; max, and iqr (interquartile range - difference between q1 and q3)
(transduce (map :rings) kixi/geometric-mean abalone/data)
;; => {:min 1.0, :q1 8.0, :median 9.431372549019608,
;;               :q3 11.0, :max 29.0, :iqr 3.0}

;; let's get summary for each variable
(into {}
      (comp
       cat ;; concatenate 
       ;; get summary for each variable
       ;; x/reduce turns `kixi/summary` fn into a transducer
       (x/by-key key val (x/reduce kixi/summary)))
      abalone/data)
;; => {:rings {:min 1.0, :q1 8.0, :median 9.09375, :q3 11.0, :max 29.0, :iqr 3.0},
;;     :sex {:min nil, :q1 nil, :median nil, :q3 nil, :max nil, :iqr nil},
;;     :shell-weight {:min 0.3, :q1 26.015904471544715, :median 46.70705294705295, :q3 65.77594696969699, :max 201.0, :iqr 39.76004249815227},
;;     :diameter {:min 11.0, :q1 70.0, :median 85.0, :q3 96.0, :max 130.0, :iqr 26.0},
;;     :whole-weight {:min 0.4, :q1 88.39417977855477, :median 159.9038757763975, :q3 230.6585389282103, :max 565.1, :iqr 142.26435914965555},
;;     :viscera-weight {:min 0.1, :q1 18.644969969969964, :median 34.12173913043479, :q3 50.57916666666666, :max 152.0, :iqr 31.934196696696695},
;;     :length {:min 15.0, :q1 90.0, :median 109.0, :q3 123.0, :max 163.0, :iqr 33.0},
;;     :shucked-weight {:min 0.2, :q1 37.21798245614035, :median 67.18149867374008, :q3 100.43694513371933, :max 297.6, :iqr 63.218962677578986},
;;     :height {:min 0.0, :q1 23.0, :median 28.0, :q3 33.0, :max 226.0, :iqr 10.0}}

;; let's visualize the previous summaries
;; "Graph is worth thousand numbers"
;; check graphs.clj (wrapper for thi.ng)
;; uses OS default program for opening SVG
;; -> check http://gapplin.wolfrosch.com/ - SVG viewer for Mac OS
;; -> don't forget to set default open app for svg properly: Finder -> select file -> Get Info -> Change all
;; see also `g\render-to-tempfile`
;; you can also use open-with
#_(g/open
 (g/histogram
  {:data abalone/data
   :x :rings}))

;; let's try again with 29 bins
(transduce (map :rings) (redux/juxt kixi/min kixi/max) abalone/data)
;; => [1.0 29.0]
#_(g/open (g/histogram {:data abalone/data
                      :x :rings
                      :bin-count 29}))

;; could the distribution be log-normal?
#_(g/open (g/histogram {:data abalone/data
                      :x (comp m/log :rings)
                      :bin-count 29}))

;; some values are still missing in the previous graph
;; => introduce some jitter
#_(g/open (g/histogram {:data abalone/data
                      :x (comp m/log (g/jitter 0.5) :rings)
                      :bin-count 29}))



;;; Part 2 - look at other variables, correlations, create statistical model
;;; https://lambdaisland.com/episodes/clojure-data-science-kixi-stats-2
;;; ------------------------------------------------------------------------

;; let's look at the summary again:
(into {}
      (comp
       cat ;; concatenate 
       ;; get summary for each variable
       ;; x/reduce turns `kixi/summary` fn into a transducer
       (x/by-key key val (x/reduce kixi/summary)))
      abalone/data)
;; => {:rings {:min 1.0, :q1 8.0, :median 9.09375, :q3 11.0, :max 29.0, :iqr 3.0},
;;     :sex {:min nil, :q1 nil, :median nil, :q3 nil, :max nil, :iqr nil},
;;     :shell-weight {:min 0.3, :q1 26.015904471544715, :median 46.70705294705295, :q3 65.77594696969699, :max 201.0, :iqr 39.76004249815227},
;;     :diameter {:min 11.0, :q1 70.0, :median 85.0, :q3 96.0, :max 130.0, :iqr 26.0},
;;     :whole-weight {:min 0.4, :q1 88.39417977855477, :median 159.9038757763975, :q3 230.6585389282103, :max 565.1, :iqr 142.26435914965555},
;;     :viscera-weight {:min 0.1, :q1 18.644969969969964, :median 34.12173913043479, :q3 50.57916666666666, :max 152.0, :iqr 31.934196696696695},
;;     :length {:min 15.0, :q1 90.0, :median 109.0, :q3 123.0, :max 163.0, :iqr 33.0},
;;     :shucked-weight {:min 0.2, :q1 37.21798245614035, :median 67.18149867374008, :q3 100.43694513371933, :max 297.6, :iqr 63.218962677578986},
;;     :height {:min 0.0, :q1 23.0, :median 28.0, :q3 33.0, :max 226.0, :iqr 10.0}}

;; BUT lots of numbers don't really tell us much
;; => Let's visualize them as box plot

(def standard-normal (kixi.dist/normal {:mu 0 :sd 1}))
(def standard-normal-sample (kixi.dist/sample 10000 standard-normal))

(transduce identity kixi/summary standard-normal-sample)
;; => {:min -3.468422909435559,
;;     :q1 -0.6766960008236963,
;;     :median 0.011753102918031645,
;;     :q3 0.6850130327131912,
;;     :max 3.751167625791219,
;;     :iqr 1.3617090335368875}

;; we already know histogram...
#_(g/open (g/histogram {:data standard-normal-sample}))
;; so let's try box-plot
#_(g/open (g/box-plot {:data standard-normal-sample}))

;; let's draw them next to each other:
#_(g/open (g/vbox
         (g/histogram {:data standard-normal-sample
                       :bin-count 50
                       :left-margin 70
                       :x-domain [-5 5]})
         (g/box-plot {:data standard-normal-sample
                       :left-margin 70
                       :x-domain [-5 5]})))

;; let's look at the plots for length, diameter, height:
#_(let [plot #(g/box-plot {:data abalone/data :x %})]
  (g/open
   (g/vbox
    (plot :length)
    (plot :diameter)
    (plot :height))))

;; let's then plot height and weight
#_(g/open (g/scatter-plot
         {:data abalone/data
          :x :height
          :y :whole-weight}))

;; we now want to remove outliers we discovered
;; which are probably errors or missing data
(def remove-outliers (filter #(< 0 (:height %) 100)))
#_(g/open (g/scatter-plot
         {:data (sequence remove-outliers abalone/data)
          :x :height
          :y :whole-weight}))


;; let's visualize box-plots without outliers
#_(let [plot #(g/box-plot {:data (sequence remove-outliers abalone/data) :x %})]
  (g/open
   (g/vbox
    (plot :length)
    (plot :diameter)
    (plot :height))))


;; box plots make some things less visible - e.g. obscur clustering
;; => we can use barcode-plot
#_(let [plot #(g/barcode-plot {:data (sequence remove-outliers abalone/data) :x %})]
  (g/open
   (g/vbox
    (plot :length)
    (plot :diameter)
    (plot :height)
    (plot :shell-weight)
    (plot :viscera-weight)
    (plot :shucked-weight)
    (plot :whole-weight)
    )))

;; let's add some jitter to length, diameter, and height because these are in milimeters
#_(let [plot #(g/barcode-plot {:data (sequence remove-outliers abalone/data) :x %})]
  (g/open
   (g/vbox
    (plot (comp (g/jitter 0.5) :length))
    (plot (comp (g/jitter 0.5) :diameter))
    (plot (comp (g/jitter 0.5) :height))
    (plot :shell-weight)
    (plot :viscera-weight)
    (plot :shucked-weight)
    (plot :whole-weight)
    )))

;; and try to use logarithms to check if it looks more like normal distribution now...
#_(let [plot #(g/barcode-plot {:data (sequence remove-outliers abalone/data) :x %})]
  (g/open
   (g/vbox
    (plot (comp (g/jitter 0.5) :length))
    (plot (comp (g/jitter 0.5) :diameter))
    (plot (comp (g/jitter 0.5) :height))
    (plot (comp g/ln :shell-weight))
    (plot (comp g/ln :viscera-weight))
    (plot (comp g/ln :shucked-weight))
    (plot (comp g/ln :whole-weight))
    )))


;; now it's time to figure out which variable use to predict
;; number of rings
;; => we'll explore correlation between variables (negative, positive, zero = no correlation)
(let [variables {:rings :rings
                 :log-rings (comp g/ln :rings)
                 :length :length
                 :diameter :diameter
                 :height :height
                 :whole-weight :whole-weight
                 :shucked-weight :shucked-weight
                 :viscera-weight :viscera-weight
                 :shell-weight :shell-weight
                 :log-whole-weight (comp g/ln :whole-weight)
                 :log-shucked-weight (comp g/ln :shucked-weight)
                 :log-viscera-weight (comp g/ln :viscera-weight)
                 :log-shell-weight (comp g/ln :shell-weight)
                 }]
  (->> abalone/data
       (transduce remove-outliers (kixi/correlation-matrix variables))
       (filter #(contains? #{:rings :log-rings} (second (key %))))
       (sort-by val)
       reverse))
;; =>
;; ([[:log-rings :rings] 0.9657620285963021]
;;  [[:rings :log-rings] 0.9657620285963021]
;;  [[:log-shell-weight :log-rings] 0.7401395289870353]
;;  [[:log-whole-weight :log-rings] 0.696019947259287]
;;  [[:height :log-rings] 0.6831670725853728]
;;  [[:log-viscera-weight :log-rings] 0.6799492099739567]
;;  [[:diameter :log-rings] 0.6682178212148066]
;;  [[:shell-weight :log-rings] 0.6670829447054146]
;;  [[:length :log-rings] 0.6536347209423127]
;;  [[:log-shell-weight :rings] 0.6341735768921787]
;;  [[:log-shucked-weight :log-rings] 0.6286561058456436]
;;  [[:shell-weight :rings] 0.6281693770553377]
;;  [[:height :rings] 0.6101065785871608]
;;  [[:whole-weight :log-rings] 0.5961369422707791]
;;  [[:log-whole-weight :rings] 0.584016481114481]
;;  [[:diameter :rings] 0.5745507040035986]
;;  [[:viscera-weight :log-rings] 0.5660724194243916]
;;  [[:log-viscera-weight :rings] 0.565498390205256]
;;  [[:length :rings] 0.5565719701768042]
;;  [[:whole-weight :rings] 0.5406206352802435]
;;  [[:log-shucked-weight :rings] 0.510275448761829]
;;  [[:viscera-weight :rings] 0.5039771816106744]
;;  [[:shucked-weight :log-rings] 0.491467379145537]
;;  [[:shucked-weight :rings] 0.42115592388147793])


;; now we're ready to do actual linear regression
;; our goal is to minimize mean squared error
(g/open
 (g/scatter-plot {:data (sequence remove-outliers abalone/data)
                  :x (comp g/ln :shell-weight)
                  :y (comp g/ln :rings)}))

(transduce remove-outliers
           (kixi/simple-linear-regression (comp g/ln :shell-weight)
                                          (comp g/ln :rings))
           abalone/data)
;; => [1.1890320511416592 0.29143092300917883]
;; these are coefficients for linear formular: y = a*x + b

;; let's represent that as a function
(defn linear-model [fx fy]
  (redux/post-complete (kixi/simple-linear-regression fx fy)
                       (fn [[b a]]
                         #(+ b (* a %)))))

(def height->rings
  (transduce remove-outliers (linear-model :height :rings) abalone/data))

(transduce remove-outliers
           (kixi/mse (comp height->rings :height) :rings)
           abalone/data)
;; expressed in "rings sqaured"
;; => 6.527071537294865
;; to be more meaningful we can take square root via `kixi/rmse`
(transduce remove-outliers
           (kixi/rmse (comp height->rings :height) :rings)
           abalone/data)
;; => 2.5548134055728737

;; let's see if we can do better using logarithms
(def log-shell-weight->log-rings
  (transduce remove-outliers
             (linear-model (comp g/ln :shell-weight)
                           (comp g/ln :rings))
             abalone/data))
(transduce remove-outliers
           (kixi/rmse (comp g/exp log-shell-weight->log-rings g/ln :shell-weight)
                      :rings)
           abalone/data)
;; better but not huge difference
;; => 2.4675436415202974

;; Be aware to compare errors of the same unit
;; If you use following you'll get much better mean squared error
;; but you compare different units!
(transduce remove-outliers
           (kixi/rmse (comp log-shell-weight->log-rings g/ln :shell-weight)
                      :rings)
           abalone/data)
;; => 8.283549246040929


;; let's visualize linear regression
;; ALWAYS USEFUL!!
#_(let [graph {:data (sequence remove-outliers abalone/data)
             :x (comp g/ln :shell-weight)
             :y (comp g/ln :rings)}]
  (g/open
   (g/overlay
    (g/scatter-plot graph)
    (g/function-plot (assoc graph :fx log-shell-weight->log-rings)))))


;; Was linear model suitable?
;; Let's visualize "residuals"
;; -> they should be randomly distributed around zero
(let [graph {:data (sequence remove-outliers abalone/data)
             :x (comp g/ln :shell-weight)
             :y #(- (log-shell-weight->log-rings (g/ln (:shell-weight %)))
                    (g/ln ((g/jitter 0.5) (:rings %))))
             :fx (constantly 0)}]
  (g/open
   (g/overlay
    (g/scatter-plot graph)
    (g/function-plot graph))))

;; residuals should also be normally distributed
(let [graph {:data (sequence remove-outliers abalone/data)
             :x (comp g/ln :shell-weight)
             :y #(- (log-shell-weight->log-rings (g/ln (:shell-weight %)))
                    (g/ln ((g/jitter 0.5) (:rings %))))
             :fx (constantly 0)}]
  (g/open
   (g/histogram
    graph)))
