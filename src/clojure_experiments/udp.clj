(ns clojure-experiments.udp
  "Universal Design Pattern as described in 'Joy of Clojure'."
  (:require [clojure.core :exclude [get]]))

;;; UDP describes 5 fundamental functions: beget, get, put, has?, forget.
;;; We'll implement three most important one: beget, get, put

(defn beget [this proto]
  (assoc this ::prototype proto))

(def child (beget {:child 1} {:parent 10}))

(defn get [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))

(get child :child)
(get child :parent)
(get child :grandparent)


(def put assoc)
(put child :likes :bananas)
