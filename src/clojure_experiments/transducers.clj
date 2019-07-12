(ns clojure-experiments.transducers
  (:require [clojure.core.async :refer [>! <! <!!] :as async]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [net.cgrand.xforms :as x]))

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




;;; Transducers from the ground up - part 2: https://labs.uswitch.com/transducers-from-the-ground-up-the-practice/

;; Composability
(def inc-and-filter (comp (map inc) (filter odd?)))
(def special+ (inc-and-filter +))
(special+ 1 1)
(special+ 1 2)

;; reduce - compare two versions
(reduce special+ 0 (range 10))
(reduce + 0 (filter odd? (map inc (range 10))))

;; transduce makes that explicit
(transduce inc-and-filter + (range 10))

;; a famous "gist" created by Rich Hickey (the inventor of Clojure) to show off the available transducers in the standard library.

(def x-form
  (comp
   (map inc)
   (filter even?)
   (dedupe)
   (mapcat range)
   (partition-all 3)
   (partition-by #(< (apply + %) 7))
   (mapcat flatten)
   (random-sample 1.0)
   (take-nth 1)
   (keep #(when (odd? %) (* % %)))
   (keep-indexed #(when (even? %1) (* %1 %2)))
   (replace {2 "two" 6 "six" 18 "eighteen"})
   (take 11)
   (take-while #(not= 300 %))
   (drop 1)
   (drop-while string?)
   (remove string?)))

(transduce x-form + (vec (interleave (range 18) (range 20))))

;; composition can be applied on top of more composition, allowing programmers to isolate and name concepts consistently.
(def x-clean
  (comp
   (map inc)
   (filter even?)
   (dedupe)
   (mapcat range)))

(def x-filter
  (comp
   (partition-all 3)
   (partition-by #(< (apply + %) 7))
   (mapcat flatten)
   (random-sample 1.0)))

(def x-additional-info
  (comp
   (take-nth 1)
   (keep #(when (odd? %) (* % %)))
   (keep-indexed #(when (even? %1) (* %1 %2)))
   (replace {2 "two" 6 "six" 18 "eighteen"})))

(def x-calculate
  (comp
   (take 11)
   (take-while #(not= 300 %))
   (drop 1)
   (drop-while string?)
   (remove string?)))

(def x-prepare
  (comp
   x-clean
   x-filter))

(def x-process
  (comp
   x-additional-info
   x-calculate))

(def x-form
  (comp
   x-prepare
   x-process))


;; we can reuse transducers with other transports - e.g. core.async:
(def xform (comp (filter odd?) (map inc)))

(defn process [items]
  (let [out (async/chan 1 xform)
        in (async/to-chan items)]
    (async/go-loop []
      (if-some [item (<! in)]
        (do
          (>! out item)
          (recur))
        (async/close! out)))
    (<!! (async/reduce conj [] out))))

(process (range 10))


;; let's use pipeline
(defn process [items]
  (let [out (async/chan (async/buffer 100))]
    (async/pipeline 4 out xform (async/to-chan items))
    (<!! (async/reduce conj [] out))))

(process (range 10))

;; Logging transducers
(defn log [& [idx]]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result el]
       (let [n-step (if idx (str "Step: " idx ". ") "")]
         (println (format "%sResult: %s, Item: %s" n-step result el)))
       (rf result el)))))

(sequence (log) [:a :b :c])

(def ^:dynamic *dbg?* false)

(defn comp* [& xforms]
  (apply comp
         (if *dbg?*
           (->>
            (range)
            (map log)
            (interleave xforms))
           xforms)))

(transduce
 (comp*
  (filter odd?)
  (map inc))
 +
 (range 5))
;; 6

(binding [*dbg?* true]
  (transduce
   (comp*
    (filter odd?)
    (map inc))
   +
   (range 5)))


;; sequence and eduction can be used to apply a transducer chain lazily
(def cnt1 (atom 0))
(let [res (eduction (map #(do (swap! cnt1 inc) %)) (range 1000))]
  (doall (clojure.core/take 10 res))
  @cnt1)

(def cnt2 (atom 0))
(let [res (sequence (map #(do (swap! cnt2 inc) %)) (range 1000))]
  (doall (clojure.core/take 10 res))
  @cnt2)

;; eduction starts a new loop for every sequence operation
(def cnt1 (atom 0))
(let [res (eduction (map #(do (swap! cnt1 inc) %)) (range 10))]
  (conj (rest res) (first res))
  @cnt1)
;; 20

(def cnt2 (atom 0))
(let [res (sequence (map #(do (swap! cnt2 inc) %)) (range 10))]
  (conj (rest res) (first res)) ; (2)
  @cnt2)
;; 10


;;; https://stackoverflow.com/questions/47333668/split-lines-in-clojure-while-reading-from-file
(defn lines-reducible [^java.io.BufferedReader rdr]
  (reify clojure.lang.IReduceInit
    (reduce [this f init]
      (try
        (loop [state init]
          (if (reduced? state)
            @state
            (if-let [line (.readLine rdr)]
              (recur (f state line))
              state)))
        (finally
          (.close rdr))))))

(comment 
  ;; count the length of the string
  (into []
        (comp
         (mapcat #(str/split % #";"))
         (map count))
        (lines-reducible (io/reader "/tmp/work.txt")))
  ;; also possible with line-seq
  (into []
        (comp
         (mapcat #(str/split % #";"))
         (map count))
        (line-seq (io/reader "/tmp/work.txt")))

  ;; Sum the length of all 'splits'
  (transduce
   (comp
    (mapcat #(str/split % #";"))
    (map count))
   +
   (lines-reducible (io/reader "/tmp/work.txt")))
  ;; also possible with line-seq
  (transduce
   (comp
    (mapcat #(str/split % #";"))
    (map count))
   +
   (line-seq (io/reader "/tmp/work.txt")))

  ;; Sum the length of all words until we find a word that is longer than 5
  (transduce
   (comp
    (mapcat #(str/split % #";"))
    (map count))
   (fn
     ([] 0)
     ([sum] sum)
     ([sum l]
      (if (> l 5)
        (reduced sum)
        (+ sum l))))
   (lines-reducible (io/reader "/tmp/work.txt")))
  ;; or with take while
  (transduce
   (comp
    (mapcat #(str/split % #";"))
    (map count)
    (take-while #(> 5 %)))
   +
   (lines-reducible (io/reader "/tmp/work.txt"))))


;;; Inside Transducers (Lambda Island): https://lambdaisland.com/episodes/inside-transducers
(reduce + [1 2 3])

(defn my-into [target src]
  (reduce conj target src))
(my-into #{} [1 2 3])

(reduce (fn [acc x]
          (if (> acc 5)
            (reduced acc)
            (+ acc x)))
        [1 2 3 4 5])
(type (reduced 42))
@(reduced 42)
;; use `unreduced` to be sure that you're dealing with unreduced value
(unreduced (reduced 42))

(defn add-index-prefix
  [[idx res] x]
  [(inc idx) (conj res (str idx ". " x))])
(second
 (reduce add-index-prefix
         [1 []]
         ["wulong" "red" "black" "green"]))
;; let's improve add-index-prefix to not force user to specify initial values and completing step by hand
(defn add-index-prefix
  ([] [1 []])
  ([acc] (second acc))
  ([[idx res] x]
   [(inc idx) (conj res (str idx ". " x))]))
;; now we need to use `transduce`
(transduce identity add-index-prefix ["wulong" "red" "black" "green"])

;; this is interesting: 10 instead of -10!
(transduce identity - 0 [1 2 3 4])
;; => this can be solved with `completing`:
(transduce identity (completing - (fn [x] x)) 0 [1 2 3 4])

;; cgrand functions
(x/count (filter #(= 0 (mod % 3))) (range 1 20))
(x/some (filter #(= 0 (mod % 3))) (range 1 20))
(x/str (comp (map str/upper-case)
             (interpose "-"))
       ["foo" "bar" "baz"])
(into [] x/str ["foo" "bar" "baz"])
;; ultimate is `x/reduce`
(into [] (x/reduce +) [3 5 7])


;;; Introduction to transducers: https://nbviewer.jupyter.org/github/amitramon/clojure-keynotes/blob/master/notebooks/transducers.ipynb

(def iris-data
  [{:sepal_length 5.1 :sepal_width 3.5 :petal_length 1.4 :petal_width 0.2 :species "setosa"}
   {:sepal_length 6.9 :sepal_width 3.1 :petal_length 4.9 :petal_width 1.5 :species "versicolor"}
   {:sepal_length 4.7 :sepal_width 3.2 :petal_length 1.3 :petal_width 0.2 :species "setosa"}
   {:sepal_length 7.1 :sepal_width 3.0 :petal_length 5.9 :petal_width 2.1 :species "virginica"}
   {:sepal_length 4.6 :sepal_width 3.1 :petal_length 1.5 :petal_width 0.2 :species "setosa"}
   {:sepal_length 5.0 :sepal_width 3.6 :petal_length 1.4 :petal_width 0.2 :species "setosa"}
   {:sepal_length 7.0 :sepal_width 3.2 :petal_length 4.7 :petal_width 1.4 :species "versicolor"}
   {:sepal_length 6.5 :sepal_width 2.8 :petal_length 4.6 :petal_width 1.5 :species "versicolor"}
   {:sepal_length 4.9 :sepal_width 3.0 :petal_length 1.4 :petal_width 0.2 :species "setosa"}
   {:sepal_length 5.7 :sepal_width 2.8 :petal_length 4.5 :petal_width 1.3 :species "versicolor"}
   {:sepal_length 6.3 :sepal_width 3.3 :petal_length 6.0 :petal_width 2.5 :species "virginica"}
   {:sepal_length 6.4 :sepal_width 3.2 :petal_length 4.5 :petal_width 1.5 :species "versicolor"}
   {:sepal_length 5.8 :sepal_width 2.7 :petal_length 5.1 :petal_width 1.9 :species "virginica"}
   {:sepal_length 5.5 :sepal_width 2.3 :petal_length 4.0 :petal_width 1.3 :species "versicolor"}
   {:sepal_length 6.3 :sepal_width 2.9 :petal_length 5.6 :petal_width 1.8 :species "virginica"}])

;; first try with usual lazy seqs (my solution)
(->> iris-data
     (filter #(= "virginica" (:species %)))
     (random-sample 0.6)
     (map (fn [{l :petal_length w :petal_width}] {:petal_ratio (/ l w)
                                                  :petal_area (* l w)})))


;; now we transducers - decoupled
(def xf-prepare-data (comp
                      (filter #(= (:species %) "virginica"))
                      (random-sample 0.6)))

(def xf-calculate (comp
                   (map (fn [item] [(:petal_length item) (:petal_width item)]))
                   (map (fn [[l w]] {:petal_ratio (/ l w) :petal_area (* l w)}))))

(def xf (comp xf-prepare-data xf-calculate))    

(transduce xf conj iris-data)


