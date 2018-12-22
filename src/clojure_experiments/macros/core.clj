(ns clojure-experiments.macros)

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
