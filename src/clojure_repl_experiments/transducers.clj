(ns clojure-repl-experiments.transducers)

;;; Tim Baldridge - Transducers - Episode 1 - Introduction to Transducers
;;; https://www.youtube.com/watch?v=WkHdqg_DBBs
(def data (vec (range 11)))

;; let's implement map easily with reduce
;; note, that this implementation is eager
(defn -map [f coll]
  (reduce
   (fn [acc v]
     (conj acc (f v)))
   []
   coll))
(-map inc data)

;; let's implement filter
(defn -filter [f coll]
  (reduce
   (fn [acc v]
     (if (f v)
       (conj acc v)
       acc))
   []
   coll))
(-filter odd? data)

;; let's compose!
(->> data
     (-map inc)
     (-filter odd?))

;; but there's some complecting here related to `reduce`
(defn -mapping [f]
  (fn [acc v]
    (conj acc (f v))))
(reduce (-mapping inc)
        []
        data)

;; Another complecting is `conj`
;; we're assuming that collection is "conjable"
;; let's add another level of function
(defn -mapping [f]
  (fn [xf]
    (fn [acc v]
      #_(println "mapping: " v " -> " (f v))
      (xf acc (f v)))))
(defn -filtering [f]
  (fn [xf]
    (fn [acc v]
      #_(println "filtering: " v " -> " (f v))
      (if (f v)
        (xf acc v)
        acc))))
;; "rfn" like "reducing function"
(def rfn (-mapping inc))
(def rfn (-filtering odd?))
;; let's compose mapping and filtering together
;; notice that -mapping if applied first, then -filtering
(def rfn (comp (-mapping inc) (-filtering odd?)))
(reduce (rfn conj)
        []
        data)

;; we can use other operations than `conj`
(reduce (rfn +)
        0
        data)

;;; Transducers episode 2
;;; https://www.youtube.com/watch?v=3E5dbAKIS_E
(def rfn (comp (-mapping int)
               (-mapping inc)
               (-filtering odd?)
               (-mapping char)))

(defn string-rf
  [^StringBuilder acc ^Character ch]
  (.append acc ch))

(str (reduce (rfn string-rf)
             (StringBuilder.)
             "Hello World"))
;; But now we're complecting construction of `StringBuilder`
;; with transformation `string-rf` and building the output with `str`
;; => Rich came with great idea to solve this problem with 3-arity function!
;; => Let's build a better string-rf
(defn string-rf
  ;; construction step
  ([] (StringBuilder.))
  ;; completing step
  ([^StringBuilder sb]
   (.toString sb))
  ;; transformation step
  ([^StringBuilder acc ^Character ch]
   (.append acc ch)))
;; we also need to fix `mapping` and `filtering`
(defn -mapping [f]
  (fn [xf]
    (fn
      ([] (xf))
      ([acc] (xf acc))
      ([acc v]
       (xf acc (f v))))))
(defn -filtering [f]
  (fn [xf]
    (fn
      ([] (xf))
      ([acc] (xf acc))
      ([acc v]
       (if (f v)
         (xf acc v)
         acc)))))

(def xform (comp (-mapping int)
               (-mapping inc)
               (-filtering odd?)
               (-mapping char)))


(transduce xform string-rf "Hello World")

;; One cool thing that we can do with this stuff is to use transient vector!
(defn vec-trans
  ([] (transient []))
  ([acc] (persistent! acc))
  ([acc v] (conj! acc v)))
(transduce xform vec-trans "Hello World")



;;; Transducers - Episode 3 - IReduce
;;; https://www.youtube.com/watch?v=7Px1rk3qWGg

;; let's extend the CollReduce protocol
;;
;; This is what makes transducers really fast:
;; -> just function calls, no allocations of lazy seqs
(extend-protocol clojure.core.protocols/CollReduce
  java.io.InputStream
  (coll-reduce [this f init]
    (let [is ^java.io.InputStream this]
      (loop [acc init]
        (let [ch (.read is)]
          (if (= -1 ch)
            acc
            (recur (f acc ch))))))))
(transduce (map char)
           conj
           (java.io.ByteArrayInputStream. (.getBytes "Hello World")))

;; But, we should support early termination!
(extend-protocol clojure.core.protocols/CollReduce
  java.io.InputStream
  (coll-reduce [this f init]
    (let [is ^java.io.InputStream this]
      (loop [acc init]
        ;; notice reduced? usage
        (if (reduced? acc)
          @acc
          (let [ch (.read is)]
            (if (= -1 ch)
              acc
              (recur (f acc ch)))))))))
