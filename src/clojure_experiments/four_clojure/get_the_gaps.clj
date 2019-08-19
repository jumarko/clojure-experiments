(ns four-clojure.get-the-gaps)

;;; http://www.4clojure.com/problem/29
;;; Write a function which takes a string and returns new string containing only capital letters

(defn only-capitals [s]
  (apply str (re-seq #"[A-Z]{1}" s)))

;; alternative solution
(defn only-capitals [word] (apply str (filter #(Character/isUpperCase %) word)))

(= (only-capitals "HeLlO WoRlD!") "HLOWRD")

(empty? (only-capitals "nothing"))

(= (only-capitals "$#A(*&987Zf") "AZ")
