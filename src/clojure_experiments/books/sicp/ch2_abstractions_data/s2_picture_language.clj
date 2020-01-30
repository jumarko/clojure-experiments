(ns clojure-experiments.books.sicp.ch2-abstractions-data.s2-picture-language
  "Subsection of Chapter 2.2 - Picture language.
  See the book p. 126 - 140.")


;;; First define basic combinations
(declare beside) ; places first painter in the left half
(declare below) ; places first painter below the second one
(declare wave)
(declare flip-vert)
(declare flip-horiz)

;;; then we can start combining them
(def wave2 (beside wave (flip-vert wave)))
(def wave4 (below wave2 wave2))

;;; Abstracting more patterns... (p. 130)
(defn flipped-pairs [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

;; and we can redefine wave4 using `flipped-pairs`
(declare flipped-pairs)
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
(declare up-split)
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
           (below smaller smaller))))))

(def right-split (split beside below))
(def up-split (split below beside))
