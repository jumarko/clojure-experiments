(ns clojure-experiments.books.joy-of-clojure.ch06
  (:require [cljol.dig9 :as cljol]))

;;; lazy quick sort (p. 133) - This is a gem!
(defn sort-parts [work]
  (lazy-seq
   (loop [[part & parts] work]
     (if-let [[pivot & xs] (seq part)]
       (let [smaller? #(< % pivot)]
         (recur (list*
                 (filter smaller? xs)
                 pivot
                 (remove smaller? xs)
                 parts)))
       (when-let [[x & parts] parts]
         (cons x (sort-parts parts)))))))

(defn lazy-qsort [xs]
  (sort-parts (list xs)))

(lazy-qsort [2 1 4 3])

(defn rand-ints [n]
  (take n (repeatedly #(rand-int n))))

#_(lazy-qsort (rand-ints 20))

(def numbers (rand-ints 1000000))

#_(time (sort numbers))
;;=> "Elapsed time: 1650.853321 msecs"

#_(time (lazy-qsort numbers))
;;=> "Elapsed time: 0.062903 msecs"
#_(time (doall (take 1000 (lazy-qsort numbers))))
;;=> "Elapsed time: 417.165721 msecs"
#_(time (doall (take 10000 (lazy-qsort numbers))))
;;=> "Elapsed time: 861.250399 msecs"
;; approaching 100,000 numbers we can see we have similar running time to native sort with full 10^6 numbers
#_(time (doall (take 100000 (lazy-qsort numbers))))
;;=> "Elapsed time: 1988.791992 msecs"
;; finally, the whole sequence is significantly slower than than native sort
#_(time (doall (lazy-qsort numbers)))
;; "Elapsed time: 14839.150604 msecs"
 


;;; Structural sharing

;; list
(def baselist (list :barnabas :adam))
(def lst1 (cons :willie baselist))
(def lst2 (cons :phoenix baselist))

;; the next parts of both lists are identical
(= (next lst1) (next lst2))
;; => true
(identical? (next lst1) (next lst2))
;; => true
#_(cljol/view lst1)


;; Let's try to build a simple tree to demonstrate
;; how structural sharing works
{:val 5 :L nil :R nil} ; use this when a single item is added to an empty list

(defn xconj
  "Builds up the tree by adding given value to the tree."
  [t v]
  (cond
    (nil? t) {:val v :L nil :R nil}))

(xconj nil 5)
;; => {:val 5, :L nil, :R nil}

;; that's fine but we want more than just a single item
(defn xconj
  "Builds up the tree by adding given value to the tree."
  [t v]
  (cond
    (nil? t) {:val v :L nil :R nil}
    (< v (:val t)) {:val (:val t)
                    :L (xconj (:L t) v)
                    :R (:R t)}
    :else {:val (:val t)
           :L (:L t)
           :R (xconj (:R t) v)}))

;; we'll need a better way to print the tree
(defn xseq [t]
  (when t
    (concat (xseq (:L t))
            [(:val t)]
            (xseq (:R t)))))

(def tree1 (xconj (xconj (xconj nil 5) 3)
                  2))
(xseq tree1)
;; => (2 3 5)

(xseq (xconj tree1 7))
;; => (2 3 5 7)


;;; Laziness

;; simple `steps` function
(defn rec-step [[x & xs]]
  (if x
    [x (rec-step xs)]
    []))
(rec-step [1 2 3 4])
;; => [1 [2 [3 [4 []]]]]

;; But this yields StackOverflowError
#_(def res (rec-step (range 20000)))
;; Can we fix it with loop-recur?
;; => I cannot find a way to do it since it's added inside the vector


;; rest vs. next (p. 126)
(def very-lazy (-> (iterate #(do (print \.) (inc %)) 1)
                   rest rest rest))
;;=> prints two dots ..


(def less-lazy (-> (iterate #(do (print \.) (inc %)) 1)
                   next next next))
;;=> prints two dots too?!

(println (first very-lazy))
;;=> prints ".4"
(println (first less-lazy))
;;=> prints ".4" too! (although it should print just "4")


;; Now try to fix rec-step with lazy-seq macro
(defn lz-rec-step [s]
  (lazy-seq
   (if (seq s)
     ;; notice using `first` and `rest` instead of destructuring!
     [(first s) (lz-rec-step (rest s))]
     [])))

(lz-rec-step [1 2 3 4])
#(prn (class %))
;; => (1 (2 (3 (4 ()))))

;; not it works but don't try to print/inspect it!
;; (it would throw StackOverflowError again due to the printer)
(def xs (doall (lz-rec-step (range 20000))))


;; Simpler lazy-seq example
(defn simple-range [i limit]
  (lazy-seq
   ;; nil returned by when will terminate the range construction
   (when (< i limit)
     (cons i (simple-range (inc i) limit)))))

(simple-range 1 10)

;; Loosing your head (p. 128)
(comment
  ;; this will work although takes some time
  (let [r (range 1e9)]
    (first r)
    (last r))
  ;; this throws OOM, although takes a long time because GC is busy trying to clear some garbage
  (let [r (range 1e9)]
    (last r)
    (first r))
  ;;=> java.lang.OutOfMemoryError: Java heap space
  ;;
  )

(defn simple-range [i limit]
  (lazy-seq
   ;; nil returned by when will terminate the range construction
   (when (< i limit)
     (print ".")
     (cons i (simple-range (inc i) limit)))))
;; prints 3 dots
(def rrr (rest (rest (rest (simple-range 1 10)))))
;; prints 4 dots
(def nnn (next (next (next (simple-range 1 10)))))


(defn simple-range [i limit]
  (lazy-seq
   ;; nil returned by when will terminate the range construction
   (when (< i limit)
     (print ".")
     ;; notice you cannot use `conj` because get StackOverflowError in the `take 10` below
     (cons i (simple-range (inc i) limit)))))
(take 10 (simple-range 1 1e20))
;; => (1 2 3 4 5 6 7 8 9 10)


(defn simple-range2 [i limit]
  (lazy-seq
   ;; nil returned by when will terminate the range construction
   (when (< i limit)
     (print ".")
     ;; notice you cannot use `conj` because get StackOverflowError in the `take 10` below
     (conj (simple-range2 (inc i) limit) i))))
;; StackOverflowError
#_(take 10 (simple-range2 1 1e20))


;;; Infinite sequences (p. 129)
(defn triangle
  "Returns a 'triangle number' for given n."
  [n]
  (/ (* n (+ n 1))
     2))
(triangle 10)
;; => 55

;; build a sequence of the first 10 triangle numbers
(map triangle (range 1 11))
;; => (1 3 6 10 15 21 28 36 45 55)


;; but by defining a sequence of ALL triangle numbers we can then use more interesting queries
(def tri-nums (map triangle (iterate inc 1))) ; why they don't use `range`?

(take 10 tri-nums)
;; => (1 3 6 10 15 21 28 36 45 55)

(take 10 (filter even? tri-nums))
;; => (6 10 28 36 66 78 120 136 190 210)

;; What Gauss found:
(nth tri-nums 99)
;; => 5050

;; converge on 2
(double (->> (map / tri-nums)
             (take 1000)
             (reduce +)))
;; => 1.998001998001998

;; first 2 greater than 10,000
(take 2 (drop-while #(< % 10000)
                    tri-nums))
;; => (10011 10153)



;;; force and delay (p. 130)
(defn defer-expensive
  "Perform the 'then' part only if a truthy value is returned."
  [cheap expensive]
  (if-let [good-enough (force cheap)]
    good-enough
    (force expensive)))

(defer-expensive
 (delay :cheap)
 (delay (do (Thread/sleep 5000)) :expensive))
;; => :cheap

;; this will block for 5 seconds
#_(defer-expensive
  (delay false)
  (delay (do (Thread/sleep 5000)) :expensive))


;; More complicated usage of `force` and `delay` (p. 131)
;; - you can implement a version of lazy seq of triangular numbers

(defn inf-triangles [n]
  {:head (triangle n)
   :tail (delay (inf-triangles (inc n)))})

(defn head [l] (:head l))
(defn tail [l] (force (:tail l)))

(def tri-nums (inf-triangles 1))

(head tri-nums)
;; => 1

(head (tail tri-nums))
;; => 3

(head (tail (tail tri-nums)))
;; => 6


;; now use `head` and `tail` to build higher-level functions

(defn taker [n l]
  (loop [t n, src l, ret []]
    (if (zero? t)
      ret
      (recur (dec n) (tail src) (conj ret (head src))))))

(defn nthr [l n]
  (if (zero? n)
    (head l)
    (recur (tail l) (dec n))))

(taker 10 tri-nums)

(nthr tri-nums 99)
;; => 5050
