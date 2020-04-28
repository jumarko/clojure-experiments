(ns clojure-experiments.stats.python
  (:require [fastmath.vector :as fmv]))

;;; compare NumPy ndarray operations performance with clojure
;;; See 'Python for Data Analysis' jupyter notebooks
;;; See also
;;;  - 'Fast, Native Speed, Vector Computations in Clojure': https://neanderthal.uncomplicate.org/articles/tutorial_native.html
;;;  - Matrix Multiplication in Clojure vs Numpy: https://stackoverflow.com/questions/8899773/matrix-multiplication-in-clojure-vs-numpy

;; Numpy:
;; my_arr = numpy.arange(1000000)
;; %time for _ in range(10): my_arr2 = my_arr * 2
;; CPU times: user 14.5 ms, sys: 8.33 ms, total: 22.8 ms

;; Clojure
;; Using vectors NumPy seems to be more than 10x faster!
;; using primitive vector via `vector-of` also doesn't help
(def my-vec (vec (range 1000000)))
(time
 (dotimes [i 10]
   (mapv #(* % 2)
         my-vec)))
"Elapsed time: 304.762938 msecs"
;; and we 10^8 elements it's super slow!!
;; this is time for doing 10 iterations:
"Elapsed time: 57803.262685 msecs"
;; But as soon as we're in reasonable limits of JVM heap it grows ~linearly:
(comment
  (def my-big-vec (vec (range 5e7)))
  (dotimes [i 1]
    (time
     (mapv #(* % 2)
           my-big-vec))))
;; 50,000,000 elements take 2 seconds;
;; compare to 1,000,000 elements taking 30 ms: 30ms * 50 = 1.5 seconds (almost 2 seconds)
"Elapsed time: 2045.849381 msecs"

;; Using arrays => still not great
(def my-array (int-array (range 1000000)))
(time
 (dotimes [i 10]
   (amap ^ints my-array
         idx
         ret
         (* (int 2) (aget ^ints my-array idx)))))
"Elapsed time: 133.522233 msecs"
;; with 10^8 elements it's surprisingly slow


;; Try fastmath
(time
 (dotimes [i 10]
   (fmv/mult my-vec 2)))
;; => unfortunately, not faster :(
"Elapsed time: 285.1403 msecs"


(comment
  (def my-big-array (int-array (range 100000000)))
  (time
   (amap ^ints my-big-array
         idx
         ret
         (* (int 2) (aget ^ints my-big-array idx)))))
