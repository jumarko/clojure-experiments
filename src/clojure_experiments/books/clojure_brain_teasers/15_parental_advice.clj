(ns clojure-experiments.books.clojure-brain-teasers.15-parental-advice)

(def names ["Scarlet" "Dandelion" "Cerulean"])
(def codes ["FD0E35" "FED85D" "02A4D3"])
(= (map #([%1 %2]) names codes)
   '(["Scarlet" "FD0E35"] ["Dandelion" "FED85D"] ["Cerulean" "02A4D3"]))
;;=>
;; 1. Unhandled clojure.lang.ArityException
;; Wrong number of args (0) passed to: clojure.lang.PersistentVector

;; Explanation:
;; the anonymous function constructs the vector ["Scarlet" "FD0E35"],
;; but the anonymous function then invokes it without any arguments: (["Scarlet" "FD0E35"]).
;; At that point, Clojure throws an ArityException to tell the user
;; that the function (the vector) was called with 0 arguments, which is not a valid arity


;; FIX: use `vector`
(map vector names codes)
;; => (["Scarlet" "FD0E35"] ["Dandelion" "FED85D"] ["Cerulean" "02A4D3"])
