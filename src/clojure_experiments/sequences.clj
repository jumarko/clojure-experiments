(ns clojure-experiments.sequences
  (:require [clojure.string :as str]
            [criterium.core :as c]
            [tesser.core :as t]
            [tesser.simple :as ts])
  (:import java.io.File))

;;; lazy sequence: http://insideclojure.org/2015/01/02/sequences/
(defn my-map [f coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (cons (f (first s)) (my-map f (rest s))))))

;; this is macroexpanded form
(defn my-map-expanded [f coll]
  (new
   clojure.lang.LazySeq
   #(let*
        [temp__5459__auto__ (seq coll)]
      (if temp__5459__auto__
        (do
          (let*
              [s temp__5459__auto__]
            (cons (f (first s)) (my-map f (rest s)))))))))

(my-map inc (range 10))

;; (def small-map (into {} (map (fn [i] [i i]) (range 3000))))
;; (def big-map (into {} (map (fn [i] [i i]) (range 1e7))))
;; (c/quick-bench (get small-map (rand-int 1e4)))
;; (c/quick-bench (get big-map (rand-int 1e8)))



;;; Clojure - Convert File Path to Tree: https://stackoverflow.com/questions/49515858/clojure-convert-file-path-to-tree

;; very interesting solution from leetwinski
(defn as-tree [data]
  (map (fn [[k vs]] (cons k (as-tree (keep next vs))))
       (group-by first data)))

(->> (File. ".")
     file-seq
     (map #(.getPath %))
     (filter #(str/ends-with? % ".clj"))
     (map #(str/split % (re-pattern File/separator)))
     as-tree
     first)
;;=> ("." 
;;     ("src" 
;;       ("playground" 
;;         ("core.clj"))) 
;;     ("test" 
;;       ("playground" 
;;         ("core_test.clj"))) 
;;     ("project.clj"))

;; another solution from Alan Thompson
(defn accum-tree
  "Accumulates a file path into a map tree"
  [file-elem-tree path-str]
  (let [path-elems (str/split path-str #"/")
        key-seq    (butlast path-elems)
        file-name  (last path-elems)]
    (assoc-in file-elem-tree key-seq file-name)))

(comment
  (dotest
   (let [file-strings ["resources/data/2012/05/02/low.xml"
                       "resources/data/2012/05/01/low.xml"]]
     (is= (reduce accum-tree {} file-strings)
          {"resources"
           {"data"
            {"2012"
             {"05"
              {"02" "low.xml",
               "01" "low.xml"}}}}}))))

;;; tesser for parallel forlds
(comment
  (time (ts/reduce + 0 (range 10000000)))
  ;; => 49999995000000
  ;; "Elapsed time: 1109.460566 msecs"

  (time (reduce + 0 (range 10000000)))
  ;; => 49999995000000
  ;; "Elapsed time: 67.243529 msecs"

  (time (reduce + 0 (range (int 1e9))))
  ;; "Elapsed time: 13938.97033 msecs"

  (defn- part-range [top threads part-index]
    (let [part-n (int (/ top threads))
          start (* part-index part-n)
          end (* (inc part-index) part-n)]
      (range start end)))

  (time
   (let [top 1e9
         threads 6]
     (->> (range threads)
          (map (fn [i] (future (reduce + 0 (part-range top threads i)))))
          (map deref)
          (reduce + 0))))
  ;; 12 threads: "Elapsed time: 3500.113004 msecs"
  ;; 6 threads: "Elapsed time: 3441.50413 msecs"


  ,)


;;; remove item from a vector
;;; https://stackoverflow.com/questions/1394991/clojure-remove-item-from-vector-at-a-specified-location/1409560
(def a [1 2 3 4 5])
(comment
  
  (time (dotimes [n 100000]
          (vec (concat (take 2 a)
                       (drop 3 a)))))
  ;; "Elapsed time: 102.644533 msecs"

  (time (dotimes [n 100000]
          (vec (concat (subvec a 0 2)
                       (subvec a 3)))))
  ;; "Elapsed time: 73.179752 msecs"
  (time (dotimes [n 100000]
          (into (subvec a 0 2)
                (subvec a 3))))
  
  ;; "Elapsed time: 51.902917 msecs"

  ;;
  )


;; hiredman: `for` is a swiss army knife
(def xml {:content ""})
(for [maybe-x (tree-seq map? :content xml)
      :when (= (:tag maybe-x) :x)
      maybe-y (:content maybe-x)
      :when (= (:tag maybe-y) :y)
      ]
  (do))

