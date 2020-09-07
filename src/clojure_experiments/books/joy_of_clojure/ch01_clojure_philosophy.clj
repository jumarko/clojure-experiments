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

(comment
  
  (infix 10 + 20 * 2)
  ;; => 50

  (infix 10 * 20 + 2)
  ;; => 202

  ;;(infix 10 + 20 + 2 * 200 + 3 * 4 * 5) ; 30 + 400 + 60
  ;; => 490

  ;;
  )



;;; Namespace encapsulation - chessboard example (p. 21-22)

(def ^:dynamic *file-key* \a)
(def ^:dynamic *rank-key* \0)

;; 1. first implementation encapsulated in private functions within a single namespace
(defn- file-component [file]
  (- (int file) (int *file-key*)))

(defn- rank-component [rank]
  (->> (int *rank-key*)
       (- (int rank))
       (- 8)
       (* 8)))

(defn- index [file rank]
  (+ (file-component file) (rank-component rank)))

(defn lookup [board pos]
  (let [[file rank] pos]
    (board (index file rank))))

(defn initial-board []
  [\r \n \b \q \k \b \n \r
   \p \p \p \p \p \p \p \p
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \- \- \- \- \- \- \- \-
   \P \P \P \P \P \P \P \P
   \R \N \B \Q \K \B \N \R
   ])

(lookup (initial-board) "a1")
;; => \R


;; 2. Then implementation encapsulated in a helper function in a single block smaller than a ns

(letfn [(index [file rank]
          (let [f (- (int file) (int \a))
                r (* 8 (- 8 (- (int rank) (int \0))))]
            (+ f r)))]
  (defn lookup2 [board pos]
    (let [[file rank] pos]
      (board (index file rank)))))

(lookup2 (initial-board) "a1")
;; => \R


;; 3. Finally everything within a single function
(defn lookup3 [board pos]
  (let [[file rank] (map int pos)
        [fc rc] (map int [\a \0])
        f (- file fc)
        r (* 8 (- 8 (- rank rc)))
        index (+ f r)]
    (board index)))
(lookup3 (initial-board) "a1")
;; => \R

