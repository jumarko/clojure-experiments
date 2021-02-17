(ns clojure-experiments.books.joy-of-clojure.ch15-performance)


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
