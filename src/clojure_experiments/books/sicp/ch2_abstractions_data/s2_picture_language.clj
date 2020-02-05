(ns clojure-experiments.books.sicp.ch2-abstractions-data.s2-picture-language
  "Subsection of Chapter 2.2 - Picture language.
  See the book p. 126 - 140.")


;;; First define basic combinations
(defn beside) ; places first painter in the left half
(defn below) ; places first painter below the second one
(defn wave)
(defn flip-vert)
(defn flip-horiz)

;;; then we can start combining them
(def wave2 (beside wave (flip-vert wave)))
(def wave4 (below wave2 wave2))

;;; Abstracting more patterns... (p. 130)
(defn flipped-pairs [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

;; and we can redefine wave4 using `flipped-pairs`
(defn flipped-pairs)
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
(defn up-split)
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
(def flipped-pairs identity flip-vert identity flip-vert)

;; and now redefine `square-limit` using `square-of-four`:
(defn square-limit [painter n]
  (let [combine4 (square-of-four flip-horiz identity
                                 rotate180 flip-vert)]
    (combine4 (cornert-split painter n)))
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
;;; - 'edge1 and edge2 vectors' represent edges of the frame (can be rectangular or a general parallelogram)
;;;
;;; 'frame coordinate map' maps unit square (used to specify images)
;;; to the frame as follows:
;;;    Origin(Frame) + x * Edge1(Frame) + y * Edge2(Frame)

;; to start with we only need "constructor" and "selectors"
(defn make-frame)
(defn origin-frame)
(defn edge1-frame)
(defn edge2-frame)

;; for frame-coord-map we also need vector operations
(defn add-vect)
(defn scale-vect)
(defn xcor-vect)
(defn ycor-vect)

(defn frame-coord-map [frame]
  (fn [a-vector]
    (add-vect (origin-frame frame)
              (add-vect (scale-vect (xcor-vect a-vector) (edge1-frame))
                        (scale-vect (ycor-vect a-vector) (edge1-frame))))))

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
(defn make-frame [origin edge1 edge2]
  (list origin edge1 edge2))
(defn origin-frame [f]
  (first f))
(defn edge1-frame [f]
  (second f))
(defn edge2-frame [f]
  (nth f 3))

;; 2nd constructor
(defn make-frame [origin edge1 edge2]
  (cons origin (cons edge1 edge2)))
(defn origin-frame [f]
  (first f))
(defn edge1-frame [f]
  (first (second f)))
(defn edge2-frame [f]
  (second (second f)))
