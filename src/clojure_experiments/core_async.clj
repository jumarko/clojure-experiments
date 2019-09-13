(ns clojure-experiments.core-async
  "Various examples of core.a usage.
  E.g. Brave Clojure tutorial: https://www.braveclojure.com/core-async/"
  (:require [clojure.core.async :as a]))

;; See also `clojure-experiments.books.brave-clojure.core-async`


;;; Experimenting with blocking vs non-blocking puts
;;; ------------------------------------------------

;; blocking put blocks the thread
(def my-chan (a/chan))
;; This blocks because the channel has no buffer
(comment 
  #_(a/>!! my-chan :a)
  ;; notice that `a/>!!` returns `true`
  (def blocking-put-future (future (doto (a/>!! my-chan :a)
                                     println)))
  (a/<!! my-chan)
  ;;=> :a
  @blocking-put-future
  ;;=> true
  ;; 
  )

;; non-blocking put works even when channel has no buffer
;; notice that the result is a channel that will receive the result of the `go` macro body
;; once that's completed => IT'S NOT THE ORIGINAL `my-chan`!
(comment 
  ;; a/>! also returns `true` once it succeeds
  ;; nothing should be printed immediately!
  ;; - it will wait until the next step will take the value from the `my-chan`
  (def result-chan (a/go (doto (a/>! my-chan :a)
                           (println "from go macro"))))

  (a/<!! my-chan)
  ;;=> :a

  ;; notice that this future won't complete until we take from the `my-chan`
  (future (println "from future after go macro: " (a/<!! result-chan)))

  ;; 
  
  )

;; but even non-blocking puts can block the current thread if the 'put queue' is too long?

(comment

  ;; nothing should be printed immediately!
  ;; - it will wait until the next step will take the values from the `my-chan`
  (def n-times 10000)
  (def result-chan (a/go (dotimes [_ n-times]
                           (doto (a/>! my-chan :a)
                             #_(println "from go macro: value taken" )))
                         (println "go macro finished")))

  (dotimes [_ n-times]
    (a/<!! my-chan))
  
  ;;=> :a

  ;; notice that this future won't complete until we take from the `my-chan`
  (future (println "from future after go macro: " (a/<!! result-chan))))



