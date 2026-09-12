(ns clojure-experiments.books.programming-clojure.repl
  "This was added in the 4th edition of the book as Chapter 3: Developing Interactively.")


;;; The Read phase:
;;; available via `read` and `read-string` functions.

;; they read the _first_ value and ignore the rest.
(read-string "(+ 1 2) (+ 10 20)")
;; => (+ 1 2)

;; the same using `read` 
(read (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. "(+ 1 2 (/ 1 0)) ")))


;;; Eval:

(eval '(+ 1 2))
;; => 3

(eval (read-string "(+ 1 2)"))
;; => 3

;; symbols evaluate in the context of the current namespae
(eval 'eval);; => #function[clojure.core/eval]


;;; Print:

(println (eval (read-string "(+ 1 2)")))
;; => nil


;;; Simple REPL

(defn rep []
  (try
    (print (str "(" *ns* ")> ")) ;; prompt
    (flush)
    (let [form (read-string (read-line)) ;; READ
          result (eval form) ;;EVAL
          ]
      (println result) ;; PRINT
      result
      )
    (catch Throwable e
      (println (str "Error: " (ex-message e))))))
(comment
  (rep)
  :-)

(defn repl []
  (loop []
    (when-not (= :exit (rep))
      (recur))))
(comment
  (repl)
  :-)
