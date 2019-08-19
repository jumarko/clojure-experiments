(ns four-clojure.intro-to-some)

;;; Intro to some: http://www.4clojure.com/problem/48

(= 6  (some #{2 7 6} [5 6 7 8]))

(= 6 (some #(when (even? %) %) [5 6 7 8]))
