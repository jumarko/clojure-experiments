(ns clojure-experiments.purely-functional.core-async.core
  "Examples from the basic course: https://purelyfunctional.tv/courses/clojure-core-async/
  Check the code: https://github.com/ericnormand/lispcast-clojure-core-async"
  (:require [clojure.core.async :as a]
            [clojure.string :as string]))


;;; Assembly line
;;; https://purelyfunctional.tv/lesson/assembly-line-coordinating-between-processes/

;; https://purelyfunctional.tv/lesson/assembly-line-coordinating-between-processes/
;; Ex. 3 
(let [ch (a/chan)]
  (a/go (dotimes [i 5]
          (a/>! ch (rand-int 100))))
  (a/go (while true 
          (let [val (a/<! ch)]
            (Thread/sleep 1000)
            (println "Got: " val)))))


;; Ex. 4
;; generate 10 lowercase letters, convert them to uppercase, conver them to a string, print them
(let [char-chan (a/chan)
      up-chan (a/chan)
      str-chan (a/chan)]
  (a/go (dotimes [_ 10] (a/>! char-chan (char (+ 92 (rand-int 25))))))
  (a/go (while true (a/>! up-chan (Character/toUpperCase (a/<! char-chan)))))
  (a/go (while true (a/>! str-chan (str (a/<! up-chan)))))
  (a/go (while true (println (a/<! str-chan)))))


;; Ex. 5 - go block for each of the seven steps in the assembly line
;; Check https://github.com/ericnormand/lispcast-clojure-core-async/blob/master/src/lispcast_clojure_core_async/core.clj
(defn assembly-line [] ,,,)


;;; Conveyor Belts (Channels)
;;; https://purelyfunctional.tv/lesson/conveyor-belts-communicating-using-channels/
(let [ch (a/chan 5)]
  (a/go (dotimes [i 10]
        (a/>! ch i)
        (println i))))

;;; Ex. 2 (add fixed buffers to all channels in assemly line)
;;; (lot of work, not done.)
