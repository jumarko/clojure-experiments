(ns clojure-repl-experiments.skyline
  "Skyline problem - motivated by Riso's interview."
  (:require [clojure.spec.alpha :as s]
            [bocko.core :as b]))

;;; Check https://github.com/mfikes/bocko for simple drawing

;;; See https://www.geeksforgeeks.org/the-skyline-problem-using-divide-and-conquer-algorithm/

;; Array of buildings
(def input
  [[1,11,5], [2,6,7], [3,13,9], [12,7,16], [14,3,25],
   [19,18,22], [23,13,29], [24,4,28]])

;; A strip has x coordinate of left side and height 
(def expected-output
  [[1 11] [3 13] [9 0] [12 7] [16 3] [19 18] [22 3] [25 0]])

(s/def ::x-start int?)
(s/def ::x-end int?)
(s/def ::height int?)
(s/def ::building (s/tuple ::x-start ::x-end ::height))
(s/def ::buildings
  (s/coll-of ::building))

(s/def ::strip (s/tuple ::x-start ::height))
(s/def ::strips (s/coll-of ::strip))

(s/fdef skyline
  :args (s/cat :buildings ::buildings)
  :ret ::strips)
;; TODO: implement
(defn skyline [buildings]
  (mapv (comp vec pop) buildings))


;;; Simple drawing stuff
;;; ====================
(defn make-points [input point-scale]
  (let [starting-point [(ffirst input) 0]
        scaled-points (map (fn [[x h]] [(* point-scale x)
                                        (* point-scale h)])
                           ;; starting point isn't in the input itself
                           (cons starting-point input))]
    (->> scaled-points
         (partition 2 1)
         (mapcat (fn [[[start-x start-y] [end-x end-y]]]
                     ;; need to adjust the end point since it's kind of implicit in the
                     ;; and "multiply" points to draw two lines not just one
                   [[[start-x start-y] [end-x start-y]]
                    [[end-x start-y] [end-x end-y]]])))))

(s/fdef draw
  :args (s/cat :strips ::strips))
(defn draw [input]
  (let [point-scale 1
        y-max 39
        points (make-points input point-scale)]
    (doseq [[[start-x start-y] [end-x end-y] :as line] points]
      ;; coordinates are inverted so recompute them
      (let [start-y (- y-max start-y)
            end-y (- y-max end-y)]
        (cond
          (= start-y end-y)
          (b/hlin start-x end-x start-y)

          (= start-x end-x)
          ;; must switch start-y and end-y because coordinates are inverted
          (b/vlin end-y start-y start-x)

          :else
          (throw (ex-info "Invalid coordinations. Can only draw horizontal or vertical lines"
                          {:line line})))))))

(comment

  (make-points expected-output 1)

  (b/clear)

  ;; or make canvas again if necessary
  #_(in-ns 'bocko.core)
  #_(def canvas (@create-canvas-fn color-map raster width height pixel-width pixel-height))

  (draw input)

  (skyline

   input))
