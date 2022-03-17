(ns clojure-experiments.transducers.codescene-mob
  "Examples from CodeScene team mob session exploring transducers.
  See also a good discussion about transducers: https://groups.google.com/g/clojure/c/9I6MtgOTD0w/m/NiG5PimBCP8J")



(defn my-filter
  "Returns a lazy sequence of the items in coll for which
  (pred item) returns logical true. pred must be free of side-effects.
  Returns a transducer when no collection is provided."
  [pred]
  (fn [rf]
    (prn "filter1: " rf)
    (fn
      ([]
       (prn "filter2 - init")
       (rf))
      ([result]
       (prn "filter2 - result:" result)
       (rf result))
      ([result input]
       (prn "filter2 - rf: " rf)
       (prn "filter2 - result, input: " result input)
       (if (pred input)
         (rf result input)
         result)))))

(defn my-map
  "Returns a lazy sequence consisting of the result of applying f to
  the set of first items of each coll, followed by applying f to the
  set of second items in each coll, until any one of the colls is
  exhausted.  Any remaining items in other colls are ignored. Function
  f should accept number-of-colls arguments. Returns a transducer when
  no collection is provided."
  [f]
  (fn [rf]
    (prn "map1: " rf)
    (fn
      ([]
       (prn "map2 - init")
       (rf))
      ([result]
       (prn "map2 - result:" result)
       (rf result))
      ([result input]
       (prn "map2: " rf)
       (prn "map2 - result, input: " result input)
       (rf result (f input)))
      ([result input & inputs]
       (rf result (apply f input inputs))))))

(def xf
  (comp
   (my-filter odd?)
   (my-map inc)
   #_(take 5)))

(comment
  (into [] xf (range 10))
  ;; => [2 4 6 8 10]

  (transduce xf conj [] (range 10))
  ;; => [2 4 6 8 10]
  ,)

(defn map-odd [f]
  (fn [rf]
    (let [i (atom 0)]
      (fn
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (let [this-i  @i
               xf-input (if (odd? this-i) (f input) input)]
           (swap! i inc)
           (rf result  xf-input)))))))

(comment
  (into [] (map-odd #(+ 100 %)) (range 10)))


;;; `eduction` and `sequence` experiments

(comment

  ;; try to inspect `iter`
  ;; - as soon as you do that you will see the whole thing being printed
  ;; - check `clojure.core/print-sequential` used by Eduction's print-method
  (def iter-edu (eduction xf (range 70)))

  ;; note that `sequence` is in some sense _less lazy_ than `eduction`.
  ;; If you eval this `def` you will immediatelly see some print statements
  ;; in the repl:
  ;;   "map1: " #object[clojure.lang.TransformerIterator$1 0x132cb401 "clojure.lang.TransformerIterator$1@132cb401"]
  ;;   "filter1: " #function[make-it-grow.transducers/my-map/fn--9348/fn--9349]
  ;;   "filter2 - rf: " #function[make-it-grow.transducers/my-map/fn--9348/fn--9349]
  ;;   "filter2 - result, input: " nil 0
  ;;   "filter2 - rf: " #function[make-it-grow.transducers/my-map/fn--9348/fn--9349]
  ;;   "filter2 - result, input: " nil 1
  ;;   "map2: " #object[clojure.lang.TransformerIterator$1 0x132cb401 "clojure.lang.TransformerIterator$1@132cb401"]
  ;;   "map2 - result, input: " nil 1
  (def iter-seq (sequence xf (range 70)))

  ;; now try to get first element of the sequence
  ;; the last thing you will see printed should be 65
  ;; (that's because of chunking (only first 32 elements are evaluated))
  (first iter-seq) ;;   "map2 - result, input: " nil 65
  ;; => 2

  ;; try again and the lazy seq caching will avoid re-computation (no more output printed)
  (first iter-seq)
  ;; => 2


  ;; the same with eduction - you only see the first 65 elements of the range
  ;; being processed
  (first iter-edu)

  ;; but if you try again, everything is re-evaluated
  (first iter-edu)


  ;; eduction/sequence might be handy when you want to combine transducers
  ;; with functions that don't support them out of the box
  (frequencies iter-seq)
  ;; => {70 1, 62 1, 20 1, 58 1, 60 1, 24 1, 46 1, 4 1, 54 1, 48 1, 50 1, 32 1, 40 1, 56 1, 22 1, 36 1, 44 1, 6 1, 28 1, 64 1, 34 1, 12 1, 2 1, 66 1, 68 1, 14 1, 26 1, 16 1, 38 1, 30 1, 10 1, 18 1, 52 1, 42 1, 8 1}

  ;; again, eduction will be re-evaluated every time you call it
  (frequencies iter-edu)
  ;; => {70 1, 62 1, 20 1, 58 1, 60 1, 24 1, 46 1, 4 1, 54 1, 48 1, 50 1, 32 1, 40 1, 56 1, 22 1, 36 1, 44 1, 6 1, 28 1, 64 1, 34 1, 12 1, 2 1, 66 1, 68 1, 14 1, 26 1, 16 1, 38 1, 30 1, 10 1, 18 1, 52 1, 42 1, 8 1}

  ;; a good discussion about transducers: https://groups.google.com/g/clojure/c/9I6MtgOTD0w/m/NiG5PimBCP8J
  ;; Eduction retains the ability to be recomposed with other transducers higher in the function chain
  ;; The following two are nearly equivalent (about 60 msecs)
  (time (transduce (take 1e6) + (eduction (filter odd?) (range))))
  (time (transduce (comp (filter odd?) (take 1e6)) + (range)))

  ;; This will be slower (about 200 msecs)
  (time (transduce (take 1e6) + (sequence (filter odd?) (range))))
  ;; ... but I guess it makes more sense like this:
  (def xs (sequence (filter odd?) (range)))
  (time (transduce (take 1e6) + xs))

  ,)


;; From a good discussion about transducers: https://groups.google.com/g/clojure/c/9I6MtgOTD0w/m/NiG5PimBCP8J
;; Alex miller:  I wanted to mention a couple things that have changed in alpha6:
;;    - Eduction is no longer Seqable and thus the return from eduction is not seqable  (but it is reducible and iterable). You can use iterator-seq to get a chunked seq over the top if you need one. 
;;    - Prior to alpha6, sequence with transformations returned a LazyTransformer. That's gone and now transformations are done using TransformerIterator, which is subsequently wrapped with a (now chunked) iterator-seq.
;;    - There are lots of performance implications due to those changes and I would recommend re-testing any perf test related to sequence or eduction on alpha6 to get a fresh picture as anything pre-alpha6 is not comparable.
;;    - eduction now takes multiple transformations, not just one, and composes them. This is designed for mechanical rewriting (hello tool developers!!) of ->> chains like this:
(def s (range 10))
(->> s (interpose 5) (partition-all 2))
;;to this:
(->> s (eduction (interpose 5) (partition-all 2)))
