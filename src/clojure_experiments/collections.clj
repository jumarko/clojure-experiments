(ns clojure-experiments.collections
  "See also experiments.clj")


;;; apply concat vs flatten, etc.
;;; http://chouser.n01se.net/apply-concat/

;; flatten uses `sequential?` under the hood
;; which means it doesn't work with sets, strings, arrays, ...
(sequential? "abc")
;; => false

(flatten [#{:a :b} #{:c :d}])
;; => (#{:b :a} #{:c :d})
;; VS.
(flatten [[:a :b] [:c :d]])
;; => (:a :b :c :d)

;; on the other hand `apply concat` or `mapcat seq` work fine
(apply concat [#{:a :b} #{:c :d}])
;; => (:b :a :c :d)
(mapcat seq [#{:a :b} #{:c :d}])
;; => (:b :a :c :d)

;; and this one is particularly tricky!
(flatten [[1 [2 3]] #{4 5 6}])	
;; => (1 2 3 #{4 6 5})

;; reduce into is eager and dependent on the type of the first element in the collection
(reduce into [#{:a :b} #{:c :d}])
;; => #{:c :b :d :a}
(reduce into [[:a :b] #{:c :d}])
;; => [:a :b :c :d]

;; sequence/edution cat:
(sequence cat [#{:a :b} #{:c :d}])
;; => (:b :a :c :d)


;; There's also cool tool for not-printing lazy seqs
(def ^:dynamic *hold-the-lazy* false)

(defn write-lazy-seq [space? coll w]
  (if (and *hold-the-lazy*
           (instance? clojure.lang.IPending coll)
           (not (realized? coll)))
    (.write w (str (when space? " ") "..."))
    (when (seq coll)
      (when space?
        (.write w " "))
      (.write w (pr-str (first coll)))
      (write-lazy-seq true (rest coll) w))))

(defmethod print-method clojure.lang.ISeq [coll w]
  (.write w "(")
  (write-lazy-seq false coll w)
  (.write w ")"))

(let [xs (map inc (range 50))]
  (binding [*hold-the-lazy* true]
    (prn xs)))
;;=> will print:
;; (...)

;; ... with chunked seqs
(binding [*hold-the-lazy* true]
  (let [xs (map inc (range 50))]
    (first xs)
    (prn xs)))
;;=> prints:
;; (1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 ...)

;; "unchunk" any chunked seqs
;; (Joy of Clojure 15.3.1)
(defn seq1 [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))
(binding [*hold-the-lazy* true]
  (let [xs (seq1 (range 50))]
    (first xs)
    (prn xs)))
;;=> prints:
;; (0 ...)


;; Do they say that eduction/sequence realize everything once asked for the first element?
(def my-seq (range 40))
(let [xs (eduction (map #(doto % print inc)) my-seq)]
  (first xs))
;; prints (not all elements!)
;; 01234567891011121314151617181920212223242526272829303132

