(ns clojure-experiments.books.sicp.ch2-abstractions-data.s2-picture-language
  "Subsection of Chapter 2.2 - Picture language.
  See the book p. 126 - 140."
  (:require [clojure-experiments.books.sicp.ch2-abstractions-data.s1-intro :as s1]
            [clojure.spec.alpha :as s]))


;;; First define basic combinations
(defn beside [x y]) ; places first painter in the left half
(defn below [x y]) ; places first painter below the second one
(defn wave [])
(defn flip-vert [x])
(defn flip-horiz [x])

;;; then we can start combining them
(def wave2 (beside wave (flip-vert wave)))
(def wave4 (below wave2 wave2))

;;; Abstracting more patterns... (p. 130)
(defn flipped-pairs [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

;; and we can redefine wave4 using `flipped-pairs`
(defn flipped-pairs [x])
(def wave4 (flipped-pairs wave))

;;; We can also define recursive operations (p.131 & 132)
;;; Note: the pictures in the book are helpful for understanding what these operations do
(defn right-split [painter n]
  (if (zero? n)
    painter
    (let [smaller (right-split painter (dec n))]
      (beside painter
              (below smaller smaller)))))

;; notice this uses `up-split` which hasn't been defined yet (ex. 2.44)
(defn up-split [n])
(defn corner-split [painter n]
  (if (zero? n)
    painter
    (let [up-split-1 (up-split (dec n))
          right-split-1 (right-split (dec n))
          up (beside (beside up-split-1 up-split-1)
                     (corner-split (dec n)))
          bottom (beside (painter (below right-split-1 right-split-1)))]
      (below up bottom))))

  ;; and finally we can define square-limit (see p. 127 and p.132)
(defn square-limit [painter n]
  (let [quarter (corner-split painter n)
        half (beside (flip-horiz quarter) quarter)]
    (below (flip-vert half) half)))

  ;; Ex. 2.44 (p. 132)
  ;; Define the `up-split` procedure
(defn up-split [painter n]
  (if (zero? n)
    painter
    (let [up-half (up-split (dec n))]
      (below painter
             (beside up-half up-half)))))


;;; Higher-order operations (p. 132)
;;; - procedures that take painter operations as arguments and create new painter operations

;; E.g. flipped-pairs and square-limit each arrange four copies of apinter's image in square pattern;
;; they differ only in how they orient the copies.
;; => We can abstract this pattern with the square-of-four procedure
(defn square-of-four [tl tr bl br]
  (fn sof [painter]
    (let [top (beside (tl painter) (tr painter))
          bottom (beside (bl painter) (br painter))]
    (below bottom top))))

;; now we can define flipped-pairs in terms of `square-of-four`
(defn flipped-pairs [painter]
  (square-of-four painter (flip-vert painter)
                  painter (flip-vert painter)))
;; or even easier!!
;; Note: this is incorrect
;; (def flipped-pairs identity flip-vert identity flip-vert)

;; and now redefine `square-limit` using `square-of-four`:
(declare rotate180)
(declare squre-of-four)
(defn square-limit [painter n]
  (let [combine4 (square-of-four flip-horiz identity
                                 rotate180 flip-vert)]
    (combine4 (corner-split painter n)))
  )


;;; Ex. 2.45 general `split` procedure (p. 134)
;;; `right-split` and `up-split` can be expressed as instances of `split`:
;;; TODO: define the `split` procedure
(defn split [orig-placer split-placer]
  (fn split-n [painter n]
    (if (zero? n)
      painter 
      (let [smaller (split-n painter (dec n))]
        (orig-placer painter
                     (split-placer smaller smaller))))))

(def right-split (split beside below))
(def up-split (split below beside))


;;; Frames (p. 134 - 136)
;;; Frames are represented by 3 vectors
;;; - 'origin vector' represents the relative position of the "origin of the frame"
;;;   from some absolute origin (0,0)
;;; - 'edge1 and e2 vectors' represent edges of the frame (can be rectangular or a general parallelogram)
;;;
;;; 'frame coordinate map' maps unit square (used to specify images)
;;; to the frame as follows:
;;;    Origin(Frame) + x * Edge1(Frame) + y * Edge2(Frame)

;; to start with we only need "constructor" and "selectors"
(declare make-frame)
(declare origin-frame)
(declare edge1-frame)
(declare edge2-frame)

;; for frame-coord-map we also need vector operations
(declare add-vect)
(declare scale-vect)
(declare xcor-vect)
(declare ycor-vect)

(defn frame-coord-map [frame]
  (fn [a-vector]
    (add-vect (origin-frame frame)
              (add-vect (scale-vect (xcor-vect a-vector) (edge1-frame frame))
                        (scale-vect (ycor-vect a-vector) (edge1-frame frame))))))

;; Ex. 2.46 (p. 136)
;; Implement two-dimensional vector v running from the origin to a pint
;; with a pair consisting of x and y corrdinates.
;; Create an abstraction via `make-vect`, `xcor-vect` and `ycor-vect`
;; Then implement `add-vect`, `sub-vect`, `scale-vect`
(defn make-vect [x y]  [x y])
(defn xcor-vect [[x _]] x)
(defn ycor-vect [[_ y]] y)

(ycor-vect (make-vect 10 -100))
;; => -100

(defn add-vect [v1 v2]
  (make-vect
   (+ (xcor-vect v1) (xcor-vect v2))
   (+ (ycor-vect v1) (ycor-vect v2))))
(add-vect (make-vect 1 100) (make-vect 10 -100))
;; => [11 0]

(defn sub-vect [v1 v2]
  (make-vect
   (- (xcor-vect v1) (xcor-vect v2))
   (- (ycor-vect v1) (ycor-vect v2))))
(sub-vect (make-vect 1 100) (make-vect 10 -100))
;; => [-9 200]

(defn scale-vect [scalar v]
  (make-vect
   (* scalar (xcor-vect v))
   (* scalar (ycor-vect v))))
(scale-vect 5 (make-vect 10 -100))
;; => [50 -500]

;; Now I'll try to reimplement add-vect and sub-vect using a more general procedure `combine-vect`
(defn combine-vect [op v1 v2]
  (make-vect
   (op (xcor-vect v1) (xcor-vect v2))
   (op (ycor-vect v1) (ycor-vect v2))))
(def add-vect (partial combine-vect +))
(def sub-vect (partial combine-vect -))
(add-vect (make-vect 1 100) (make-vect 10 -100))
;; => [11 0]
(sub-vect (make-vect 1 100) (make-vect 10 -100))
;; => [-9 200]


;; Ex. 2.47 (p. 136)
;; Provide selectors for given constructors:

;; 1st constructor
(defn make-frame [origin edge1 e2]
  (list origin edge1 e2))
(defn origin-frame [f]
  (first f))
(defn edge1-frame [f]
  (second f))
(defn edge2-frame [f]
  (nth f 3))

;; 2nd possible constructor
(defn make-frame [origin edge1 e2]
  (cons origin (cons edge1 e2)))
(defn origin-frame [f]
  (first f))
(defn edge1-frame [f]
  (second f))
(defn edge2-frame [f]
  (nnext f))

#_(edge2-frame (make-frame
  (make-vect 5 5)
  (make-vect 2 9)
  (make-vect 10 9)))


