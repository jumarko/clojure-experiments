(ns clojure-repl-experiments.visualizations.jutsu
  (:require [jutsu.core :as j]))

;;; See https://github.com/hswick/jutsu
;;This will start up a httpkit server and open the jutsu client in your default browser.

(defn start-server []
  (j/start-jutsu!))

(comment
  ;;This will start up a httpkit server and open the jutsu client in your default browser.
  (start-server)

  ;;Adds a graph to the jutsu client
  (j/graph!
   "foo"
   [{:x [1 2 3 4]
     :y [1 2 3 4]
     :mode "markers"}])

  ;;To do realtime updates of a graph
  (j/update-graph!
   "foo"
   {:data {:y [[4]] :x [[5]]}
    :traces [0]})

  ;;You can even view matrix like datasets
  (j/dataset! "dataset-foo"
              [[1 2 3 4]
               [1 2 3 4]
               [1 2 3 4]
               [1 2 3 4]])

  (j/graph! "Line Chart"
            [{:x [1 2 3 5]
              :y [6 7 8 9]
              :type "scatter"}])

  (j/graph! "Bar Chart"
            [{:x ["foo" "bar" "foobar"]
              :y [20 30 40]
              :type "bar"}])
  
  (j/graph! "Pie Chart"
            [{:values [19 26 55]
              :labels ["Residential" "Non Residential" "Utility"]
              :type "pie"}])


  (j/graph! "3D Scatter"
            [{:x (take 100 (repeatedly #(rand)))
              :y (take 100 (repeatedly #(rand)))
              :z (take 100 (repeatedly #(rand)))
              :type "scatter3d"
              :mode "markers"}]
            {:width 600
             :height 600})

  )
