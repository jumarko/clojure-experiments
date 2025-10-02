(ns clojure-experiments.books.clojure-brain-teasers.04-truthy)

(true? true)
;; => true

(true? :ski-is-blue)
;; => false

(false? false)
;; => true

(false? nil)
;; => false

(false? '())
;; => false

(false? 0)
;; => false


;;; Example: 3 equivalent ways to express the same thing.
(def moms-birthday "April 20, 1969")
(when-not (nil? moms-birthday)
  (println "Happy Birthday Mom!!"))
(when (some? moms-birthday)
  (println "Happy Birthday Mom!!"))
;; this one is preferred
(when moms-birthday
  (println "Happy Birthday Mom!!"))
