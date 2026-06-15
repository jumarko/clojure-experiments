(ns clojure-experiments.books.clojure-brain-teasers.21-hanging-around
  "Gotcha of lazy seqs escaping the scope."
  (:require
   [clojure.java.io :as jio]
   [clojure.string :as str]))

;;; Lazy processing -> PROBLEMATIC!
(defn grep
  "Returns a lazy sequence of matches to substring in the file at path."
  [substring path]
  (with-open [reader (jio/reader path)]
    (->> reader
         (line-seq)
         (filter #(str/includes? % substring)))))

(grep "defn" "src/clojure_experiments/books/clojure_brain_teasers/21_hanging_around.clj");; => 
;;=>
;; 1. Caused by java.io.IOException
;;    Stream closed
;;        BufferedReader.java:  127  java.io.BufferedReader/ensureOpen
;;        BufferedReader.java:  325  java.io.BufferedReader/readLine
;;        BufferedReader.java:  400  java.io.BufferedReader/readLine
;;                   core.clj: 3099  clojure.core/line-seq
;;                   core.clj: 3100  clojure.core/line-seq/fn
;;               LazySeq.java:   50  clojure.lang.LazySeq/force
;;               LazySeq.java:   89  clojure.lang.LazySeq/realize
;;               LazySeq.java:  106  clojure.lang.LazySeq/seq
;;                    RT.java:  555  clojure.lang.RT/seq
;;                   core.clj:  139  clojure.core/seq
;;                   core.clj: 2826  clojure.core/filter/fn
;;               LazySeq.java:   50  clojure.lang.LazySeq/force
;;               LazySeq.java:   60  clojure.lang.LazySeq/lockAndForce
;;               LazySeq.java:   69  clojure.lang.LazySeq/sval
;;               LazySeq.java:   77  clojure.lang.LazySeq/unwrap
;;               LazySeq.java:   93  clojure.lang.LazySeq/realize
;;               LazySeq.java:  106  clojure.lang.LazySeq/seq
;;                    RT.java:  555  clojure.lang.RT/seq
;;                   core.clj:  139  clojure.core/seq
;;             core_print.clj:   53  clojure.core/print-sequential
;;             core_print.clj:  174  clojure.core/fn



;;; Solution: let's go EAGER

;; easiest: `doall`
(defn grep
  "Returns a realized sequence of matches to substring in the file at path."
  [substring path]
  (with-open [reader (jio/reader path)]
    (doall
     (->> reader
          (line-seq)
          (filter #(str/includes? % substring))))))
(grep "defn" "src/clojure_experiments/books/clojure_brain_teasers/21_hanging_around.clj")
;; => ("(defn grep" "(grep \"defn\" \"src/clojure_experiments/books/clojure_brain_teasers/21_hanging_around.clj\");; => ")


;; can we retain the laziness?
;; -> move the responsibility for closing the resource to the caller
(defn grep-reader
  "Returns a lazy seq of matches to substring in the reader.
  Caller is responsible for closing the reader."
  [substring reader]
  (->> reader
       (line-seq)
       (filter #(str/includes? % substring))))
;; the caller:
(with-open [rdr (jio/reader  "src/clojure_experiments/books/clojure_brain_teasers/21_hanging_around.clj")]
  ;; Notice `doall` is needed here even when running in the REPL (otherwise `with-open` would close the reader before we had the chance to get the results)
  (doall (grep-reader "defn" rdr)))
;; => ("(defn grep" "(grep \"defn\" \"src/clojure_experiments/books/clojure_brain_teasers/21_hanging_around.clj\");; => " "(defn grep" "(grep \"defn\" \"src/clojure_experiments/books/clojure_brain_teasers/21_hanging_around.clj\")" ";; => (\"(defn grep\" \"(grep \\\"defn\\\" \\\"src/clojure_experiments/books/clojure_brain_teasers/21_hanging_around.clj\\\");; => \")" "(defn grep-reader")
