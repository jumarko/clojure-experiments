(ns clojure-experiments.books.joy-of-clojure.ch15-performance
  "See also `clojure-experiments.collections/unfold`."
  (:require [clojure.core.reducers  :as r]))


;;; Type hints

;; let's improve function asum-sq from chapter 12 (p. 295)
(set! *warn-on-reflection* true)

;; The original version was like this.
;; As soon as you load this you'll see a lot of reflection warnings
;; Reflection warning, ...ch15_performance.clj:13:22 - call to static method aget on clojure.lang.RT can't be resolved (argument types: unknown, int).
;; Reflection warning, ...ch15_performance.clj:11:13 - call to static method aset on clojure.lang.RT can't be resolved (argument types: unknown, int, java.lang.Number).
;; Reflection warning, ...ch15_performance.clj:14:5 - call to static method alength on clojure.lang.RT can't be resolved (argument types: unknown).
;; ....
;; Reflection warning, ...ch15_performance.clj:15:21 - call to static method aget on clojure.lang.RT can't be resolved (argument types: unknown, int).
;; form-init5739487266911467216.clj:14 recur arg for primitive local: ret is not matching primitive, had: Object, needed: long
;; Auto-boxing loop arg: ret
;; ...
(defn asum-sq [xs]
  (let [dbl (amap xs i ret
                  ;; Note that I had to add `float` here to workaround this error when trying to call `asum-sq`
                  ;; No matching method aset found taking 3 args
                  ;; (check also macroexpansion of `asum-sq`)
                  (float (* (aget xs i)
                            (aget xs i))))]
    (areduce dbl i ret 0
             (+ ret (aget dbl i)))))

;; let's try to run it in a hot loop
#_(time (dotimes [_ 10000] (asum-sq (float-array [1 2 3 4 5]))))
;; "Elapsed time: 2731.927315 msecs"

;; note that this throws a weird error because types don't align
#_(aset (float-array [1 2]) 0 10)
;; => No matching method aset found taking 3 args
;; you have to use float!
(aset (float-array [1 2]) 0 (float 10))

;; let's try to add a few type hints to avoid reflection across function boundaries
;; (Clojure uses java.lang.Object across function boundaries)
(defn asum-sq [^floats xs]
  (let [^floats dbl (amap xs i ret
                  (float (* (aget xs i)
                            (aget xs i))))]
    (areduce dbl i ret 0
             (+ ret (aget dbl i)))))
;; still a few reflection warnings:
;;=>
;; form-init5739487266911467216.clj:45 recur arg for primitive local: ret is not matching primitive, had: double, needed: long
;; Auto-boxing loop arg: ret

;; but it's now way faster:
(time (dotimes [_ 10000] (asum-sq (float-array [1 2 3 4 5]))));; => nil
;; "Elapsed time: 12.419082 msecs"

;; but there are still some warnings - if you want to use the return value
(.intValue (asum-sq (float-array [1 2 3 4 5])))
;; Reflection warning, /Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/books/joy_of_clojure/ch15_performance.clj:57:1 - reference to field intValue can't be resolved.
;; => 55
(defn ^Float asum-sq [^floats xs]
  (let [^floats dbl (amap xs i ret
                          ;; cast to float to avoid the 'No matching method aset found taking 3 args' error
                          ;; (check the macroexpansion and search for `aset` call)
                          (float (* (aget xs i)
                                    (aget xs i))))]
    ;; coerce to `float` here too otherwise I get the error: class java.lang.Double cannot be cast to class java.lang.Float
    (float (areduce dbl
              i
              ret
              (float 0)
              (+ ret (aget dbl i))))))
(time (.intValue (asum-sq (float-array [1 2 3 4 5]))))
;; => 55

;; my simple example showing that actually you can type-hint ^long or ^double
;; here just using ^long can cut the execution time by 50%
(defn my-hinted-fn [i]
  (dotimes [n 1000000] (* i i)))
