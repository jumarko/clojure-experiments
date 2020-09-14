(ns clojure-experiments.books.joy-of-clojure.ch04-scalars)

;;; 4.1.5 Rounding errors (p. 72)

;; Patriot missile failure => 28 dead soldiers in the first Gulf War

(let [approx-interval (/ 209715 2097152) ; Patrio's approx of 0.1 second
      actual-interval 1/10
      hours (* 3600 100 10) ; rounding error accumulated over 100 hours => 0.34 secs timing error
      actual-total (double (* hours actual-interval))
      approx-total (double (* hours approx-interval))
      ]
  (- actual-total approx-total))
;; => 0.34332275390625
