(ns clojure-experiments.books.joy-of-clojure.ch05-collections)

;;; Persistence, sequences, and complexity (p. 85)

;; arrays are mutable => no historical versions are preserved

(def d [:willie :barnabas :adam])
(def ds (into-array d))
(seq ds)
;; => (:willie :barnabas :adam)

(aset ds 1 :quentin)
;; => :quentin

(seq ds)
;; => (:willie :quentin :adam)

;; original vector is still unchanged
d
;; => [:willie :barnabas :adam]

;; even if we replace an item
(replace {:barnabas :quentin} d)
;; => [:willie :quentin :adam]
d
;; => [:willie :barnabas :adam]


;;; Sequential, sequence, seq

;; list, vector and java.util.List are sequential
(= '(1 2 3) [1 2 3])
;; => true

(= '(1 2 3) (java.util.ArrayList. [1 2 3]))
;; => true

;; but set is not
(= '(1 2 3) #{1 2 3})
;; => false


;;; p. 89 - sequence abstraction
;;; Every collection type supports at least one kind of seq object via the `seq` function;
;;; but some collections like vectors and maps support more:
;;; - vector supports `rseq`
;;; - map supports `keys`, `vals`
;;; NOTE: all of them return `nil` if the collection is empty!!!

(rseq [])
;; => nil

(vals {})
;; => nil

(class (seq (hash-map :a 1)))
;; => clojure.lang.PersistentHashMap$NodeSeq

(class (keys (hash-map :a 1)))
;; => clojure.lang.APersistentMap$KeySeq


;;; Vectors - p. 91 - 99

;; convert any collection to a vector
(vec (range 10))
;; => [0 1 2 3 4 5 6 7 8 9]

;; use `into` if you already have a vector
;; - it's like "concatenation" -> O(n)
(let [my-vector [:a :b :c]]
  (into my-vector (range 10)))
;; => [:a :b :c 0 1 2 3 4 5 6 7 8 9]

;; `vector` is convenient when you have multiple arguments such as:
(map vector [:a :b :c] (range 10))
;; => ([:a 0] [:b 1] [:c 2])

;; `vector-of` uses primitives internally
;; - gtochaes with overflow, underflow, etc. apply
(into (vector-of :int ) [Math/PI 2 1])
;; => [3 2 1]


#_(into (vector-of :int ) [1 2 173981273891723])
;;=> Execution error (IllegalArgumentException) at clojure.core.Vec/cons (gvec.clj:188).
;;   Value out of range for int: 173981273891723

;; using `update-in` with a nested vector
(def matrix
  [[1 2 3]
   [4 5 6]
   [7 8 9]])

(update-in matrix [1 2] * 100)
;; => [[1 2 3] [4 5 600] [7 8 9]]

(defn neighbors
  ([size yx]
   (neighbors [[-1 0] [1 0] [0 -1] [0 1]]
              size
              yx))
  ([deltas size [y x :as yx]]
   ;; my own implementation
   (for [[dy dx :as d] deltas
         :let [newyx (mapv + d yx)]
         :when (every? #(< -1 % size) newyx)]
     newyx)
   ;; from the book
   #_(filter (fn [new-yx]
             (every? #(< -1 % size) new-yx))
           (map #(mapv + yx %)
                deltas))))

(neighbors 3 [0 0])
;; => ([1 0] [0 1])
(neighbors 3 [1 1])
;; => ([0 1] [2 1] [1 0] [1 2])



;;; Vectors as stacks (p. 9)
;;; conj, pop, peek
(def my-stack [1 2 3])

(peek my-stack)
;; => 3

(pop my-stack)
;; => [1 2]

(conj my-stack 4)
;; => [1 2 3 4]

;; but pop from empty list throws an exception
#_(pop [])
;; Execution error (IllegalStateException) at clojure-experiments.core/eval17211 (form-init2026280959139683651.clj:56).
;; Can't pop empty vector


;;; Thinking in Maps (p. 107)

;; you can pretend lists are tuples
(into {} (map vec '[(:a 1) (:b 2)]))

;; sorted-map
(sorted-map-by #(compare (subs %1 1) (subs %2 1))
               "bac" 2
               "abc" 9)
;; => {"bac" 2, "abc" 9}

;; sorted map doesn't distinguish between floats and ints!
(assoc {1 :int}1.0 :float)
;; => {1 :int, 1.0 :float}

(assoc (sorted-map 1 :int)
       1.0 :float)
;; => {1 :float}


;; array-map
(seq (hash-map :a 1 :b 2 :c 3))
;; => ([:c 3] [:b 2] [:a 1])
(seq (array-map :a 1 :b 2 :c 3))
;; => ([:a 1] [:b 2] [:c 3])


;;; Putting it all together (p. 109)
;;; Implement function `pos` that will return position of a value in the collection.
;;; For sequential collections it should return numerical index;
;;; for maps and sets it should return the key.
;;; The function should return _all_ the positions if the value is present multiple times.

(defn index
  "Index given collection by mapping the collection to a sequence of tuples,
  where each tuple is [index, val] - `val` is the original value from the collection"
  [coll]
  (cond
    (map? coll) coll
    (set? coll) (map vector coll coll)
    :else (map vector (range) coll)))

(defn pos [coll val]
  (for [[i v] (index coll)
        :when (= v val)]
    i))

(pos {:a 1} 1)
;; => (:a)
(pos #{:a} :a)
;; => (:a)
(pos [0 1 2 3 0 1 2 3 3] 3)
;; => (3 7 8)

;; we can then modify `pos` to achieve greater level of flexibility
;;=> accepting arbitrary predicate
(defn pos [pred coll]
  (for [[i v] (index coll)
        :when (pred v)]
    i))
(pos even? [0 1 2 3 0 1 2 3 3])
;; => (0 2 4 6)

