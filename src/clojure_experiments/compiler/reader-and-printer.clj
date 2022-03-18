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

