(ns clojure-experiments.stats.kixi.graphs
  (:require [thi.ng.geom.viz.core :as viz]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.vector :as v]
            [thi.ng.color.core :as col]
            [thi.ng.math.core :as m :refer [PI TWO_PI]]
            [kixi.stats.core :as kixi]
            [kixi.stats.distribution :as kixi.dist]
            [net.cgrand.xforms :as x]
            [clojure.java.browse :as browse]
            [clojure.java.shell :as sh])
  (:import [java.lang Math]))

(def ^:dynamic *defaults*
  "Default values for graphs, these can all be changed by passing them in
  explicitly to the graph functions, or you can rebind them here to provide your
  own defaults."
  {:width            800
   :height           500
   :left-margin      50
   :right-margin     10
   :top-margin       20
   :bottom-margin    30
   :x-padding-factor 0.1
   :y-padding-factor 0.1
   :grid?            true
   :grid-stroke      "#caa"
   :major-x?         true
   :major-y?         true
   :grid-minor-x?    false
   :grid-minor-y?    false})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utilities

(defn log10 [n]
  (Math/log10 n))

(defn ln [n]
  (Math/log n))

(defn exp [n]
  (Math/exp n))

(defn pow [base exp]
  (Math/pow base exp))

(defn round [n]
  (Math/round n))

(defn floor [n]
  (Math/floor n))

(defn precision-round
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (pow 10 precision)]
    (/ (round (* d factor)) factor)))

(defn graph-opts [opts]
  (merge *defaults* opts))

(defn opts->xs
  "Given a opts map, return a sequence of x values."
  [{:keys [data x xs] :or {x first}}]
  (or xs (map x data)))

(defn opts->ys
  "Given a opts map, return a sequence of y values."
  [{:keys [data y ys] :or {y second}}]
  (or ys (map y data)))