(time (my-hinted-fn 101999))
;; "Elapsed time: 24.124804 msecs"
(defn my-hinted-fn [^long i]
  (dotimes [n 1000000] (* i i)))
(time (my-hinted-fn 101999))
;; => nil
;; "Elapsed time: 10.950294 msecs"

;; you could also hint at the point of usage:
(defn asum-sq [^floats xs]
  (let [^floats dbl (amap xs i ret
                          ;; cast to float to avoid the 'No matching method aset found taking 3 args' error
                          ;; (check the macroexpansion and search for `aset` call)
                          (float (* (aget xs i)
                                    (aget xs i))))]
    (float (areduce dbl i ret 0
              (+ ret (aget dbl i))))))

(.intValue (asum-sq (float-array [1 2 3 4 5])))
;; Reflection warning, /Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/books/joy_of_clojure/ch15_performance.clj:82:1 - reference to field intValue can't be resolved.

;; here we type hint at the point of usage
(.intValue ^Float (asum-sq (float-array [1 2 3 4 5])))
;; => 55



;;; Transients

(defn zencat1 [x y]
  (loop [src y ret x]
    (if (seq src)
      (recur (next src) (conj ret (first src)))
      ret)))
(zencat1 [1 2 3] [4 5 6])
;; => [1 2 3 4 5 6]

(time (dotimes[_ 1000000] (zencat1 [1 2 3] [4 5 6])))
; "Elapsed time: 372.23397 msecs"

;; now try with transients
(defn zencat2 [x y]
  (loop [src y ret (transient x)]
    (if (seq src)
      (recur (next src) (conj! ret (first src)))
      (persistent! ret))))

(time (dotimes[_ 1000000] (zencat2 [1 2 3] [4 5 6])))
;; What? it's more than before...
;; "Elapsed time: 445.001502 msecs"

;; that's because we're measuring tiny vectors in a tight loop
;; => measure large vectors instead!
(def bv (vec (range 1e6)))
(first (time (zencat1 bv bv)))
;; "Elapsed time: 74.655156 msecs"
;; => 0
(first (time (zencat2 bv bv)))
;; "Elapsed time: 48.695721 msecs"
;; => 0

;; The difference is still not that large on my computer;
;; try even bigger vectors
(def bvv (vec (range 1e7)))
(first (time (zencat1 bvv bvv)))
;; "Elapsed time: 1534.051347 msecs"
(first (time (zencat2 bvv bvv)))
;; "Elapsed time: 493.242101 msecs"


;; Note: Clojure doesn't allow transients to be modified across threads?
(let [tv (transient [1 2 3])]
  (conj! tv 4)
  (future (conj! tv 5))
  (Thread/sleep 10)
  (persistent! tv))
;; It seems it does!
;; => [1 2 3 4 5]
;; => https://clojure.org/reference/transients
;; In Clojure 1.6 and earlier, transients would detect any (read or write) use from a thread other than the one that created them and throw an exception. That check was removed in 1.7 to allow for more flexible use in frameworks like core.async go blocks that enforce the single-threaded constraint via other means.


