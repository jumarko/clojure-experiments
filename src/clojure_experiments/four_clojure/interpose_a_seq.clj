(ns four-clojure.interpose-a-seq)

;;; Interpose a Seq problem: http://www.4clojure.com/problem/40
;;;
;;; Write a functiona which separates the items of a sequence by an arbitrary value

(defn interpose-fn
  "Separates the items of a sequence by an arbitrary value."
  [sep s]
  (vec
   (drop-last
    (mapcat #(vector % sep)
            s))))

;; another possibility using "reduce"
(defn interpose-fn
  "Separates the items of a sequence by an arbitrary value."
  [sep s]
  (pop
   (reduce #(conj %1 %2 sep) []
           s)))


(= (interpose-fn 0 [1 2 3])
   [1 0 2 0 3])

(= (apply str (interpose-fn ", " ["one" "two" "three"]))
   "one, two, three")

(= (interpose-fn :z [:a :b :c :d])
   [:a :z :b :z :c :z :d])