;;; Painters (p. 136)
;;; Painter is a procedure that given a fame as arg, draws a particular image
;;; shifted and scaled to fit the frame.

;; DRAWING EXPERIMENTS - INCOMPLETE and NOT WORKING PROPERLY!
;; suppose we have primitive procedure `draw-line` which draws a line on the screen
;; between two specified points;
;; then we can create painters for line drawings
;; (here we also copy start-segment and end-segment from Chapter2, Section 1.)

;; <this was copied from clojure.inspector/inspect>
(import (javax.swing JPanel JFrame))
(defn swing-graphics
  "Calls given function with the Graphics object of a fresh JPanel visible in  JFrame."
  ([f]
   (swing-graphics f 400 400))
  ([f width heigth]
   (doto (JFrame. "Picture Language")
     (.add
      (proxy [JPanel] []
        (paintComponent [graphics]
          (proxy-super paintComponent graphics)
          (f graphics))))
     (.setSize width heigth)
     (.setVisible true))))

(defn draw-line [graphics start end]
  (.drawLine graphics
             (xcor-vect start) (ycor-vect start)
             (xcor-vect end) (ycor-vect end)))

#_(swing-graphics
   (fn [g]
     (draw-line g
                (make-vect 0 0)
                (make-vect 50 100)
                )
     ))

(defn- draw-frame [g frame]
  (let [start (origin-frame frame)
        [x1 y1] ((juxt xcor-vect ycor-vect) start)
        end (origin-frame frame)
        [x2 y2] ((juxt xcor-vect ycor-vect) end)]
    (.drawRect g x1 y1 x2 y2)))

(defn segments->painter [segment-list]
  (fn [frame]
    (swing-graphics
     (fn [g]
       ;; draw frame borders to make it more intuitive
       (draw-frame g frame)
       (run!
        (fn [segment]
          (draw-line g
                     ((frame-coord-map frame) (s1/start-segment segment))
                     ((frame-coord-map frame) (s1/end-segment segment))))
        segment-list))
     1000
     1000
     )))

;; main challenge is translation between (0,1) coordinates to pixels
#_((segments->painter
  [(s1/make-segment (s1/make-point 0 0) (s1/make-point 0.3 0.5))
   #_(s1/make-segment (s1/make-point 100 400) (s1/make-point 200 50))
   #_(s1/make-segment (s1/make-point 10 80) (s1/make-point 200 220))
   
   ])
 (make-frame
  (make-vect 50 50)
  (make-vect 200 90)
  (make-vect 10 90)))



