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

;; notice that java.sql.Timestamp is special in this regard
;; and serializaing and deserializing it again will return java.util.Date
;; see https://ask.clojure.org/index.php/11898/printing-and-reading-date-types
(type (deserialize (serialize (java.sql.Timestamp. 1))))
;; => java.util.Date


(defn my-print [& more]
  (binding [*print-readably* nil]
    (apply pr more)))
(def m {:a "one" :b "two"})
(my-print m);; => nil
;; this reads one and two as symbols, not strings
(= m (read-string (with-out-str (my-print m))))
;; => false
;; ... here they are proper strings
(= m (read-string (pr-str m)))
;; => true


(read-string "[a b #=(* 3 4)]")
;; => [a b 12]
;; but #= can be prohibited:
#_(binding [*read-eval* false]
  (read-string "#=(java.lang.System/exit 0)"))
;; 1. Unhandled java.lang.RuntimeException
;; EvalReader not allowed when *read-eval* is false.



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

(read-string "#=(java.util.ArrayList. [8 9]))")
;; => [8 9]
(type (read-string "#=(java.util.ArrayList. [8 9]))"))
;; => java.util.ArrayList
(type (read-string "[8 9])"))
;; => clojure.lang.PersistentVector

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



;;; Print and read in Clojure: https://www.proofbyexample.com/print-and-read-in-clojure.html
;;; print-dup example of org.joda.time.DateTime
;;; Note that print-method has an implementation for java.lang.Object, which is why it is able to print anything.
;;;   print-dup, on the other hand has fewer stock methods, and will throw when you try to print something it doesn’t explicitly know about.

#_(binding [*print-dup* true] (pr-str (org.joda.time.DateTime.)))
;; IllegalArgumentException No method in multimethod 'print-dup' for dispatch value:
;; class org.joda.time.DateTime  clojure.lang.MultiFn.getFn (MultiFn.java:160)

;; Make it possible to print joda DateTime instances; required for caching.
(defmethod print-dup org.joda.time.DateTime
  [dt out]
  (.write out (str "#=" `(org.joda.time.DateTime. ~(.getMillis dt)))))

(binding [*print-dup* true] (pr-str (org.joda.time.DateTime.)))
;; => "#=(org.joda.time.DateTime. 1649324290242)"
(read-string "#=(org.joda.time.DateTime. 1649324290242)")
;; => #object[org.joda.time.DateTime 0x1c8e91f "2022-04-07T11:38:10.242+02:00"]

