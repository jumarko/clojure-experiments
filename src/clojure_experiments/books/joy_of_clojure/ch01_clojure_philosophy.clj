(ns clojure-experiments.books.joy-of-clojure.ch01-clojure-philosophy)


;;; Infix vs prefix notation
;;; See also http://fogus.me/fun/unfix/ -> https://github.com/fogus/unfix

;; infix3 function - p.12
;; this implementation works for both + and * (precendece of operators)

(def order {+ 0
            * 1})
(defn infix3 [a op1 b op2 c]
  (if (< (order op1) (order op2))
    (op1 a (op2 b c))
    (op2 (op1 1 a b) c)))

(infix3 10 + 20 * 2)
;; => 50

(infix3 10 * 20 + 2)
;; => 202


;; this if infix implementation supporting arbitrary number of arguments:
;; https://github.com/fogus/unfix/blob/master/src/joy/unfix/infix.clj#L11-L19


(def ^:dynamic *ops* '[- + * / < > && || = !=])
(def rank (zipmap *ops* (iterate inc 1)))

(defn- infix* 
  [[a b & [c d e & more] :as v]]
  (prn a)
  (prn b)
  (prn c)
  (prn d)
  (prn e)
  (prn more)
  (prn v)
  (cond
    (vector? a) (recur (list* (infix* a) b c d e more))
    (vector? c) (recur (list* a b (infix* c) d e more))
    (rank b)    (if (and d (< (rank b 0) (rank d 0)))
                  (recur (list a b (infix* (list* c d e more))))
                  (recur (list* (list (identity b) a c) d e more)))
    :else a))


;; this has to be placed in `src/data_readers.clj` if `_` in infix* should work properly
;; (defn infix-reader [form]
;;   (binding [_ identity]
;;     (infix* form)))

(defmacro infix [& args]
  (infix* args))

(infix 10 + 20 * 2)
;; => 50

(infix 10 * 20 + 2)
;; => 202

(infix 10 + 20 + 2 * 200 + 3 * 4 * 5) ; 30 + 400 + 60
;; => 490

