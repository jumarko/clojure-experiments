(ns clojure-experiments.ground-up.debugging
  "https://aphyr.com/posts/319-clojure-from-the-ground-up-debugging")


(defn perimeter
  "Given a rectangle, returns a vector of its edge lengths."
  [rect]
  [(:x rect)
   (:y rect)
   (:z rect)
   (:y rect)])

(defn frame
  "Given a mat width, and a photo rectangle, figure out the size of the frame
  required by adding the mat width around all edges of the photo."
  [mat-width rect]
  (let [margin (* 2 rect)]
    {:x (+ margin (:x rect))
     :y (+ margin (:y rect))}))

(def failure-rate
  "Sometimes the wood is knotty or we screw up a cut. We'll assume we need a
  spare segment once every 8."
  1/8)

(defn spares
  "Given a list of segments, figure out roughly how many of each distinct size
  will go bad, and emit a sequence of spare segments, assuming we screw up
  `failure-rate` of them."
  [segments]
  (->> segments
       ; Compute a map of each segment length to the number of
       ; segments we'll need of that size.
       frequencies
       ; Make a list of spares for each segment length,
       ; based on how often we think we'll screw up.
       (mapcat (fn [ [segment n]]
                 (repeat (* failure-rate n)
                         segment)))))

(def cut-size
  "How much extra wood do we need for each cut? Let's say a mitred cut for a
  1-inch frame needs a full inch."
  1)

(defn total-wood
  [mat-width photos]
  "Given a mat width and a collection of photos, compute the total linear
  amount of wood we need to buy in order to make frames for each, given a
  2-inch mat."
  (let [segments (->> photos
                      ;; Convert photos to frame dimensions
                      (map (partial frame mat-width))
                      ;; Convert frames to segments
                      (mapcat perimeter))]

    ; Now, take segments
    (->> segments
         ; Add the spares
         (concat (spares segments))
         ; Include a cut between each segment
         (interpose cut-size)
         ; And sum the whole shebang.
         (reduce +))))

;; Let's try
(comment
  (->> [{:x 8
         :y 10}
        {:x 10
         :y 8}
        {:x 20
         :y 30}]
       (total-wood 2)
       (println "total inches:"))
  ;;=> 
  ;; 1. Unhandled java.lang.ClassCastException
  ;;  clojure.lang.PersistentArrayMap cannot be cast to java.lang.Number

  ;;             Numbers.java:  173  clojure.lang.Numbers/multiply
  ;;             Numbers.java: 3829  clojure.lang.Numbers/multiply
  ;;            debugging.clj:   17  clojure-experiments.ground-up.debugging/frame
  ;;            debugging.clj:   13  clojure-experiments.ground-up.debugging/frame
  ;;                 core.clj: 2624  clojure.core/partial/fn
  ;;                 core.clj: 2753  clojure.core/map/fn
  ;;             LazySeq.java:   42  clojure.lang.LazySeq/sval
  ;;             LazySeq.java:   51  clojure.lang.LazySeq/seq
  ;;                  RT.java:  530  clojure.lang.RT/seq
  ;;                 core.clj:  137  clojure.core/seq
  ;;                 core.clj: 2746  clojure.core/map/fn
  ;;             LazySeq.java:   42  clojure.lang.LazySeq/sval
  ;;             LazySeq.java:   51  clojure.lang.LazySeq/seq
  ;;                  RT.java:  530  clojure.lang.RT/seq
  ;;                 core.clj:  137  clojure.core/seq
  ;;                 core.clj:  660  clojure.core/apply
  ;;                 core.clj: 2783  clojure.core/mapcat
  ;;                 core.clj: 2783  clojure.core/mapcat
  ;;              RestFn.java:  423  clojure.lang.RestFn/invoke
  ;;            debugging.clj:   55  clojure-experiments.ground-up.debugging/total-wood
  ;;            debugging.clj:   46  clojure-experiments.ground-up.debugging/total-wood
  ;;                     REPL:  261  clojure-experiments.ground-up.debugging/eval17431
  ;;                     REPL:  255  clojure-experiments.ground-up.debugging/eval17431

  )

;; => we try to multiply number with map
;; FIX it
(defn frame
  "Given a mat width, and a photo rectangle, figure out the size of the frame
  required by adding the mat width around all edges of the photo."
  [mat-width rect]
  (let [margin (* 2 mat-width)]
    {:x (+ margin (:x rect))
     :y (+ margin (:y rect))}))

;; NOW TRY AGAIN!
(comment
  (->> [{:x 8
         :y 10}
        {:x 10
         :y 8}
        {:x 20
         :y 30}]
       (total-wood 2)
       (println "total inches:"))

  ;;=>
  ;; 1. Unhandled java.lang.NullPointerException
  ;; (No message)

  ;; Numbers.java: 1068  clojure.lang.Numbers/ops
  ;; Numbers.java:  153  clojure.lang.Numbers/add
  ;; core.clj:  992  clojure.core/+
  ;; core.clj:  984  clojure.core/+
  ;; protocols.clj:  168  clojure.core.protocols/fn
  ;; protocols.clj:  124  clojure.core.protocols/fn
  ;; protocols.clj:   19  clojure.core.protocols/fn/G
  ;; protocols.clj:   27  clojure.core.protocols/seq-reduce
  ;; protocols.clj:   75  clojure.core.protocols/fn
  ;; protocols.clj:   75  clojure.core.protocols/fn
  ;; protocols.clj:   13  clojure.core.protocols/fn/G
  ;; core.clj: 6804  clojure.core/reduce
  ;; core.clj: 6790  clojure.core/reduce
  ;; debugging.clj:   64  clojure-experiments.ground-up.debugging/total-wood
  ;; debugging.clj:   46  clojure-experiments.ground-up.debugging/total-wood


  )

;; Now we can fix the typo (NPE)
(defn perimeter
  "Given a rectangle, returns a vector of its edge lengths."
  [rect]
  [(:x rect)
   (:y rect)
   (:x rect)
   (:y rect)])

;; AND FINALLY!
(->> [{:x 8
       :y 10}
      {:x 10
       :y 8}
      {:x 20
       :y 30}]
     (total-wood 2)
     (println "total inches:"))
