(ns clojure-experiments.visualizations.repl-plot
  "https://github.com/wyegelwel/repl-plot"
  (:require
   [clojure.math :as m]
   [repl-plot.core :as rp]))

;; plot sinus
(let [xs (range 0 10 0.3)
      ys (map #(m/sin %) xs)]
  (rp/plot xs ys :max-width 50 :max-height 20 :x-axis-display-step 10 :precision 2))
;;  1.00 |        **                             * *         
;;  0.90 |      *    *                          *   *        
;;  0.80 |     *                              *              
;;  0.70 |            *                               *      
;;  0.60 |   *                               *               
;;  0.50 |                                                   
;;  0.40 |              *                              *     
;;  0.30 |  *                              *                 
;;  0.20 |                                                   
;;  0.10 |               *                               *   
;;  0.00 |*                               *                  
;; -0.10 |                                                   
;; -0.20 |                 *                              *  
;; -0.30 |                              *                    
;; -0.40 |                  *                                
;; -0.50 |                                                  *
;; -0.60 |                             *                     
;; -0.70 |                    *                              
;; -0.80 |                           *                       
;; -0.90 |                     *    *                        
;; -1.00 |                       **                          
;;        ---------------------------------------------------
;;        0         1         3         5         7         9          
;;        .         .         .         .         .         .          
;;        0         9         9         9         9         9          
;;        0         8         6         4         2         0          


;; histogram
(rp/hist [[:apples 5] [:bananas 2] [:oranges 1]])
;; :apples 5 #####
;; :bananas 2 ##
;; :oranges 1 #
