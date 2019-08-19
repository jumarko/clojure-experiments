(ns four-clojure.half-truth)

;;; http://www.4clojure.com/problem/83
;;; Write a function that takes variable number of booleans.
;;; Your function should return true if some of the parameters are true, but not all of the parameters are true.
;;; Otherwise your function should return false

(defn half-truth [& args]
  (and
   (not-every? true? args)
   ;; notice that we can't use the resut of "some" directly since it returns nil in case the condition is not satisfied
   (true? ( some true? args))))

;; simplest solution is probably following
(defn half-truth [& args]
  (and
   (not-every? true? args)
   (not-every? false? args)))


(= false (half-truth false false))

(= true (half-truth true false))

(= false (half-truth true))

(= true (half-truth false true false))

(= false (half-truth true true true))

(= true (half-truth true true true false))
