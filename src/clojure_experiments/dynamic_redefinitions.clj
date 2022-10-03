(ns clojure-experiments.dynamic-redefinitions)

;;; with-redefs and functions like `inc`
;;; https://clojurians.slack.com/archives/C03S1KBA2/p1664744013312439
;;; with-redefs doesn't work out of the box due to inlining

;; you can redefine dissoc
(with-redefs [conj dissoc] (conj {:a 1} :a))
;; => {}
;; BUT you cannot redefine  inc!!!
(with-redefs [inc dec] (inc 1))
;; => 2
;; ... you can cheat by using the var syntax and derefing it

;; but let's try to disable inlining:
(alter-meta! #'inc dissoc :inline)
(with-redefs [inc dec] (inc 1))
;; => 0
