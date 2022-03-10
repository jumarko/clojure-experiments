(ns clojure-experiments.books.joy-of-clojure.debugging-repl
  "This is the 'debug repl' part from Chapter 17.
  `contextual-eval` is copied from Chapter 8."
  )


(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `'~v])
                    ctx)]
      ~expr)))

;; here the let bindings will be: [a (quote 1) b (quote 2)]
(contextual-eval '{a 1 b 2} '(+ a b))
;; => 3

;; let's first override `clojure.main/repl` reader
(defn readr [prompt exit-code]
  (let [input (clojure.main/repl-read prompt exit-code)]
    (if (= input ::tl)
           exit-code
           input)))

;; now play
(comment
  ;; you will see stdin promtp and when you press enter this will return the form you entered
  ;; type [1 2 3]
  (readr #(print "invisible=> ") ::exit)
;; => [1 2 3]

  ;; type ::tl
  (readr #(print "invisible=> ") ::exit)
  ;; => :clojure-experiments.books.joy-of-clojure.ch17-clojure-way-of-thinking/exit

  ,)


;; overriding evaluator - :eval
(defmacro local-context []
  (let [symbols (keys &env)]
    ;; instead of `(quote ~sym) the usual way is just `'sym ? would that work?
    (zipmap (map (fn [sym] `'~sym)
                 symbols)
            symbols)))

(local-context)
;; => {}

(let [a 1 b 2 c 3]
  (let [b 200]
    (local-context)))
;; => {a 1, b 200, c 3}

;; no we want to evaluate forms in the local context
(defmacro break []
  `(clojure.main/repl
    :prompt #(print "debug=> ")
    :read readr
    :eval (partial contextual-eval (local-context))))


(defn div [n d] (break) (int (/ n d)))

(comment
  ;; type `(local-context)` then `::tl`
  (div 10 0)

  ,)

