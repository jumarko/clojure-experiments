(ns four-clojure.regular-expressions)

;; Example:
(re-seq #"jam" "I like jam in my jam ")

(= "ABC" (apply str (re-seq #"[A-Z]+" "bA1B3Ce")))
