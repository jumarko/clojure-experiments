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


;;; Eval works via compilation to bytecode and running the bytecode: https://clojurians.slack.com/archives/C03S1KBA2/p1761093620504029?thread_ts=1761092076.285239&cid=C03S1KBA2
;; when you eval something like a date, or some other arbitrary object, it needs to be serialized into something that can be used to reconstruct the object when bytecode runs
;; The way that works is the compiler calls pr-str on the object and stashes the string and when the bytecode executes it calls read-string

(eval (java.util.Date.))
;; => #inst "2025-10-23T03:14:18.844-00:00"

