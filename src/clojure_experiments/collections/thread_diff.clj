(ns clojure-experiments.collections.thread-diff
  "Useful debugging thread macro from https://clojurians.slack.com/archives/C053PTJE6/p1661543383600139?thread_ts=1661543243.366939&cid=C053PTJE6"
  (:require [lambdaisland.deep-diff2 :as ddiff]))

(defn print-diff! [a b]
  (ddiff/pretty-print (ddiff/diff a b) (ddiff/printer {:width 80})))

(defn outerpose [x ys]
  (concat [x] (interpose x ys) [x]))

;; Made more general by accepting the threading macro symbol
(defmacro tdiff
  "Threads the expr through the forms. Inserts x as the
  second item in the first form, making a list of it if it is not a
  list already. If there are more forms, inserts the first form as the
  second item in second form, etc."
  {:added "1.0"}
  [thread-sym in-x & in-forms]
  (loop [x [::nothing in-x],
         forms (concat
                ;; Starting:
                [(list (list 'fn '[[old new]]
                             (list 'println "Starting -diff-> with:" '(pr-str new) "\n")
                             '[old new]))]
                (interleave
                 ;; run with old and new context steps
                 (mapv
                  (fn [form]
                    (list (list 'fn '[[_ new]] ['new (list thread-sym 'new form)])))
                  in-forms)
                 ;; report diff result steps
                 (mapv (fn [form]
                         (list (list 'fn '[[old new]]
                                     (list 'println "")
                                     ;; print Running step
                                     (list 'println "Running: " (list 'str "(-> "  (list pr-str 'old) " " (pr-str form) ")"))
                                     ;; print diff
                                     '(print-diff! old new)
                                     '[old new])))
                       in-forms)))]
    (if forms
      (let [form (first forms)
            threaded (if (seq? form)
                       (with-meta `(~(first form) ~x ~@(next form)) (meta form))
                       (list form x))]
        (recur threaded (next forms)))
      (list last x))))

(comment
  (tdiff ->
   {}
   (assoc :a 1 :b {})
   (assoc-in [:b :c] [1 2 3 4])
   (update-in [:b :c 2] inc)
   (update-in [:b :c] reverse))
  ;; => prints this:
  ;; Starting -diff-> with: {} 

  ;; Running:  (-> {} (assoc :a 1 :b {}))
  ;; {+:a 1, +:b {}}

  ;; Running:  (-> {:a 1, :b {}} (assoc-in [:b :c] [1 2 3 4]))
  ;; {:a 1, :b {+:c [1 2 3 4]}}

  ;; Running:  (-> {:a 1, :b {:c [1 2 3 4]}} (update-in [:b :c 2] inc))
  ;; {:a 1, :b {:c [1 2 -3 +4 4]}}

  ;; Running:  (-> {:a 1, :b {:c [1 2 4 4]}} (update-in [:b :c] reverse))
  ;; {:a 1, :b {:c [-1 -2 4 4 +2 +1]}}

  (tdiff ->> (range 1000)
       (map inc)
       (filter odd?))

  
  .)
