(ns clojure-experiments.fixed-point)

;;; See SICP and https://stackoverflow.com/questions/44478322/how-do-i-iterate-until-a-fixed-point-in-clojure
;;;

(defn- avg [x y] (/ (+ x y) 2))

(defn abs [x]
  (if (< x 0) (- x) x))

(defn better-good-enough? [old-guess new-guess]
  (< (abs (/ (- old-guess new-guess)
             new-guess))
     0.001))

;; check https://stackoverflow.com/questions/44478322/how-do-i-iterate-until-a-fixed-point-in-clojure
(defn fixed-point [improve-fn start]
  (reduce #(if (better-good-enough? %1 %2)
             ;; using `reduced`
             (reduced %2)
             %2)
          (iterate improve-fn start)))

(defn average-damp [f]
  (fn [x] (avg (f x) x)))

(defn sqrt [x]
  (fixed-point (average-damp (fn [y] (/ x y)))
               1))
(double (sqrt 4))
;; => 2.000000092922295