;;; Chunked sequences (introduced in Clojure 1.1)
;;; it's a "chunk-at-a-time" model
(def gimme #(do (print \.) %))
;; prints 32 dots
(take 1 (map gimme (range 64)))
;; ... still 32 dots
(take 1 (drop 31 (map gimme (range 64))))
;; ... now its 64 dots
(take 1 (drop 32 (map gimme (range 64))))

;; use seq1 if you want total laziness
(defn seq1 [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))
;; prints only 1 dot
(take 1 (map gimme (seq1 (range 64))));; => (0)


;;; 15.4 Memoization

(def debug (atom false))
(def gcd (memoize
          (fn [x y]
            (when @debug (println "x, y: " x y))
            (cond
              ;; notice that by using `fn` and `recur` we don't leverage cache
              ;; for intermediate results!!!
              (> x y) (recur (- x y) y)
              (< x y) (recur x (- y x))
              :else x))))
(time (gcd 1000645475 56130776629010010))
"Elapsed time: 1098.919443 msecs"
;; => 215
(reset! debug true)
(time (gcd 1000645475 56130776629010010))
"Elapsed time: 0.051336 msecs"
;; => 215

;; Now try to fix the memoized gcd to use case even for intermediate results
(defn gcd [x y]
  (cond
    ;; notice that by using `fn` and `recur` we don't leverage cache
    ;; for intermediate results!!!
    (> x y) (recur (- x y) y)
    (< x y) (recur x (- y x))
    :else x))
(def memo-gcd (memoize gcd))
(time (memo-gcd 1000645475 56130776629010010))
"Elapsed time: 838.084435 msecs"
;; => 215
(time (memo-gcd 1000645475 56130776629010010))
"Elapsed time: 0.06143 msecs"
;; no difference?


;; CacheProtocol - instead of providing a mechanism to invalidate individual cache items
;; we define a flexible set of operations
(defprotocol CacheProtocol
  (lookup [cache e] "Retrieves the item in the cache if it exists.")
  (has? [cache e] "Checks if the item is in the cache.")
  (hit [cache e] "Called when the item is found in the cache. Should return the cache object itself.")
  (miss [cache e ret] "Called when the item isn't found in the cache. Should return the cache object itself."))

;; Note: here's the clojure.core.cache.CacheProtocol
(comment
  (defprotocol CacheProtocol
    "This is the protocol describing the basic cache capability."
    (lookup [cache e]
      [cache e not-found]
      "Retrieve the value associated with `e` if it exists, else `nil` in
   the 2-arg case.  Retrieve the value associated with `e` if it exists,
   else `not-found` in the 3-arg case.")
    (has?    [cache e]
      "Checks if the cache contains a value associated with `e`")
    (hit     [cache e]
      "Is meant to be called if the cache is determined to contain a value
   associated with `e`")
    (miss    [cache e ret]
      "Is meant to be called if the cache is determined to **not** contain a
   value associated with `e`")
    (evict  [cache e]
      "Removes an entry from the cache")
    (seed    [cache base]
      "Is used to signal that the cache should be created with a seed.
   The contract is that said cache should return an instance of its
   own type.")))

(deftype BasicCache [cache]
  CacheProtocol
  (lookup [_ item]
    (get cache item))
  (has? [_ item] (contains? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (BasicCache. (assoc cache item result))))

(def cache (BasicCache. {}))

(lookup (miss cache '(servo) :robot) '(servo))
;; => :robot

;; let's build some helper functions
(defn through [cache f item]
  (if (has? cache item)
    (hit cache item)
    ;; use `delay` to make sure the value is calculated only on first retrieval
    (miss cache item (delay (apply f item)))))

(deftype PluggableMemoization [f cache]
  CacheProtocol
  (has? [_ item] (has? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (PluggableMemoization. f (miss cache item result)))
  (lookup [_ item] (lookup cache item)))


(defn memoization-impl [cache-impl]
  (let [cache (atom cache-impl)]
    (with-meta
      (fn [& args]
        (let [cs (swap! cache through (.f cache-impl) args)]
          @(lookup cs args)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 1000) x))

(def sometimes-slowly (memoization-impl (PluggableMemoization.
                                         slowly
                                         (BasicCache. {}))))
(time [(sometimes-slowly 108) (sometimes-slowly 108)])
;; "Elapsed time: 1004.458937 msecs"
;; => [108 108]

(time [(sometimes-slowly 108) (sometimes-slowly 108)])
;; "Elapsed time: 0.147804 msecs"
;; => [108 108]

(meta sometimes-slowly)
;; => {:cache #atom[#object[clojure_experiments.books.joy_of_clojure.ch15_performance.PluggableMemoization 0x2813d83a "clojure_experiments.books.joy_of_clojure.ch15_performance.PluggableMemoization@2813d83a"] 0x66404756]}
(.cache @(:cache (meta sometimes-slowly)))
;; => #object[clojure_experiments.books.joy_of_clojure.ch15_performance.BasicCache 0x7814d673 "clojure_experiments.books.joy_of_clojure.ch15_performance.BasicCache@7814d673"]
(reset! (:cache (meta sometimes-slowly))
        (PluggableMemoization. slowly (BasicCache. {})))
(time [(sometimes-slowly 108) (sometimes-slowly 108)])
;; "Elapsed time: 1001.756556 msecs"
;; => [108 108]


;;; Coercion
;;;;;;;;;;;;;;

;; factorial without primitives
(defn factorial-a [original-x]
  ;; acc here is primitive because it's defined with a literal number
  ;; but type of x is unknown
  (loop [x original-x acc 1]
    (if (>= 1 x) acc (recur (dec x) (* x acc)))))
(factorial-a 10)
;; => 3628800
(factorial-a 20)
;; => 2432902008176640000
(time (dotimes [_ 1e5]
        (factorial-a 20)))
;; "Elapsed time: 67.290228 msecs"

;; let's coerce x to long to make it fastk
(defn factorial-b [original-x]
  ;; acc here is primitive because it's defined with a literal number
  ;; but type of x is unknown
  (loop [x (long original-x) acc 1]
    (if (>= 1 x) acc (recur (dec x) (* x acc)))))
(time (dotimes [_ 1e5]
        (factorial-b 20)))
;; "Elapsed time: 26.985663 msecs"

;; explicit coercion in `loop` is the same as using "type hint" on the fn arg
(defn factorial-c [^long original-x]
  (loop [x original-x acc 1]
    (if (>= 1 x) acc (recur (dec x) (* x acc)))))
(time (dotimes [_ 1e5]
        (factorial-c 20)))
;; "Elapsed time: 27.574799 msecs"

;; Let's try "unchecked math" as the final optimization
(set! *unchecked-math* true)
(defn factorial-d [^long original-x]
  ;; acc here is primitive because it's defined with a literal number
  ;; but type of x is unknown
  (loop [x original-x acc 1]
    (if (>= 1 x) acc (recur (dec x) (* x acc)))))
;; Note: you switch unchecked-math immediatelly to false but that doesn't mean
;; that following function calls will use "checked math"
;; !!! *unchecked-math* is used during the compilation phase!!!
(set! *unchecked-math* false)
(time (dotimes [_ 1e5]
        (factorial-d 20)))
"Elapsed time: 9.268438 msecs"
(time (dotimes [_ 1e5]
        (factorial-d 21)))
;; Unfortunately, using unchecked math you introduced an error:
(factorial-d 21)
;; => -4249290049419214848


;;; Using double for large factorials

(double Long/MAX_VALUE)
;; => 9.223372036854776E18
Double/MAX_VALUE
;; => 1.7976931348623157E308

(defn factorial-e [^double original-x]
  ;; Here we're using 1.0 literals instead of 1 to get doubles instead of longs
  ;; This also slightly improves performance
  (loop [x original-x acc 1.0]
    (if (>= 1.0 x) acc (recur (dec x) (* x acc)))))
(factorial-e 10.0)
;; => 3628800.0
(factorial-e 20.0)
;; => 2.43290200817664E18 ; less precise than the long version
(factorial-e 30.0)
;; => 2.652528598121911E32
(factorial-e 170.0)
;; => 7.257415615308004E306
(factorial-e 171.0)
;; => ##Inf

;; doubles are still fast:
(time (dotimes [_ 1e5] (factorial-e 20)))
;; "Elapsed time: 10.413068 msecs"


;;; Using auto-promotion for large factorials
;;; promoting operators: +', -', *', inc', dec'

;; notice that we still use ^long on `original-x`
;; because that's just for the iteration - we don't assume anybody to compute
;; factorials for numbers larger than Long/MAX_VALUE
(defn factorial-f [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (*' x acc)))))
(factorial-f 20)
;; => 2432902008176640000
(factorial-f 30)
;; => 265252859812191058636308480000000N
(time (factorial-f 171))
;; "Elapsed time: 0.833 msecs"
;; => 1241018070217667823424840524103103992616605577501693185388951803611996075221691752992751978120487585576464959501670387052809889858690710767331242032218484364310473577889968548278290754541561964852153468318044293239598173696899657235903947616152278558180061176365108428800000000000000000000000000000000000000000N
(time (dotimes [_ 1e5] (factorial-f 20)))
;; "Elapsed time: 33.752078 msecs"



;;; Reducibles
;;;;;;;;;;;;;;;

(defn empty-range? [start end step]
  (or (and (pos? step) (>= start end))
      (and (neg? step) (<= start end))))

(defn lazy-range [i end step]
  (lazy-seq
   (if (empty-range? i end step)
     nil ; nil terminates lazy seq
     (cons i (lazy-range (+ i step)
                         end
                         step)))))
(lazy-range 5 10 2)
;; => (5 7 9)
(lazy-range 6 0 -1)
;; => (6 5 4 3 2 1)
(range 6 0 -1)
;; => (6 5 4 3 2 1)

(reduce conj [] (lazy-range 6 0 -1))
;; => [6 5 4 3 2 1]
;; is the same as this?
(vec (lazy-range 6 0 -1))
;; => [6 5 4 3 2 1]

(reduce + 0 (lazy-range 6 0 -1))
;; => 21


;; instead of paying the penalty for using lazy seqs when we don't need them
;; let's built a function that returns a function that acts like `reduce`.
;; => you just call it to extract the elements of the collection
;; See also `clojure-experiments.collections/unfold`
(defn reducible-range [start end step]
  (fn [reducing-fn init]
    (loop [result init, i start]
      (if (empty-range? i end step)
        result
        (recur (reducing-fn result i)
               (+ i step))))))

(def countdown-reducible (reducible-range 6 0 -1))
(countdown-reducible conj [])
;; => [6 5 4 3 2 1]
(countdown-reducible + 0)
;; => 21

(time ((reducible-range 1000000 0 -1)
       + 0))
;; After several invocations:
;;   "Elapsed time: 18.307556 msecs"

;; the duration is much longer!
(time (reduce + 0(lazy-range 1000000 0 -1)))
;; "Elapsed time: 105.026483 msecs"


;;; can we call map, filter, et al on a reducable?
(defn half [x]
  (/ x 2))
(half 4)
;; => 2
(half 7)
;; => 7/2

(defn sum-half [result input]
  ;; notice + is the reducing function here and it's hardcoded
  (+ result (half input)))
(reduce sum-half 0 (lazy-range 0 10 2))
;; => 10

(comment
  (reduce sum-half 0 (lazy-range 0 10 2))
  ;; => 10
  )

((reducible-range 0 10 2) sum-half 0)
;; => 10

;; let's make it more generic by not hardcoding the reducing function (+)
(defn half-transformer [f1]
  (fn f1-half [result input]
    (f1 result (half input))))
((reducible-range 0 10 2) (half-transformer +) 0)
;; => 10
((reducible-range 0 10 2) (half-transformer conj) [])
;; => [0 1 2 3 4]

;; ... but `half` is still hardcoded!
;; => we need another wrapper on top of the previous one.
(defn mapping [mapping-fn]
  (fn map-transformer [rf]
    ;; this is "reducing function"
    (fn map-rf [result input]
      (rf result (mapping-fn input)))))

((reducible-range 0 10 2) ((mapping half) +) 0)
;; => 10
((reducible-range 0 10 2) ((mapping half) conj) [])
;; => [0 1 2 3 4]
((reducible-range 0 10 2) ((mapping list) conj) [])
;; => [(0) (2) (4) (6) (8)]

;; note that we can try the same thing with `map` now:
((reducible-range 0 10 2) ((map half) conj) [])
;; => [0 1 2 3 4]


;; mapping produces a collection of the same size as the input
;; `filtering` is a way how to produce a smaller collection
(defn filtering [pred]
  (fn filter-transformer [rf]
    (fn filter-rf [result input]
      (if (pred input)
        (rf result input)
        result))))
#_((reducible-range 0 10 1) ((filtering any?) conj) [])
;; => [0 1 2 3 4 5 6 7 8 9]
((reducible-range 0 10 1) ((filtering even?) conj) [])
;; => [0 2 4 6 8]

;; we can now combine both filtering and mapping because both return transformers
((reducible-range 0 10 2)
 ;; Notice that we combine "reducing functions" here: `((filtering #(not= %2)) con)` produces the `filter-rf` reducing function
 ;; which is passed as an arg to `map-transformer` and it produces the `map-rf` reducing function;
 ;; which is finally called by `reduciable-range` when traversing the collection
 ((filtering #(not= % 2))
  ((mapping half) conj))
 [])
;; => [0 2 3 4]

;; .. and we can switch their order
((reducible-range 0 10 2)
 ((mapping half)
  ((filtering #(not= % 2)) conj))
 [])
;; => [0 1 3 4]


(defn mapcatting [map-fn]
  (fn mapcat-transformer [rf]
    (fn mapcat-rf [result input]
      ;; Notice calling `map-fn` here and treating the result as reducible
      ;; - there's no explicit concatenation
      (let [reducible (map-fn input)]
        (reducible rf result)))))

(defn and-plus-ten [x]
  (reducible-range x (+ 11 x) 10))
((and-plus-ten 5) conj [])
;; => [5 15]

((reducible-range 0 10 2)
 ((mapcatting and-plus-ten) conj)
 [])
;; => [0 10 2 12 4 14 6 16 8 18]
;; compare to simple mapping
((reducible-range 0 10 2)
 ((mapping and-plus-ten) conj)
 [])
;; => [#function[clojure-experiments.books.joy-of-clojure.ch15-performance/reducible-range/fn--23900] #function[clojure-experiments.books.joy-of-clojure.ch15-performance/reducible-range/fn--23900] #function[clojure-experiments.books.joy-of-clojure.ch15-performance/reducible-range/fn--23900] #function[clojure-experiments.books.joy-of-clojure.ch15-performance/reducible-range/fn--23900] #function[clojure-experiments.books.joy-of-clojure.ch15-performance/reducible-range/fn--23900]]


;; let's create r-map and r-filter to use them in a way similar to map and filter
;; Note: the code common between `r-map` and `r-filter` can be factored out;
;; this is what `clojure.core.reducers/reducer` does
(defn r-map [mapping-fn reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((mapping mapping-fn) reducing-fn) init)))

(defn r-filter [filter-pred reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((filtering filter-pred) reducing-fn) init)))

(def our-final-reducible
  (r-filter #(not= % 2)
            (r-map half (reducible-range 0 10 2))))
(our-final-reducible conj [])
;; => [0 1 3 4]


;;; Let's measure the performance effect
(comment
  
  (require '[criterium.core :as crit])

  ;; lazy seqs
  (crit/quick-bench (reduce + 0 (filter even? (map half (lazy-range 0 (* 10 1000 1000) 2)))))
  ;; Execution time mean : 1.796338 sec

  ;; lazy chunked seqs
  (crit/quick-bench (reduce + 0 (filter even? (map half (range 0 (* 10 1000 1000) 2)))))
  ;; Execution time mean : 464.296606 ms

  ;; reducibles
  (crit/quick-bench ((r-filter even? (r-map half (reducible-range 0 (* 10 1000 1000) 2)))
                     +
                     0))
  ;; Execution time mean : 392.990063 ms
  
  ,)


;;; Integrating reducibles with Clojure reduce

;; just my experiment with IReduce and IReduceInit
(def myri (reify
            clojure.lang.IReduce
            (reduce [_ rf]
              (loop [acc 0]
                (if (< acc 10) (recur (inc acc))
                  acc)))
            clojure.lang.IReduceInit
            (reduce [_ rf init]
              (loop [acc init]
                (if (< acc 10)
                  (recur (inc acc))
                  acc)))))
(reduce identity myri)
;; => 10
(reduce identity 13 myri)
;; => 13


;; let's try reducers
(defn core-r-map [mapping-fn core-reducible]
  (r/reducer core-reducible (mapping mapping-fn)))

(defn core-r-filter [filter-pred core-reducible]
  (r/reducer core-reducible (filtering filter-pred)))

(reduce conj []
        (core-r-filter #(not= % 2)
                       (core-r-map half [0 2 4 6 8])))
;; => [0 1 3 4]

;; Compare performance? Does using the ColLReduce protocol make it slower?
(time ((reducible-range 1000000 0 -1)
       + 0))
;; "Elapsed time: 34.490845 msecs"
(time (reduce + 0 (core-r-map identity (range 1e6))
       ))
;; "Elapsed time: 43.655903 msecs"

;; Implement your own 'core reducible'
(defn reduce-range [reducing-fn init start end step]
  (loop [result init, i start]
    (if (empty-range? i end step)
      result
      (recur (reducing-fn result i)
             (+ i step)))))

(require '[clojure.core.protocols :as protos])
(defn core-reducible-range [start end step]
  (reify protos/CollReduce
    (coll-reduce [this reducing-fn init]
      (reduce-range reducing-fn init start end step))
    (coll-reduce [this reducing-fn]
      (if (empty-range? start end step)
        (reducing-fn)
        (reduce-range reducing-fn start (+ start step) end step)))))
(time ((reducible-range 1e7 0 -1)
       + 0))
;; => 5.0000005E13
;; "Elapsed time: 575.320062 msecs"

(time (reduce + 0
              (core-reducible-range 1e7 0 -1)))
;; => 5.0000005E13
;; "Elapsed time: 558.873644 msecs"



;;; `clojure.core.reducers/fold`
(defn core-f-map [mapping-fn core-reducible]
  (r/folder core-reducible (mapping mapping-fn)))
(defn core-f-filter [filter-pred core-reducible]
  (r/folder core-reducible (filtering filter-pred)))

(r/fold + (core-f-filter #(not= % 2)
                         (core-f-map half [0 2 4 6 8])))
;; 1 + 3 + 4
;; => 8

;; reducers have their own version of map and filter
(r/fold + (r/filter #(not= % 2)
                         (r/map half [0 2 4 6 8])))
;; => 8

;; r/fold doesn't take an initial value but instead calls 'combining-fn' with zero args
(r/fold (fn
          ([] (println "called") 100)
          ([a b] (+ a b)))
        (range 10))
;; => 145
;; above is the same as this:
(r/fold (constantly 100) ; combining fn
        (fn [a b] (+ a b)) ; reducing fn
        (range 10))

;; `r/monoid` can be used to be explicit about the initial value having the existing function
;; - the monoid implementation is eactly as the two-arity arg fn shown above
(r/fold (r/monoid + (constantly 100)) (range 10))
;; => 145

;; combining fn is the same as reducing fn for operations like +
;; but it's different for producting vectors (conj vs into)
;; ... this is incorrect
(r/fold 4
        (r/monoid conj (constantly []))
        conj
        (vec (range 10)))
;; => [0 1 [2 3 4] [5 6 [7 8 9]]]

;; ... and this is correct
(r/fold 4
        (r/monoid into (constantly []))
        conj
        (vec (range 10)))


;; reducers performance
(def big-vector (vec (range 0 1e7 2)))
(comment
  (require '[criterium.core :as crit])
  (crit/quick-bench
   (r/fold + (core-f-filter even? (core-f-map half big-vector))))
  ;;              Execution time mean : 62.433853 ms
  )
