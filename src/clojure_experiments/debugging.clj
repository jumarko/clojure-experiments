(ns clojure-experiments.debugging
  "Experiments with debugging code,
  perhaps using Cider debugger: https://docs.cider.mx/cider/debugging/debugger.html"
  (:require [clojure.string :as str]
            [flow-storm.api :as fs-api]
            [sc.api :as sc]))


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

(defmacro get-function-name
  "Returns the name of the function currently being on the top of the stack."
  []
  `(let [frame# (first (:trace (Throwable->map (ex-info "" {}))))]
    (clojure.repl/demunge (name (first frame#)))
    ;; StackWalker is another option: https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/lang/StackWalker.html#getCallerClass()
    #_(println (.getCallerClass (java.lang.StackWalker/getInstance java.lang.StackWalker$Option/RETAIN_CLASS_REFERENCE)))))

(defn add [a b]
  (println (get-function-name))
  (+ a b))
(add 10 20)

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
(my-function 4 5 6)
(my-function 5 6 7)

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



;;; `expand-locals` macro as a supplement to `locals`
;;; This looks quite similar to scope-capture and its `letsc` and `defsc` macros:
;;; https://github.com/vvvvalvalval/scope-capture#usage
;;; - 

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
(defmacro expand-locals
  "Establish  bindings saved in given var as local symbols via `let`."
  [bindings-var-sym & body]
  (let [binding-syms (keys (deref (ns-resolve *ns* bindings-var-sym)))]
    `(let [~@(mapcat (fn [sym] [sym `(get ~bindings-var-sym '~sym)])
                     binding-syms)]
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

(clojure.walk/macroexpand-all '(exl project-id))
(let*
    [project-id (clojure.core/get clojure-experiments.debugging/my-locals 'project-id)]
  project-id)


;;; scope-capture: https://github.com/vvvvalvalval/scope-capture#usage
;;; similar but more advanced than my `locals` and `expand-locals` macros.
(def my-fn
  (let [a 23
        b (+ a 3)]
    (fn [x y z]
      (let [u (inc x)
            v (+ y z u)]
        (* (+ x u a)
           ;; Insert a `spy` call in the scope of these locals
           (sc/spy
            (- v b)))))))

(my-fn 3 4 5)
;; => -390

(sc/last-ep-id)
;; => [3 -3]

(sc/letsc 3
          [a b x y z])
;; => [23 26 3 4 5]
(macroexpand-1 '(sc/letsc 3
                          [a b x y z]))
(clojure.core/binding
    []
  (clojure.core/let
      [a
       (sc.impl/ep-binding 3 'a)
       b
       (sc.impl/ep-binding 3 'b)
       x
       (sc.impl/ep-binding 3 'x)
       y
       (sc.impl/ep-binding 3 'y)
       z
       (sc.impl/ep-binding 3 'z)
       u
       (sc.impl/ep-binding 3 'u)
       v
       (sc.impl/ep-binding 3 'v)]
    [a b x y z]))


;;; what about having a `locals` macro that also saves the history??
;;; simple way to use taps via atom which can then be inspected with cider-inspect
(def my-locals (atom []))

(defn schedule-cleanup! [max-history]
  (add-watch my-locals :cleanup
             (fn [_k r _old new]
               (when (< max-history (count new))
                 (swap! r subvec 1)))))

;; keep up to 100 last records by default
(schedule-cleanup! 100)

(defn record-locals [x]
  (swap! my-locals conj x))

(defmacro get-function-name
  "Returns the name of the function currently being on the top of the stack."
  []
  `(let [frame# (first (:trace (Throwable->map (ex-info "" {}))))]
     (clojure.repl/demunge (name (first frame#)))
     ;; StackWalker is another option: https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/lang/StackWalker.html#getCallerClass()
     #_(println (.getCallerClass (java.lang.StackWalker/getInstance java.lang.StackWalker$Option/RETAIN_CLASS_REFERENCE)))))

(defmacro locals []
  (let [ks (keys &env)]
    `(let [ls# (zipmap '~ks [~@ks])
           fname# (get-function-name)
           now# (java.util.Date.)
           dmeta# {:fn-name fname#
                   :file *file*
                   :timestamp now#}]
       (printf "\ndebugging function %s in %s\n" fname# *file*)
       (println "====================== DEBUG locals =======================")
       (clojure.pprint/pprint ls#)
       (println "====================== END DEBUG locals =======================")
       ;; save it in the atom
       (record-locals (with-meta ls# dmeta#))
       ;; and also tap it
       (tap> (with-meta [fname# ls#] dmeta#)))))

(defmacro expand-locals
  "Establish  bindings saved in given var as local symbols via `let`."
  [bindings-var-sym & body]
  (let [atom-var (deref (ns-resolve *ns* bindings-var-sym))
        binding-syms (keys (peek @atom-var))]
    ;; get the latest values of locals from bindings-var-sym
    `(let [~@(mapcat (fn [sym] [sym `(get (peek @~bindings-var-sym) '~sym)])
                     binding-syms)]
       ~@body)))
(defmacro exl [& body]
  `(expand-locals my-locals ~@body))

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
(my-function 4 5 6)
(my-function 5 6 7)

(exl [x y z a b c])
;; => [5 6 7 210 211 211/10]

