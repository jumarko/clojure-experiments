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
;;; 
;;; FlowStorm 2.2 new features demo: https://www.reddit.com/r/Clojure/comments/v9z7ob/flowstorm_22_new_features_demo/
;;; - https://www.youtube.com/watch?v=cnLwRzxrKDk

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


;;; debugging macro that can save the local context
(defmacro locals []
  (let [ks (keys &env)]
    `(let [ls# (zipmap '~ks [~@ks])]
       (println "====================== DEBUG locals =======================")
       (clojure.pprint/pprint ls#)
       (println "====================== END DEBUG locals =======================")
       (def my-locals (with-meta ls#
                        {:created (java.util.Date.)})))))

(defn my-function [x y z]
  (let [a (* x y z)
        b (inc a)
        c (/ b 10)]
    (locals)
    (->> (range b)
         (map inc)
         (filter odd?))))
(my-function 3 4 5)
my-locals
;; => {x 3, y 4, z 5, a 60, b 61, c 61/10}

;; nested functions?
(defn my-function [x y z]
  (let [a (* x y z)
        b (inc a)
        c (/ b 10)
        myf (fn my-nested-function [m n]
              (locals))
        res (myf 100 200)]
    (->> (range b)
         (map inc)
         (filter odd?))))
(my-function 3 4 5)
my-locals
;; => {x 3,
;;     a 60,
;;     my-nested-function #function[clojure-experiments.debugging/my-function/my-nested-function--28259],
;;     y 4,
;;     n 200,
;;     m 100,
;;     c 61/10,
;;     z 5,
;;     b 61}


;; TODO: expand-locals macro
;; - what's wrong?

;; first attempt
(defmacro expand-locals [bindings-map & body]
  `(let [~@(mapcat identity bindings-map)]
     ~@body))
;; this works - notice not quoting project-id inside the map
(expand-locals {project-id 100}
               project-id)
;; => 100

;; ... BUT this doesn't work - I have to quote because it's defined before the macro invocation
(def mylocals {'project-id 100})
#_(expand-locals my-locals (do project-id))
;; 1. Caused by java.lang.IllegalArgumentException
;;    Don't know how to create ISeq from: clojure.lang.Symbol

;; transform the body instead?
(defmacro expand-locals [bindings-map & body]
  (walk/postwalk
   (fn [form] (if (simple-symbol? form)
                `(get ~bindings-map '~form '~form)
                form))
   body))
;; => it's all just a mess and I don't know how to make it work.


;; perhaps `eval` is really the best answer?
(defmacro expand-locals [bindings-map & body]
  `(let [~@(mapcat identity (eval bindings-map))]
     ~@body))
;; doesn't work
#_(expand-locals my-locals
               project-id)
;; => 100
(expand-locals {'project-id 100}
               project-id)
;; => 100

;; From slack: supporting a var isn't hard (but supporting local scope lookup is harder)
;; https://clojurians.slack.com/archives/C03S1KBA2/p1659527382940809?thread_ts=1659523935.962109&cid=C03S1KBA2

(defmacro expand-locals [bindings-map & body]
  `(let [~@(if (symbol? bindings-map)
             (mapcat identity (deref (ns-resolve *ns* bindings-map)))
             (mapcat identity bindings-map))]
     ~@body))
;; doesn't work
#_(expand-locals my-locals project-id)
;; => 100
(expand-locals {project-id 100}
               project-id)
;; => 100

;; Final attempt - dynamic lookup of values from the var.
;; This works even with non-readable stuff like Hickari connection pool stored in a request map
(require 'clojure.walk)
(def my-locals {'project-id 100})
(defmacro expand-locals [bindings-var-sym & body]
  (let [bindings (deref (ns-resolve *ns* bindings-var-sym))]
    (->> body
         (clojure.walk/postwalk
          (fn [form] (if (and (simple-symbol? form)
                              (contains? bindings form))
                       `(get ~bindings-var-sym '~form)
                       form)))
         (mapcat identity))))
(expand-locals my-locals
               (let [a 1]
                 (str "b/" project-id "/" a)))
;; => "b/100/1"

(defmacro exl
  "shorter version of `expand-locals` using hardcoded symbol 'my-locals."
  [& body]
  (->> body
       (clojure.walk/postwalk
        (fn [form] (if (and (simple-symbol? form)
                            (contains? my-locals form))
                     `(get my-locals '~form)
                     form)))
       (mapcat identity)))
(exl project-id)
;; => 100



;; but it should be possible to simply generate the let bindings
;; using similar approach!
(defmacro expand-locals [bindings-var-sym & body]
  (let [bindings (deref (ns-resolve *ns* bindings-var-sym))]
    `(let [~@(mapcat (fn [[sym _]]
                       [sym `(get ~bindings-var-sym '~sym)])
                     bindings)]
       ~@body)))
(expand-locals my-locals
               (let [a 1]
                 (str "b/" project-id "/" a)))
;; => "b/100/1"

;; TIP: macroexpand-all call to `exl` to get the full let* form
;; so you can _edit_ the bindings
(defmacro exl [& body]
`(expand-locals my-locals ~@body))
(exl project-id)
;; => 100
