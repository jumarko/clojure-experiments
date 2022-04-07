(ns clojure-experiments.compiler.reader-and-printer)

;;; Stuart Sierra's presentation: https://stuartsierra.com/download/2011-11-10-clojure-next-steps.pdf
;;; - start on p. 10
(defn serialize [x]
  (binding [*print-dup* true]
    (pr-str x)))

(defn deserialize [string]
  (read-string string))

(serialize {:a 1 :b [1 2 3]})
;; => "#=(clojure.lang.PersistentArrayMap/create {:a 1, :b [1 2 3]})"


(defn my-print [& more]
  (binding [*print-readably* nil]
    (apply pr more)))
(def m {:a "one" :b "two"})
(my-print m);; => nil
(= m (read-string (with-out-str (my-print m))))
;; => false
(= m (read-string (pr-str m)))
;; => true


(read-string "[a b #=(* 3 4)]")
;; => [a b 12]
;; but #= can be prohibited:
(binding [*read-eval* false]
  (read-string "#=(java.lang.System/exit 0)"))


;;; Serializing Clojure objects: https://groups.google.com/g/clojure/c/5wRBTPNu8qo [2008 discussion with a couple of comments from Rich Hickey]
;;; Rich: Yes, please consider print/read.
;;; It is readable text, works with a lot of data structures, and is extensible.
;;; As part of AOT I needed to enhance print/read to store constants of many kinds, and restore faithfully.
;;;  This led to a new multimethod - print-dup, for high-fidelity printing.
;;; You can get print-dup behavior by binding *print-dup*
;;;
;;; Rich's example:
(binding [*print-dup* true]
  (dorun
   (map prn
        [[1 2 3]
         {4 5 6 7}
         (java.util.ArrayList. [8 9])
         String
         "string"
         42M
         :hello
         #"ethel"
         (sorted-set 9 8 7 6)
         #'rest])))
;; prints this:
;; [1 2 3]
;; #=(clojure.lang.PersistentArrayMap/create {4 5, 6 7})
;; #=(java.util.ArrayList. [8 9])
;; #=java.lang.String
;; "string"
;; 42M
;; :hello
;; #"ethel"
;; #=(clojure.lang.PersistentTreeSet/create [6 7 8 9])
;; #=(var clojure.core/rest)


;; Compare to this
(dorun
 (map prn
      [[1 2 3]
       {4 5 6 7}
       (java.util.ArrayList. [8 9])
       String
       "string"
       42M
       :hello
       #"ethel"
       (sorted-set 9 8 7 6)
       #'rest]))
;;=> prints this:
;; [1 2 3]
;; {4 5, 6 7}
;; [8 9]
;; java.lang.String
;; "string"
;; 42M
;; :hello
;; #"ethel"
;; #{6 7 8 9}
;; #'clojure.core/rest
