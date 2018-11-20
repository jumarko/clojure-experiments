(ns clojure-repl-experiments.visualizations.quil
  (:require [quil.core :as q]
            [quil.middleware :as qm]
            [quil.middlewares.bind-output :as qout]))

;;; http://nbeloglazov.com/2014/05/29/quil-intro.html
;; define function which draws spiral
(defn draw []
  ;; make background white
  (q/background 255)

  ;; move origin point to centre of the sketch
  ;; by default origin is in the left top corner
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
    ;; parameter t goes 0, 0.01, 0.02, ..., 99.99, 100
    (doseq [t (range 0 100 0.01)]
      ;; draw a point with x = t * sin(t) and y = t * cos(t)
      (q/point (* t (q/sin t))
               (* t (q/cos t))))))

;; run sketch
(defn setup []
  (q/frame-rate 60)
  (q/background 255))

(def window-width 500)
(def window-height 500)

(comment
  (q/defsketch trigonometry
    :setup setup
    :size [window-width window-height]
    :draw draw)

  )

;; Now let's refactor draw function making plotting functions easier. 
(defn f [t]
  [(* t (q/sin t))
   (* t (q/cos t))])

(defn draw-plot [f from to step]
  (doseq [two-points (->> (range from to step)
                          (map f)
                          (partition 2 1))]
                                        ; we could use 'point' function to draw a point
                                        ; but let's rather draw a line which connects 2 points of the plot
    (apply q/line two-points)))

(defn draw []
  (q/background 255)
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
    (draw-plot f 0 100 0.01)))

;; Live Reloading:
;; After we changed code we don't need to close sketch, recompile everything and start sketch again
;; as we would do in most other languages. In quil we can update all functions on the fly
;; and see results immediately. In fact we can program whole sketch from the beginning to the end
;; without ever closing it.
;; Of course there are some things we can't do on the fly. We can't register mouse
;; and keyboard listeners on the fly, but we still can update already registered.

;; => let's update `f` function:
;; you can get awesome plots using random combinations of trigonometric functions
;; here f which plots a flower
(defn f [t]
  (let [r (* 200 (q/sin t) (q/cos t))]
    [(* r (q/sin (* t 0.2)))
     (* r (q/cos (* t 0.2)))]))



;;; http://quil.info/sketches/show/example_matrix

(def katakana "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン")
(def glyph-size 12)
(def min-drops 1)
(def max-drops 30)

(defn gen-raindrop []
  {:text (apply str (shuffle katakana))
   :index -1
   :x ((fn [x] (- x (mod x glyph-size)))
       (rand (q/width)))
   :eol true})

(defn setup []
  (q/frame-rate 20)
  (q/background 0)

  (repeatedly min-drops gen-raindrop))

(defn update-raindrop [raindrop]
  (let [next-index  (inc (:index raindrop))
        eol         (>= next-index (count (:text raindrop)))]
    (assoc raindrop :index next-index :eol eol)))

(defn regen-raindrop [raindrop]
  (if (and (:eol raindrop) (< (rand) 0.1))
    (gen-raindrop)
    raindrop))

(defn spawn-raindrop [state]
  (if (and (< (count state) max-drops) (< (rand) 0.1))
    (cons (gen-raindrop) state)
    state))

(defn update-state [state]
  (def dbg-state state)
  (->> state
       (map (comp update-raindrop regen-raindrop))
       (spawn-raindrop)))

(defn draw-state [state]
  ; draw transparent backdrop
  (q/fill 0 10)
  (q/rect 0 0 (q/width) (q/height))

  ; draw text raindrop
  (q/fill 0 255 0)
  (doseq [raindrop state]
    (let [c (.charAt ^String (:text raindrop) (:index raindrop))
          y (* glyph-size (:index raindrop))]
      (if-not(:eol raindrop)
        (q/text c (:x raindrop) y)))))

(comment 
  ;; doesn't work: java.lang.String cannot be cast to java.util.Collection
  ;; lines 108 -> 112 -> 103 -> 105 -> 81

  (q/defsketch matrix
    :host "host"
    :size [800 800]
    :setup setup
    :update update-state
    :draw draw-state
    :middleware [qout/bind-output qm/pause-on-error qm/fun-mode])


  )
