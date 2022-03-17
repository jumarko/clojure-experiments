(ns clojure-experiments.eval
  "Experiments with `eval`-uation of code.
  Motivated by https://stackoverflow.com/questions/71447267/clojure-memory-leaks-using-eval"
  )


;;; https://stackoverflow.com/questions/71447267/clojure-memory-leaks-using-eval

(comment
  ;; Note: this can be very slow
  ;; Here are approximate durations for various number of elements:
  ;; - 1000 items => "Elapsed time: 315.882502 msecs"
  ;; - 10000 items => "Elapsed time: 3272.799276 msecs"
  ;; - 100000 items => "Elapsed time: 61999.405319 msecs"
  ;; - 1000000 items => "Elapsed time: 529079.032449 msecs"
  (time (dotimes [x 1000000] ; or some arbitrary large number
          (eval '(+ 1 1))))
  ;; "Elapsed time: 529079.032449 msecs"

,)
