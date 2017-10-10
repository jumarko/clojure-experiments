(ns clojure-repl-experiments.sequences
  (:require [criterium.core :as c]))

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
