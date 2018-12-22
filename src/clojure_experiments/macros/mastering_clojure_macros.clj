(ns clojure-experiments.macros.mastering-clojure-macros
  "Examples from the book Mastering Clojure Macros.")

;;; Chapter 1 - Build a Solid Foundation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(read-string "(+ 1 2 3 4 5)")
(class (read-string "(+ 1 2 3 4 5)"))
(eval (read-string "(+ 1 2 3 4 5)"))
(class (eval (read-string "(+ 1 2 3 4 5)")))

;; we can now programatically replace addition with multiplication
(let [expression (read-string "(+ 1 2 3 4 5)")]
  (cons (read-string "*")
        (rest expression)))
#_(eval *1)

;; let's now use real data structures instead of string (and `read-string`)
(let [expression (quote (+ 1 2 3 4 5))]
  (cons (quote *)
        (rest expression)))
#_(eval *1)

;; finally simplifying using '
(let [expression '(+ 1 2 3 4 5)]
  (cons '* (rest expression)))
#_(eval *1)

;; when macro
(defmacro my-when [test & body]
  (list 'if test (cons 'do body)))
(my-when (= 2 (+ 1 1))
         (print "you got")
         (print " the touch!")
         (println))
;; substituted it looks like this:
(list 'if
      '(= 2 (+ 1 1))
      (cons 'do
            '((print "You got")
              (print " the touch!") (println))))

;; my cond macro
(defmacro my-cond
  [& clauses]
  (when (seq clauses)
    `(if ~(first clauses)
       ~(second clauses)
       (my-cond ~@(drop 2 clauses)))))
(my-cond
 (> 1 10) 10
 (= 3 2) 5
 (= 2 2) 1000
 (> 10 2) 1000000)
;; expanded form
(if (> 1 10)
  10
  (if (= 3 2) 5 (if (= 2 2) 1000 (if (> 10 2) 1000000 nil))))

;; cond macro from the book
(defmacro book-cond [& clauses]
  (when clauses
    (list 'if (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException. "cond requires even number of forms")))
          (cons 'clojure.core/cond (next (next clauses))))))
(book-cond
 (> 1 10) 10
 (= 3 2) 5
 (= 2 2) 1000
 (> 10 2) 1000000)


(macroexpand-1 '(when-falsy (= 1 2) (println "hi!")))
;=> (when (not (= 1 2)) (do (println "hi!")))
(macroexpand '(when-falsy (= 1 2) (println "hi!")))
;=> (if (not (= 1 2)) (do (do (println "hi!"))))


;; macroexpand works only when the inner macro is in the first position
;; macroexpand-all covers some cases inside macro too
(macroexpand '(if-let [x (range 10)]
                (println "Computing...")
                (when (< (first x) (second x))
                  (println "True."))))
;; expanded as:
(comment
  (let*
      [temp__5533__auto__ (range 10)]
    (if
        temp__5533__auto__
      (clojure.core/let [x temp__5533__auto__] (println "Computing..."))
      (when (< (first x) (second x)) (println "True."))))
  )

(clojure.walk/macroexpand-all '(if-let [x (range 10)]
                                 (println "Computing...")
                                 (when (< (first x) (second x))
                                   (println "True."))))
;; espanded as:
(comment
 (let*
     [temp__5533__auto__ (range 10)]
   (if
       temp__5533__auto__
     (let* [x temp__5533__auto__] (println "Computing..."))
     (if (< (first x) (second x)) (do (println "True."))))))
