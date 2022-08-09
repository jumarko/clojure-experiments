(ns clojure-experiments.macros.macros
  (:require [clojure.data :as data]))
;;; Simple experiment with macros and the difference between compile-time and runtime
;;; The first version of `name-it` macro is too naive and it calls println without quoting the form.
;;; Which means that println is executed in compile-time.
(defmacro name-it [x f]
  (println "Hello"))
;; with the macro above, we get "Hello" printed every time we compile this function!
(defn name-name-it []
  (name-it myhello identity))

;; ... so we need to actually quote the form:
(defmacro name-it [x f]
  `(println "I'm making a symbol name for you"))
;; .. and remember to compile `name-name-it` again
;; otherwise the new version of `name-it` compiled inside the function
;; won't be reflected and nothing is printed when the function is called
(name-name-it)

;;; and maybe we can demonstrate it with a single macro?
;; when you just compile this macro, nothing happens yet
(defmacro stuff [x]
  (println "Compiling..." x)
  `(println "Executing..." ~x))
;; Compile time: every time you compile this function you get "Compiling... y" printed
;; that means the macro is actually _expanded_ before the function body is compiled.
(defn do-stuff [y]
  (stuff y))
;; Runtime: this prints "Executing... Hello"
(do-stuff "Hello")


;;; See Jeff Terrell's blog post:
;;; A Few Tips for Writing Macros in Clojure: http://blog.altometrics.com/2016/04/a-few-tips-for-writing-macros-in-clojure/