(defn maybe-call
  "Allows specifying an option either as a concrete value, or a function of the data point.

  e.g.

  {:stroke \"black\"}
  {:stroke (fn [{:keys [rings]}] (str \"rgb(80,\" (* rings 5) \", 20)\"))}

  Returns `fallback' if no ifn-or-value is given.
  "
  [ifn-or-value data-point & [fallback]]
  (cond
    (nil? ifn-or-value) fallback
    (ifn? ifn-or-value) (ifn-or-value data-point)
    :else               ifn-or-value))

(defn jitter
  "Randomly move a value within a certain limit around its current value."
  [limit]
  (fn [x]
    (+ x (- (rand (* 2 limit)) limit))))

(defn histo-bins
  "Split data in a number of bins of equal size. Returns pairs containing the
  center of the bin, and the number of values in that bin."
  [data bin-count]
  (let [hist      (transduce identity kixi/histogram data)
        max'      (kixi.dist/maximum hist)
        min'      (kixi.dist/minimum hist)
        range'    (- max' min')
        bin-width (/ range' (dec bin-count))
        cnt       (count hist)
        bin-cdf   #(kixi.dist/cdf hist (+ min' (* bin-width %)))]
    (for [bin-idx (range bin-count)]
      ;; #items x fraction in bin
      [(+ min' (* bin-width bin-idx))
       (* cnt (- (bin-cdf (+ bin-idx 0.5))
                 (bin-cdf (- bin-idx 0.5))))])))

(defn tick-mark-size
  "Return a round number that can be used as a major tick mark size based on the
  domain size of the data."
  [range]
  (let [l10 (log10 range)
        mag (pow 10 (floor l10))
        x   (/ range mag)]
    (* mag
       (cond
         (<= x 3)
         0.2
         (<= 3 x 6)
         0.5
         :else
         1))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SVG layouts

;; These are used as geom-viz's :layout parameter, they do the actual drawing
;; using the specified coordinate system.
;;
;; Implementing a new graph type means first implementing one of these, and then
;; setting up an actual graph function that returns a renderable
;; graph-specification.

(defn svg-barcode-plot
  "Draw the lines of the barcode plot, returning an svg group.

  Assumes a y-axis domain from 0 to 1. Specify :y-start/:y-end to draw in a
  different range.

  :stroke, :stroke-opacity, :stroke-width, :y-start, :y-end may be given as
  concrete values or functions of the data point. "
  [{:keys [x-axis y-axis project] :or {project v/vec2} :as v-spec}
   {:keys [data x stroke stroke-opacity stroke-width y-start y-end]
    :as   d-spec}]
  (let [scale-x (:scale x-axis)
        scale-y (:scale y-axis)]
    (apply svg/group {}
           (for [d data
                 :let [v              (x d)
                       stroke         (maybe-call stroke d "rgb(20,50,150)")
                       stroke-opacity (maybe-call stroke-opacity d 0.2)
                       stroke-width   (maybe-call stroke-width d "1px")
                       y-start        (maybe-call y-start d 0)
                       y-end          (maybe-call y-end d 1)]]
             (svg/line (project [(scale-x v) (scale-y y-start)])
                       (project [(scale-x v) (scale-y y-end)])
                       {:stroke         stroke
                        :stroke-opacity stroke-opacity
                        :stroke-width   stroke-width})))))

(defn svg-box-plot
  [{:keys [x-axis y-axis project] :or {project v/vec2} :as v-spec}
   {:keys [color y-pos box-size whisker-size]
    :as   opts
    :or   {y-pos        0.5
           box-size     0.2
           whisker-size 0.1
           color        "#39D"}}]

  (let [values                             (opts->xs opts)
        scale-x                            (:scale x-axis)
        scale-y                            (:scale y-axis)
        from-center                        #(scale-y (- y-pos %))
        {:keys [q1 q3 median min max iqr]} (transduce identity kixi/summary values)
        min-whisker                        (x/some (comp (filter #(> % (- q1 (* 1.5 iqr))))
                                                         x/min)
                                                   values)
        max-whisker                        (x/some (comp (filter #(< % (+ q3 (* 1.5 iqr))))
                                                         x/max)
                                                   values)
        outliers                           (remove #(< (- q1 (* 1.5 iqr)) % (+ q3 (* 1.5 iqr))) values)]
    (svg/group {}
               ;; crossbar
               (svg/line (project [(scale-x min-whisker) (from-center 0)])
                         (project [(scale-x max-whisker) (from-center 0)])
                         {:stroke color})

               ;; box = inter quartile range
               (svg/rect (project [(scale-x q1) (from-center (- box-size))])
                         (- (scale-x q3) (scale-x q1))
                         (- (from-center (- box-size))
                            (from-center box-size))
                         {:stroke color :fill "white"})

               ;; median
               (svg/line (project [(scale-x median) (from-center box-size)])
                         (project [(scale-x median) (from-center (- box-size))])
                         {:stroke       color
                          :stroke-width "4px"})

               ;; whiskers
               (let [y1    (from-center whisker-size)
                     y2    (from-center (- whisker-size))
                     attrs {:stroke color :stroke-width "2px"}]
                 (svg/group
                  {}
                  (svg/line (project [(scale-x min-whisker) y1])
                            (project [(scale-x min-whisker) y2])
                            attrs)
                  (svg/line (project [(scale-x max-whisker) y1])
                            (project [(scale-x max-whisker) y2])
                            attrs)))

               ;; outliers
               (apply svg/group {}
                      (for [o outliers]
                        (svg/circle [(scale-x o) (from-center ((jitter 0.1) 0))]
                                    3
                                    {:fill color
                                     :fill-opacity 0.5}))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph components
;;
;; Bits and pieces that make up a renderable graph-specification

(defn x-domain [opts]
  (if-let [domain (:x-domain opts)]
    domain
    (let [xs      (opts->xs opts)
          min-x   (apply min xs)
          max-x   (apply max xs)
          range-x (- max-x min-x)
          padding (* (:x-padding-factor opts) range-x)]
      [(- min-x padding) (+ max-x padding)])))

(defn x-axis-spec
  "Auto-generate an x-axis description that spans the domain of provided values."
  [opts]
  (let [{:keys [width
                height
                left-margin
                right-margin
                bottom-margin
                x-padding-factor
                x-label]}
        opts

        [min-x max-x :as domain] (x-domain opts)
        delta (- max-x min-x)]
    (cond-> {:domain  domain
             :range   [left-margin (- width right-margin)]
             :major   (:x-major opts (tick-mark-size delta))
             :minor   (:x-minor opts (/ (tick-mark-size delta) 2))
             :pos     (- height bottom-margin)
             :visible (:x-visible opts true)}
      x-label (assoc :label x-label))))

(defn y-domain [opts]
  (if-let [domain (:y-domain opts)]
    domain
    (let [ys      (opts->ys opts)
          min-y   (apply min ys)
          max-y   (apply max ys)
          range-y (- max-y min-y)
          padding (* (:y-padding-factor opts) range-y)]
      [min-y (+ max-y padding)])))

(defn y-axis-spec [opts]
  (let [{:keys [height left-margin bottom-margin top-margin y-label]} opts
        [min-y max-y :as domain] (y-domain opts)
        delta (- max-y min-y)]
    (cond-> {:domain      domain
             :range       [(- height bottom-margin) top-margin]
             :pos         left-margin
             :major       (:y-major opts (tick-mark-size delta))
             :minor       (:y-minor opts (/ (tick-mark-size delta) 2))
             :label-dist  15
             :label-style {:text-anchor "end"}
             :visible     (:y-visible opts true)}
      y-label (assoc :label y-label))))

(defn grid-spec [{:keys [grid-stroke
                         major-x?
                         major-y?
                         grid-minor-x?
                         grid-minor-y?
                         grid?] :as opts}]
  (when grid?
    (:grid opts {:attribs {:stroke grid-stroke}
                 :major-x major-x?
                 :major-y major-y?
                 :minor-x grid-minor-x?
                 :minor-y grid-minor-y?})))

(defn xy-coordinate-system [{:keys [x-axis x-axis-fn
                                    y-axis y-axis-fn]
                             :or   {x-axis-fn viz/linear-axis
                                    y-axis-fn viz/linear-axis}
                             :as   opts}]
  (let [x-spec (->> opts
                    x-axis-spec
                    (or x-axis))
        y-spec (->> opts
                    y-axis-spec
                    (or y-axis))]
    {;; expected by geom
     :x-axis    (x-axis-fn x-spec)
     :y-axis    (x-axis-fn y-spec)
     :grid      (grid-spec opts)

     ;; stored for possible further processing, e.g. overlay
     :x-axis-spec x-spec
     :y-axis-spec y-spec
     :x-axis-fn x-axis-fn
     :y-axis-fn y-axis-fn}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graphs
;;
;; These, together with the render functions below, are the main entry points
;; for this namespace. These functions all return a map with
;; keys :plot, :graph, :width, :height.
;;
;; :plot is the drawing function, it receives :graph, which is typically a
;; geom-viz graph specification (:x-axis, :y-axis, :data, etc.)
;;
;; width/height can be inspected to e.g. set the SVG canvas size. They can also
;; be used to lay out multiple graphs on a page.

(defn barcode-plot
  "Return the graph specification for a barcode plot."
  [opts]
  (let [{:keys [width height] :as opts}
        (graph-opts (merge {:left-margin 10
                            :x           identity
                            :grid?       false
                            :y-domain    [0 1]
                            :y-visible   false
                            :height      100}
                           opts))]
    {:plot   viz/svg-plot2d-cartesian
     :width  width
     :height height
     :graph  (assoc (xy-coordinate-system opts)
                    :data [(-> opts
                               (select-keys [:data
                                             :x
                                             :stroke
                                             :stroke-opacity
                                             :stroke-width
                                             :y-start
                                             :y-end])
                               (assoc :layout svg-barcode-plot))])}))

(defn histogram
  "Draws a histogram based on :data and :x (a key or function). Uses t-digest to
  calculate bins, so re-rendering can yield slightly different results.
  Specify :bin-count to change the number of bins, otherwise uses a rule of
  thumb for number of bins based on the data size."
  ([opts]
   (let [{:keys [data
                 width
                 height
                 stroke
                 stroke-width
                 interleave
                 bar-width]
          :or   {stroke       "#3385ff"
                 stroke-width 10
                 interleave   1
                 bar-width    0}
          :as   opts} (graph-opts (merge {:x identity} opts))

         ;; Sturge's Rule of thumb
         bin-count (:bin-count opts (+ 1 (* 3.322 (log10 (count data)))))

         bins  (histo-bins (opts->xs opts) bin-count)
         shape (fn [a b v]
                 (svg/line a b
                           {:stroke       (maybe-call stroke v)
                            :stroke-width (maybe-call stroke-width v)}))]
     {:plot   viz/svg-plot2d-cartesian
      :width  width
      :height height
      :graph  (merge (xy-coordinate-system (assoc opts :data bins :x first :y second))
                     {:data [{:values     bins
                              :shape      shape
                              :layout     viz/svg-bar-plot
                              :interleave interleave
                              :bar-width  bar-width}]})})))

(defn scatter-plot [opts]
  (let [{:keys [width height data x y fill fill-opacity stroke stroke-opacity radius]
         :or   {x first
                y second}
         :as   opts}
        (graph-opts opts)]
    {:plot   viz/svg-plot2d-cartesian
     :width  width
     :height height
     :graph  (-> (xy-coordinate-system opts)
                 (assoc :data [{:values   data
                                :shape    (fn [[pos d]]
                                            (svg/circle pos
                                                        (maybe-call radius d 3)
                                                        {:fill (maybe-call fill d "#f93")
                                                         :fill-opacity (maybe-call fill-opacity d)
                                                         :stroke (maybe-call stroke d "none")
                                                         :stroke-opacity (maybe-call fill-opacity d)}))
                                :item-pos (juxt x y)
                                :layout   viz/svg-scatter-plot}]))}))

(defn function-plot
  "Given a function f(x) (given as :fx), draw it as a line. Still expects :data
  / :x / :y to be able to determine the coordinate system.

  It plots 100 points linearly along the x-axis (use :resolution for finer rendering).

  TODO: distribute the plotted points evenly based on the axis scale. The
  current implementation will be suboptimal for log/lens scales.

  Tip: use `overlay` to e.g. draw on top of a scatter-plot."
  [opts]
  (let [{:keys [width height fx]
         :as   opts}
        (graph-opts opts)

        coords        (xy-coordinate-system opts)
        [x-min x-max] (-> coords :x-axis :domain)
        resolution    (:resolution opts (/ (- x-max x-min) 100))]
    {:plot   viz/svg-plot2d-cartesian
     :width  width
     :height height
     :graph  (-> coords
                 (assoc :data [{:values   (range x-min x-max resolution)
                                :item-pos (juxt identity fx)
                                :attribs  {:stroke (:stroke opts "#3385ff")}
                                :layout   viz/svg-line-plot}]))}))

(defn box-plot
  "Draws a box-and-whiskers plot with 1.5*standard deviation whiskers, and
  separate outliers."
  [opts]
  (let [{:keys [width height] :as opts}
        (graph-opts (merge {:left-margin 10
                            :x           identity
                            :y-domain    [0 1]
                            :y-visible   false
                            :height      200}
                           opts))]
    {:plot   viz/svg-plot2d-cartesian
     :width  width
     :height height
     :graph  (assoc (xy-coordinate-system opts)
                    :data [(-> opts
                               (assoc :layout svg-box-plot))])}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Combining

(defn overlay
  "Combine multiple graphs, showing them on a single coordinate system.
  Experimental, doesn't work on all inputs."
  [& graphs]
  (let [axis-merge (fn [ax bx]
                     (update ax :domain (fn [[a-start a-end]]
                                          (let [[b-start b-end] (:domain bx)]
                                            [(min a-start b-start)
                                             (max a-end b-end)]))))]
    (reduce (fn [g h]
              (let [gg          (:graph g)
                    x-axis-fn   (:x-axis-fn gg)
                    y-axis-fn   (:y-axis-fn gg)
                    gh          (:graph h)
                    x-axis-spec (axis-merge (:x-axis-spec gg)
                                            (:x-axis-spec gh))
                    y-axis-spec (axis-merge (:y-axis-spec gg)
                                            (:y-axis-spec gh))
                    x-axis      (x-axis-fn x-axis-spec)
                    y-axis      (y-axis-fn y-axis-spec)
                    data        (into (:data gg) (:data gh))
                    height      (max (:height g) (:height h))
                    width       (max (:width g) (:width h))]
                (assoc (merge g h)
                       :height height
                       :width width
                       :graph (assoc gg
                                     :x-axis x-axis
                                     :y-axis y-axis
                                     :data data
                                     :x-axis-spec x-axis-spec
                                     :y-axis-spec y-axis-spec
                                     :x-axis-fn x-axis-fn
                                     :y-axis-fn y-axis-fn))))
            graphs)))

(declare render*)

(defn hbox
  "Draw multiple graphs side by side. Can be nested/combined with vbox."
  [& graphs]
  {:plot (fn [graphs]
           (transduce identity
                      (fn
                        ([] [0 []])
                        ([[x-pos els] {:keys [width] :as g}]
                         [(+ x-pos width)
                          (conj els (render* (assoc g :x-pos x-pos :y-pos 0)))])
                        ([[_ els]]
                         (apply svg/group {} els)))
                      graphs))
   :width (apply + (map :width graphs))
   :height (apply max (map :height graphs))
   :graph graphs})

(defn vbox
  "Draw multiple graphs top to bottom. Can be nested/combined with hbox."
  [& graphs]
  {:plot (fn [graphs]
           (transduce identity
                      (fn
                        ([] [0 []])
                        ([[y-pos els] {:keys [height] :as g}]
                         [(+ y-pos height)
                          (conj els (render* (assoc g :x-pos 0 :y-pos y-pos)))])
                        ([[_ els]]
                         (apply svg/group {} els)))
                      graphs))
   :width (apply max (map :width graphs))
   :height (apply + (map :height graphs))
   :graph graphs})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Rendering

(defn render*
  "Render a single graph/plot."
  [{:keys [plot graph transform x-pos y-pos]}]
  (svg/group
   (cond-> {}
     (or transform (and x-pos y-pos))
     (assoc :transform (or transform (str "translate(" x-pos " " y-pos ")"))))
   (plot graph)))

(defn with-bg [color body]
  [:g
   [:rect {:width "100%" :height "100%" :fill color}]
   body])

(defn render-svg [spec]
  (->> (render* spec)
       (with-bg (:background spec "white"))
       (svg/svg {:width (some :width [spec *defaults*])
                 :height (some :height [spec *defaults*])})
       (svg/serialize)))

(defn render-to-file [spec svg-file]
  (spit svg-file (render-svg spec)))

(defn render-to-tempfile [spec]
  (let [file (java.io.File/createTempFile (namespace `_) ".svg")]
    (render-to-file spec file)
    file))

(defn render-to-org-link [spec]
  (str "[[file:" (render-to-tempfile spec) "]]"))

(defn open [spec]
  (let [file (render-to-tempfile spec)]
    (browse/browse-url (str "file://" file))))

(defn open-with [spec program]
  (sh/sh program (str (render-to-tempfile spec)))
  nil)
