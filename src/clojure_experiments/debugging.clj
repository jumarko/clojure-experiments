(ns clojure-experiments.debugging
  "Experiments with debugging code,
  perhaps using Cider debugger: https://docs.cider.mx/cider/debugging/debugger.html"
  (:require [flow-storm.api :as fs-api]
            [clojure.string :as str]))


(defn baz [z]
  (let [zz (+ 10 z)
        zzs (repeat zz z)]
    (mapv inc zzs)))

(defn bar [y]
  (let [yy (+ 5 y)]
    (baz yy)))

(defn foo [x]
  (bar (inc x)))


(comment

  (foo 3)

  ,)


;;; flow-storm-debugger: https://github.com/jpmonettas/flow-storm-debugger/
;;; Intro video: https://www.youtube.com/watch?v=YnpQMrkj4v8
(comment
  ;; will run the debbuger GUI and get everything ready
  (fs-api/local-connect)

  #rtrace (reduce + (map inc (range 10)))

  #rtrace (foo 3)

  ;; Example from the video: https://youtu.be/YnpQMrkj4v8?t=240
  ;; you need to execute this two separately!
  #trace
  (defn factorial [n]
    (if (zero? n) 1 (* n (factorial (dec n)))))

  #rtrace
  (->> (range)
       (filter odd?)
       (take 3)
       (reduce +)
       factorial)


  ;; instrument any function with `fs-api/instrument-var`
  ;; there's also `fs-api/instrument-forms-for-namespaces`
  (fs-api/instrument-var 'clojure.string/join)
  ;; notice you still need to use #rtrace
  #rtrace (str/join "," [1 2 3])

  (fs-api/instrument-forms-for-namespaces #{"clojure.string"} {})

  ;; you can instrument multiple namespaces with a prefix - e.g. clojure.java.
  ;; matches clojure.java.io, clojure.java.shell, clojure.java.browse, clojure.java.javadoc
  ;; BUT  this isn't very smart because it will overwhelm the system
  ;; - clojure.java.io/file is called a lot!
  (comment 
    (fs-api/instrument-forms-for-namespaces #{"clojure.java."} {})
    (require '[clojure.java.javadoc :as javadoc])
    #rtrace (javadoc/javadoc String)
    .)

  .)


