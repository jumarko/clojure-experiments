(ns clojure-experiments.books.clojure-for-finance.ch02)

;;; p.17
(defn generate-prices [lower-bound upper-bound]
  (filter (fn [x] (>= x lower-bound))
          (repeatedly (fn [] (rand upper-bound)))))
(take 10 (generate-prices 12 35))
;; => (15.67234041358418 28.52735350663132 32.38363118954629 30.604375079334943 26.15220165729753 27.81115648002795 19.13959904138829 24.971644849233535 33.47139598234628 22.849835713911116)


(def pricelist (generate-prices 12 35))
(take 25 (->> (map (fn [x y]
                     [x y])
                   (map (fn [x] {:time x}) (iterate inc 0))
                   (map (fn [x] {:price x}) pricelist))
              (map (fn [x] (merge (first x) (second x))))))
;; => ({:time 0, :price 24.562948297924184}
;;     {:time 1, :price 17.840554973504396}
;;     {:time 2, :price 17.419139755105356}
;; ...
;;     {:time 24, :price 17.950735031439976})

;; it's much easier written like this:
(defn generate-timeseries [pricelist]
  (map (fn [x y] {:time x :price y})
       (iterate inc 0)
       pricelist))
(take 25 (generate-timeseries pricelist))
;; => ({:time 0, :price 24.562948297924184}
;;     {:time 1, :price 17.840554973504396}
;;     {:time 2, :price 17.419139755105356}
;; ...
;;     {:time 24, :price 17.950735031439976})
