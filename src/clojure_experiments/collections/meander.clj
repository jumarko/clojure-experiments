(ns clojure-experiments.collections.meander
  "https://github.com/noprompt/meander/
  See also https://jimmyhmiller.github.io/building-meander-in-meander"
  (:require [meander.epsilon :as m]))


;;; https://github.com/noprompt/meander/#what-can-meander-do
(defn favorite-food-info [foods-by-name user]
  (m/match {:user user
            :foods-by-name foods-by-name}
    {:user
     {:name ?name
      :favorite-food {:name ?food}}
     :foods-by-name {?food {:popularity ?popularity
                            :calories ?calories}}}
    {:name ?name
     :favorite {:food ?food
                :popularity ?popularity
                :calories ?calories}}))

(def foods-by-name
  {:nachos {:popularity :high
            :calories :lots}
   :smoothie {:popularity :high
              :calories :less}})

(favorite-food-info foods-by-name
                    {:name :alice
                     :favorite-food {:name :nachos}})
;; => {:name :alice, :favorite {:food :nachos, :popularity :high, :calories :lots}}

