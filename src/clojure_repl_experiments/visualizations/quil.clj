(ns clojure-repl-experiments.visualizations.quil
  (:require [quil.core :as q]))

;;; https://github.com/quil/quil

(defn setup []
  (q/frame-rate 1)                    ;; Set framerate to 1 FPS
  (q/background 200))                 ;; Set the background colour to
                                      ;; a nice shade of grey.

(defn draw []
  (q/stroke (q/random 255))             ;; Set the stroke colour to a random grey
  (q/stroke-weight (q/random 10))       ;; Set the stroke thickness randomly
  (q/fill (q/random 255))               ;; Set the fill colour to a random grey

  (let [diam (q/random 100)             ;; Set the diameter to a value between 0 and 100
        x    (q/random (q/width))       ;; Set the x coord randomly within the sketch
        y    (q/random (q/height))]     ;; Set the y coord randomly within the sketch
    (q/ellipse x y diam diam)))         ;; Draw a circle at x y with the correct diameter

(comment

  (q/defsketch example                  ;; Define a new sketch named example
    :title "Oh so many grey circles"    ;; Set the title of the sketch
    :settings #(q/smooth 2)             ;; Turn on anti-aliasing
    :setup setup                        ;; Specify the setup fn
    :draw draw                          ;; Specify the draw fn
    :size [323 200])                    ;; You struggle to beat the golden ratio
)
