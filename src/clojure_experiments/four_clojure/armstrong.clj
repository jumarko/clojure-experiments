(ns clojure-experiments.four-clojure.armstrong
  (:require [clojure.string :as str]))

;;; From clojurians slack

;; Guillaume De Lazzer  Hi, I am having trouble working with big numbers (bigint/long),
;; I am working through the exercism.io exercices and I am trying to do the armstrong problem
;; Here are 2 examples of how I am trying to solve it, but I never get the right results, and I don’t understand why:
;; The expected result should be 21897142587612075.
;; Would someone be able to explain me what am I doing wrong and why I get 2 different results?

(def a (str/split (str 21897142587612075) #""))
(long (reduce +' (map #(Math/pow (Integer/parseInt %) (count a)) a)))
;; => 21897142587612072
(reduce + (map #(long (Math/pow (Integer/parseInt %) (count a))) a))
;; => 21897142587612074

;; my version => using BigInteger.pow: https://stackoverflow.com/questions/8071363/calculating-powers-of-integers
;;   https://docs.oracle.com/javase/6/docs/api/java/math/BigInteger.html#pow%28int%29
(def digits (str 21897142587612075) )
(def exponent (int (count digits)))
(reduce + (map (fn [digit]
                 (.pow (BigInteger/valueOf (Integer/parseInt (str digit)))
                       exponent))
               digits))
;; => 21897142587612075N