(defmacro numeric-if [form]
  `(let [x# ~form]
     (cond
       (neg? x#) :neg
       (pos? x#) :pos
       :else     :zero)))

(numeric-if (/ -1 10))


(defmacro avg
  [seqable-expr]
  `(let [seqable# ~seqable-expr]
     (/ (reduce + seqable#)
        (count seqable#))))


(macroexpand-1 '(defn foo [a b] (+ a b)))

(defn foo [a b] (+ a b))

;; hiredman - analogy between :const and macro templates
(def ^:const foo 1)
(defn bar [] (+ foo 10))
;; is similar to
(def foo 1)
(defmacro foo* [] (list 'quote foo))
(defn bar [] (+ (foo*) 10))



;;; <-- macro https://clojurians.slack.com/archives/C03S1KBA2/p1642537427215800
;;; Christophe Grand: https://twitter.com/cgrand/status/1281527501387440128
(defmacro <<- [& forms] `(->> ~@(reverse forms)))

(defn fn-which-may-return-falsey [x]
  (or x false))
(defn fn-which-may-return-nil [x]
  (or x nil))

(let [a 10
      y 100]
  (<<-
   (if (odd? a) 1)
   (let [a (quot a 2)])
   (when-let [x (fn-which-may-return-falsey a)])
   (when-some [b (fn-which-may-return-nil x)])
   (when (pos? x))
   ;; this is tricky: it prints '5' with `do` but '5 2' without `do`!
   (do (println x))
   (if (odd? (+ x y)) 2)
   3))
;; => 2

;; macroexpansion:
(let*
    [a 10 y 100]
  (if (odd? a)
    1
    (let*
        [a (quot a 2)]
      (let*
          [temp__5756__auto__ (fn-which-may-return-falsey a)]
        (if temp__5756__auto__
          (do
            (let*
                [x temp__5756__auto__]
              (let*
                  [temp__5760__auto__ (fn-which-may-return-nil x)]
                (if (nil? temp__5760__auto__)
                  nil
                  (let*
                      [b temp__5760__auto__]
                    (if (pos? x)
                      (do
                        (do
                          (println x)
                          (if (odd? (+ x y)) 2 3))))))))))))))
;; => 2

;; alternative `better-cond` usage
(comment 
  ;; Note that this doesn't compile
  (better/cond
    (odd? a) 1
    :let [a (quot a 2)]
    :when-let [x (fn-which-may-return-falsey a)]
    :when-some [b (fn-which-may-return-nil x)]
    :when (pos? x)
    :do (println x)
    (odd? (+ x y)) 2
    3)
  ,)


;;; on slack: https://clojurians.slack.com/archives/C053AK3F9/p1655979353819919
;;; ---------
;;; Why do I need gensym outside of the quoted form, rather than using auto-gensym # syntax?
;;; I would expect both version to result in [1 2] but the first one seems to fail to scope the let binding as I'd like.
;;; (Obvs these macros are simplified to the point that they don't need to be macros - the conditional logic in the real version isn't relevant here)
;;; ----
;;; Nice tip from delaguardo!! defmacro itself is a macro so you can expand the declaration of recursive-macro-1  to debug
(defmacro recursive-macro-1 [[x & more :as xs] acc]
  (if (seq xs)
    `(let [x# ~x] ; DOES NOT WORK as expected!
       (recursive-macro-1 ~more (conj ~acc x#)))
    acc))

(defmacro recursive-macro-2 [[x & more :as xs] acc]
  (if (seq xs)
    (let [gx (gensym 'x)]
      `(let [~gx ~x]
         (recursive-macro-2 ~more (conj ~acc ~gx))))
    acc))


(comment
  (recursive-macro-1 [1 2] []) ;; => [2 2]
  (recursive-macro-2 [1 2] []) ;; => [1 2]
  )

;; auto-gensym
(let [x__10885__auto__ 1]
  (recursive-macro-1 (2) (conj [] x__10885__auto__)))
;; macroexpand-all
(let*
    [x__10885__auto__ 1]
  (let*
      [x__10885__auto__ 2]
    (conj (conj [] x__10885__auto__) x__10885__auto__)))

;; gensym
(let [x11094 1]
  (recursive-macro-2 (2) (conj [] x11094)))
;; macroexpand-all
(let* [x11095 1]
  (let* [x11096 2] (conj (conj [] x11095) x11096)))

;; so recursive-macro-1 throws away all the elements but the last one!
(recursive-macro-1 [1 2 3 4 5] [])
;; => [5 5 5 5 5]


;; debugging macro that can save the local context
;; see `clojure-experiments.debugging/locals`
(defmacro locals []
  (let [ks (keys &env)]
    `(let [ls# (zipmap '~ks [~@ks])]
       (println "====================== DEBUG locals =======================")
       (clojure.pprint/pprint ls#)
       (println "====================== END DEBUG locals =======================")
       (def my-locals ls#))))


;;; idea for `condto` macro: https://clojurians.slack.com/archives/C03M5U2LLAC/p1659859505220269
;;; - heavily inspired by `cond->`

;; first prepare some objects
(definterface ICfg
  (setMaxTotal [max])
  (setMaxIdle [max])
  (setMaxWait [max])
  (inspect []))

;; For mutable fields, see https://stackoverflow.com/questions/3132931/mutable-fields-in-clojure-deftype
(deftype cfg [^:volatile-mutable maxTotal
              ^:volatile-mutable maxIdle
              ^:volatile-mutable maxWait]
  ICfg
  (setMaxTotal [this max] (set! maxTotal max))
  (setMaxIdle [this max] (set! maxIdle max))
  (setMaxWait [this max] (set! maxWait max))
  (inspect [this] [maxTotal maxIdle maxWait]))

(def my-config (->cfg 0 0 0))
(.inspect my-config)
;; => [0 0 0]


;; ... then the idea is to simplify this:
(let [max-total 100
      max-idle 20
      max-wait-ms 1000]
  (cond-> my-config
    max-total   (doto (.setMaxTotal max-total))
    max-idle    (doto (.setMaxIdle max-idle))
    max-wait-ms (doto (.setMaxWait max-wait-ms))))
(.inspect my-config)
;; => [100 20 1000]

;; this is the quick first solution - just replacing one symbol in cond->
;; it's not as simple as it could be though
(defmacro condto
  "Like `cond->` but passes the original (mutable) object 
  through each form for which the corresponding test  expression is true.
  Uses `doto` instead of `->`."
  [expr & clauses]
  (assert (even? (count clauses)))
  (let [g (gensym)
        steps (map (fn [[test step]]
                     ;; this is the only change compared to `cond->`: instead of `->` we use `doto`
                     `(if ~test (doto ~g ~step) ~g))
                   (partition 2 clauses))]
    `(let [~g ~expr
           ~@(interleave (repeat g) (butlast steps))]
       ~(if (empty? steps)
          g
          (last steps)))))

(let [max-total 1000
      max-idle 200
      max-wait-ms 10000]
  (condto my-config
    max-total   (.setMaxTotal max-total)
    max-idle    (.setMaxIdle max-idle)
    max-wait-ms (.setMaxWait max-wait-ms)))
(.inspect my-config)
;; => [1000 200 10000]

;; Finally, this is an attempt to simplify the macroexpanded form
;; based on our constraint that we do not actually want to thread the result
;; but the original object.
(defmacro condto
  "Like `cond->` but passes the original (mutable) object 
  through each form for which the corresponding test  expression is true.
  Uses `doto` instead of `->`."
  [expr & clauses]
  (assert (even? (count clauses)))
  (let [g (gensym)
        steps (map (fn [[test [fst & next :as step]]]
                     `(when ~test ~(conj next g fst)))
                   (partition 2 clauses))]
    `(let [~g ~expr]
       ~@steps
       ~g)))

(let [max-total 1000000
      max-idle 2
      max-wait-ms 10]
  (condto my-config
          max-total   (.setMaxTotal max-total)
          max-idle    (.setMaxIdle max-idle)
          max-wait-ms (.setMaxWait max-wait-ms)))
(.inspect my-config)
;; => [1000000 2 10]