(transduce (comp (map char)
                 (map #(Character/toUpperCase %))
                 ;; testing early termination with `take`
                 ;; when you look at source code of `take` you'll see that it calls `reduced`
                 (take 3))
           conj
           (java.io.ByteArrayInputStream. (.getBytes "Hello World")))

;; Notes:
;; - Instead of extending protocol CollReduce we can also implement clojure.lang.IReduce - better performance?
;; - coll-reduce function also has 2-arity function (no initial value) - most of the time we shouldn't worry about it



;;; Tim Baldridge Episode 4: Emitting Multiple Items
;;; `cat` itself is a transducers

;; let's start with simple example
(def high-low
  (fn [xf]
    (fn
      ([] (xf))
      ([result] (xf result))
      ([result item]
       (xf
        (xf result (inc item))
        (dec item))))))
(transduce high-low conj [] [1 2 3])

;; first try to implement `cat` ourselves
(def cat
  (fn [xf]
    (fn
      ([] (xf))
      ([result] (xf result))
      ([result coll]
       (reduce xf result coll)))))
(transduce cat conj [] [[1 2 3] [10 20 30] [100 200 300]])

;; but we have a problem here - let's compose with `take`
(def print-stuff
  (map (fn [x]
         (print "-" x "-")
         x)))
(transduce (comp cat print-stuff (take 3)) conj [[1 2] [3 4] [5 6]])
;; you'll see following print in REPL
;; - 1 -- 2 -- 3 -- 5 -
;; that's because of `reduce` called in `cat` transducer;
;; despite the fact that `take` uses `reduced` it's negated by our call to `reduce`

;; we can solve our issues by wrapping the value with another reduced
;; this is what `preserving-reduced` private function in clojure.core does:
(defn ^:private preserving-reduced
  [rf]
  #(let [ret (rf %1 %2)]
     (if (reduced? ret)
       (reduced ret)
       ret)))
(def cat
  (fn [xf]
    (let [pr (preserving-reduced xf)]
      (fn
        ([] (xf))
        ([result] (xf result))
        ([result coll]
         (reduce pr result coll))))))
(transduce (comp cat print-stuff (take 3)) conj [[1 2] [3 4] [5 6]])

;; RESULT: whenever you are outputing multiple values from transducer
;; you must take into account that any of those values might be "reduced"



;;; Tim Baldridge Episode 5: Stateful Transducers
;;; `take` example

;; Let's implement take which stores its state in an atom
;; Notice that atom has to be initialized inside inner function
;; otherwise the transducer created like this `(def take-3 (take 3))` wouldn't be reusable
(defn take [n]
  (fn [xf]
    (let [left (atom n)]
      (fn
        ([] (xf))
        ([result] (xf result))
        ([result item]
         (if (pos? @left)
           (do
             (swap! left dec)
             (xf result item))
           (reduced result)))))))
(transduce (take 3) conj (range 5))
;; let's make it clear that take terminates early
;; -> following sequence throws AssertionError if anybody tries to access
;;    anything behind the fifth element
;; also notice that if `take 5` then error is thrown too (clojure.core/take works in this case)
(transduce (take 3) conj (concat (range 5)
                                 (lazy-seq (assert false))))

;; Atom uses Compare-And-Swap (CAS)
;; which basically means that CPU core running the code
;; has to go grab Cache line, lock it so nobody else can modify item
;; modify the cache line, and then release it
;; It's possible that one CPU core blocks the others.
;; This operation is quite fast, but we can do better!

;; So we can use `volatile!` because we don't need CAS
;; We're good as long as no 2 threads try to run the same transducing step
;; concurrently - this is ensured, because transducers are single-threaded.
;; Note, that two different threads can execute two different transducing
;; steps safely.
(defn take [n]
  (fn [xf]
    ;; notice `volatile!`
    (let [left (volatile! n)]
      (fn
        ([] (xf))
        ([result] (xf result))
        ([result item]
         (if (pos? @left)
           (do
             (vswap! left dec)
             (xf result item))
           (reduced result)))))))
(transduce (take 3) conj (concat (range 5)
                                 (lazy-seq (assert false))))


;;; Transducers episode 6 - cat
;;; https://www.youtube.com/watch?v=1tw5VXTmydQ

;; LazySeq
(type (sequence (map inc) [1 2 3 4]))

;; example of transducer filtering some values
;; and partitioning them
(def xform (comp
            (filter even?)
            (partition-all 2)
            #_(clojure.core/cat)
            (map str)))

;; usage of channels



;;; Transducers episode 7: Core.Async integration
;;; https://www.youtube.com/watch?v=17-o2qCERxg
;;; Tim Baldridge (Clojure Tutorials)

(require '[clojure.core.async :as async])

;; every value that goes in is transformed by transducer
;; put (>!!) locks the channel for small amount of time
;; does its work, and unlocks
(let [c (async/chan 1024 (map inc))]
      (async/>!! c 42)
      (async/<!! c))

;; Transducer runs inside channel lock!
;; - transducers have to be single-threaded
;; - while transducer is running no other channel
;;   can be putting into or taking from the channel
;; => You certainly don't want to do sth. like this:
(let [c (async/chan 1024 (map (fn [x]
                                (Thread/sleep 1000) ; channel is locked for the full second
                                (inc x))))]
  (async/>!! c 42)
  (async/<!! c))


(let [c (async/chan 1024 (mapcat range))]
  (async/>!! c 3)
  [(async/<!! c)
   (async/<!! c)
   (async/<!! c)])

;; notice that you can output multiple items from transducer
;; even if sizer of buffer is only 1!
(let [c (async/chan 1 (mapcat range))]
  (async/>!! c 3)
  ;; but watch this second put to block
  ;; and succeed only after the three values are removed
  (async/put! c 1 (fn [x] (println "done")))
  [(async/<!! c)
   (async/<!! c)
   (async/<!! c)])


;; you can also specify exception exception handler for your transducer
;; Note: otherwise you should never throw exception inside your transducer
;; because it can crash things (your repl :))
;; If you hadn't this feature, the exception would bubble up the call stack
;; and leave the channel locked.
(let [c (async/chan 1
                    (map (fn [x]
                           (assert (odd? x))))
                    ;; exception handler
                    (fn [ex]
                      (println ex)
                      :error))]
  (async/>!! c 2)
  (async/<!! c))


(transduce (map :someCount) + [{:someCount 10} {:someCount 20}])