;;; Ex 2.48
(defn make-segment [start end]
  [start end])
(def start-segment first)
(def end-segment second)

;;; Ex. 2.49 (p. 137)
;; a. The painter that draws the outline of the designated frame
(defn frame-painter [frame]
  (let [a (origin-frame frame)
        e1 (edge1-frame frame)
        e2 (edge2-frame frame)
        b (add-vect a e1)
        c (add-vect b e2)
        d (add-vect a e2)]
    (segments->painter
     [(make-segment a b)
      (make-segment b c)
      (make-segment c d)
      (make-segment d a)])))


;;; Transforming and combining painters (p. 138)
;;; flip-vert  doesn't need to know how painters work,
;;; just how to turn a frame upside down.

(defn transform-painter
  "Produced a new painter which calls the original painter
  on a transformed frame defined by new origin and corners."
  [painter origin corner1 corner2]
  (fn [frame]
    (let [m (frame-coord-map frame)
          new-origin (m origin)
          new-corner1 (sub-vect (m corner1) new-origin)
          new-corner2 (sub-vect (m corner2) new-origin)]
      (painter
       (make-frame new-origin new-corner1 new-corner2)))))

;; we can then define flip-vert easily
(defn flip-vert [painter]
  (transform-painter painter
                     (make-vect 0.0 1.0) ; new origin
                     (make-vect 1.0 1.0) ;new end of edge1
                     (make-vect 0.0 0.0) ;new end of edge1
                     ))

;; we can also shrink to upper right corner
(defn shrink-to-upper-right [painter]
  (transform-painter painter
                     (make-vect 0.5 0.5)
                     (make-vect 1.0 0.5 )
                     (make-vect 0.5 1.0)))


(defn rotate90 [painter]
  (transform-painter painter
                     (make-vect 1.0 0.0)
                     (make-vect 1.0 1.0)
                     (make-vect 0.0 0.0)))

;; or squash towards the center of the frame
(defn squash-inwards [painter]
  (transform-painter painter
                     (make-vect 0.0 0.0)
                     (make-vect 0.65 0.35)
                     (make-vect 0.35 0.65)))

;; and finally beside
(defn beside [painter1 painter2]
  (let [split-point (make-vect 0.5 0.0)
        paint-left (transform-painter painter1
                                      (make-vect 0.0 0.0)
                                      split-point
                                      (make-vect 0.0 1.0))
        paint-right (transform-painter painter2
                                       split-point
                                       (make-vect 1.0 0.0)
                                       (make-vect 0.5 1.0))]
    (fn [frame]
      (paint-left frame)
      (paint-right frame))))


;;; Ex. 2.50 (p. 140)
;;; Define the transformation flip-horiz which flips painters horizontally
;;; and transformations that rotate painters counterclockwise by 180 degrees
;;; and 270 degrees.
(defn flip-horiz
  "Flips the painter horizontally."
  [painter]
  (transform-painter painter
                     (make-vect 1.0 0.0) ; new origin
                     (make-vect 0.0 0.0) ;new end of edge1
                     (make-vect 1.0 1.0) ;new end of edge2
                     ))

(defn rotate180 [painter]
  (transform-painter painter
                     (make-vect 1.0 1.0) ; new origin
                     (make-vect 0.0 1.0) ;new end of edge1
                     (make-vect 1.0 0.0) ;new end of edge2
                     ))
(defn rotate270 [painter]
  (transform-painter painter
                     (make-vect 0.0 1.0) ; new origin
                     (make-vect 0.0 0.0) ;new end of edge1
                     (make-vect 1.0 1.0) ;new end of edge2
                     ))

;;; Ex. 2.51 (p. 140 & p.131)
;;; Define below operation
;;; Draw it using two different ways
;;; 1. analogous to the `beside` implementation
;;; 2. using `beside` and rotations from Ex 2.50

;; 1. as `beside`
(defn below1 [painter-bottom painter-top]
  (let [split-point (make-vect 0.0 0.5)
        paint-bottom (transform-painter painter-bottom
                                      (make-vect 0.0 0.0)
                                      (make-vect 0.0 1.0)
                                      split-point)
        paint-top (transform-painter painter-top
                                       split-point
                                       (make-vect 1.0 0.5)
                                       (make-vect 0.0 1.0))]
    (fn [frame]
      (paint-bottom frame)
      (paint-top frame))))


;; 2. using `beside` and rotations
(defn below2 [painter-bottom painter-top]
  (rotate90
   (beside (rotate270 painter-top) (rotate270 painter-bottom))))
;; or 
(defn below2 [painter-bottom painter-top]
  (rotate270
   (beside (rotate90 painter-top) (rotate90 painter-bottom))))
