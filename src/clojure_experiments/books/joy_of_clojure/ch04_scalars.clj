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


;;; 4.4 Symbolic resolution & metadata

;; equally named symbols often aren't the same because they can have different metadata
;; notice that symbol equality depends on neither equality nor metadata:
(let [x (with-meta 'goat {:ornery true})
      y (with-meta 'goat {:ornery false})]
  [(= x y)
   (identical? x y)
   (meta x)
   (meta y)])
;; => [true false {:ornery true} {:ornery false}]




;;; Regular expressions

;; comments and whitespaces are ignored
(def with-comment #"(?x)   [Hh]ello, #something everybody knows
                           [Ww]orld!")

(re-find with-comment "Hello,World!")
;; => "Hello,World!"

;; multiline regex `m` modifier switches ^ and $ to match end of lines instead of end of the regex
(def multi-line #"(?m)Hello,World$")
;; this wouldn't match without `(?m)`
()re-find multi-line "Hello,World
ahoj
hi")
;; => "Hello,World"
