(ns clojure-experiments.puzzles)

;;; What are the problems that beginners face often?
;;; 1. Laziness
;;; 2. 
(def x (map println (range 100)))
;; what does this print?
(take 10 x)
;; what does this print?
(take 10 x)


;;; PurelyFunctional.tv newsletter 11.2.2019
;;;; https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-313-always-use-the-3-argument-version-of-reduce

;; returns number not a string!
(reduce str [1])
;; => 1

;; weird behavior of atom (We don't use futures after all!)
(def counter (atom 0))

(type @counter)

(defn print-counter []
  (let [counter @counter]
    (println "===========")
    (println "| COUNTER |")
    ;; Because this Long is being dereferenced here we get exception
    ;; but it'd be better
    (println (format "| %07d |" @counter))
    (println "==========")))

#_(print-counter)
;;=> Syntax error (ClassCastException) compiling at (puzzles.clj:30:1).
;;   java.lang.Long cannot be cast to java.util.concurrent.Future

