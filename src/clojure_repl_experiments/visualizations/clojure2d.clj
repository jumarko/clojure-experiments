(ns clojure-repl-experiments.visualizations.clojure2d
  (:require [clojure2d.core :as d2]
            [clojure2d.color :as d2c]
            [fastmath.core :as m]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; See https://github.com/Clojure2D/clojure2d
;;;; and examples: https://github.com/Clojure2D/clojure2d-examples/blob/master/src/ex00_helloworld.clj
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;;; https://github.com/Clojure2D/clojure2d-examples/blob/master/src/ex00_helloworld.clj
;; be sure everything is fast as possible
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;; define canvas
(def my-canvas (d2/canvas 600 600))

;; create window
(def window (d2/show-window my-canvas "Hello World!"))

;; draw rectangle with line wrapping with threading canvas context
(d2/with-canvas-> my-canvas ;; prepare drawing context in canvas
  (d2/set-background 10 5 5) ;; clear background
  (d2/set-color 210 210 200) ;; set color
  (d2/rect 100 100 400 400) ;; draw rectangle
  (d2/set-color 50 50 60) ;; set another color
  (d2/set-stroke 2.0) ;; set line width
  (d2/line 50 300 550 300) ;; draw line
  (d2/set-font-attributes 30) ;; set font size
  (d2/set-color :maroon) ;; set current color
  (d2/text "Hello World!" 110 130)) ;; draw line

;; draw dots
(d2/with-canvas [c my-canvas]
  (d2/set-color c :black) ;; set color to black
  (doseq [angle (range 0.0 m/TWO_PI (/ m/TWO_PI 10.0))] ;; draw circles around mid of canvas
    (d2/ellipse c
             (+ 300.0 (* 30.0 (m/sin angle)))
             (+ 300.0 (* 30.0 (m/cos angle)))
             10 10)))

;; saving, save small version of canvas
(d2/save (d2/resize my-canvas 300 300) "results/ex00/helloworld.jpg")

;; [[../results/ex00/helloworld.jpg]]

;; now lets define drawing function
;; type hints to avoid boxed operations
(defn draw
  "Draw rotating rectangle. This function is prepared to be run in refreshing thread from your window."
  [canvas ;; canvas to draw on
   window ;; window bound to function (for mouse movements)
   ^long framecount ;; frame number
   state] ;; state (if any), not used here
  (let [midwidth (* 0.5 ^long (d2/width canvas))] ;; find middle of the canvas

    (-> canvas ;; use canvas (context is already ready! It's draw function.)
        (d2/set-background :linen) ;; clear background with :inen color
        (d2/translate midwidth midwidth) ;; set origin in the middle
        (d2/rotate (/ framecount 100.0)) ;; rotate clockwise (based on number of frame)
        (d2/set-color :maroon) ;; set color to maroon
        (d2/crect 0 0 midwidth midwidth) ;; draw centered rectangle
        (d2/rotate (/ framecount -90.0)) ;; rotate counterclockwise
        (d2/set-color 255 69 0 200) ;; set color to orange with transparency
        (d2/crect 0 0 (* 0.9 midwidth) (* 0.9 midwidth))))) ;; draw smaller rectangle

;; run twice!
(def window (d2/show-window (d2/canvas 600 600) "Rotating square" draw)) ;; create canvas, display window and draw on canvas via draw function (60 fps)

;; save on space pressed (be aware that saving is not synchronized with drawing. Occassional glitches may appear.
(defmethod d2/key-pressed ["Rotating square" \space] [_ _] 
  (d2/save (d2/resize (d2/get-image window) 300 300) "results/ex00/rotating.jpg"))

;; [[../results/ex00/rotating.jpg]]
