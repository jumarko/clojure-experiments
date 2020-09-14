(ns clojure-experiments.books.joy-of-clojure.ch02-clojure-fire-hose
  "Chapter 2: Dring from the Clojure Fire Hose.")

;;; p. 26 - 29: Scalars

;; binary numbers
2r1001
;; => 9


;; multi-line strings
"this is also a
             String"
;; => "this is also a\n             String"


;;; Functions

;; variable arguments
(defn arity2+ [first second & more]
  (vector first second more))

(arity2+ 1 2)
;; => [1 2 nil] ; Notice nil!

(arity2+ 1 2 3 4)
;; => [1 2 (3 4)]


;;; Recursion and loops
;;; Because local variables in Clojure can't be mutated,
;;; the classic way to implement a loop is a recursive call

;; recur
(defn print-down-from [x]
  (when (pos? x)
    (println x)
    (recur (dec x))))

(print-down-from 4)

;; you can use loop-recur to avoid having to provide initial value for an accumulator
(defn sum-down-from [x]
  (loop [sum 0
         x x]
    (if (pos? x)
      (recur (+ sum x) (dec x))
      sum)))

(sum-down-from 10)
;; => 55


;;; Evaluation and Quoting

;; to prevent things from evaluating we can use single quote
'(1 (+ 2 3))
;; => (1 (+ 2 3))

;; previous example doesn't quite give us what we want so we can use syntax quote:
`(1 ~(+ 2 3))
;; => (1 5)

;; but notice this difference
#_`(1 ~(2 3)) ; this fails with ClassCastException
;; vs.
(let [x '(2 3)]
  `(1 ~x))
;; => (1 (2 3))

;; unquote splicing can help to get proper list (1 2 3)
(let [x '(2 3)]
  `(1 ~@x))
;; => (1 2 3)

