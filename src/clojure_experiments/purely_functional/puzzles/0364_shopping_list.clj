(ns clojure-experiments.purely-functional.puzzles.0364-shopping-list
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-364-tip-seek-the-model-and-enshrine-it-in-code/"
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

;;; Challenge: total price of a shopping list
;;; shopping list is represented as a vector where items are hash maps like this:
{:item :bread 
 :quantity 1
 :price 3.50}
;;; There's a 10% tax added to teh sum
;;; Write a function that takes a shopping list and returns a map like this:
{:total-before-tax 75.40
 :tax 7.54
 :total-after-tax 82.94}

(s/def ::item keyword?)
(s/def ::quantity pos-int?)
(s/def ::price (s/and decimal? pos?)) ;; to achieve accurate price arithmetic
(s/def ::shopping-item (s/keys :req-un [::item ::quantity ::price]))
(s/def ::shopping-list (s/coll-of ::shopping-item :min-count 1))
(s/def ::total-before-tax ::price)
(s/def ::tax ::price)
(s/def ::total-after-tax ::price)
(s/fdef total-price
  :args (s/cat :shopping-list ::shopping-list)
  :ret (s/keys :req-un [::total-before-tax ::tax ::total-after-tax]))
(defn total-price [shopping-list]
  (let [total (reduce (fn [subtotal {:keys [price]}]
                        (+ subtotal price))
                      0
                      shopping-list)
        tax (* (bigdec 0.10) total)
        total-after-tax (+ total tax)]
    {:total-before-tax total
     :tax tax
     :total-after-tax total-after-tax}))

(comment
  (s/exercise ::shopping-list)

  (def my-shopping-list [{:item :_/., :quantity 2, :price 2.0M}
                         {:item :*/C, :quantity 2, :price 1.0M}
                         {:item :p/_, :quantity 1, :price 3.0M}
                         {:item :y/K, :quantity 2, :price 0.75M}
                         {:item :./!, :quantity 1, :price 1.0M}
                         {:item :m/z, :quantity 2, :price 2.0M}
                         {:item :d/h, :quantity 2, :price 2.0M}
                         {:item :-/E, :quantity 1, :price 0.5M}
                         {:item :C/?, :quantity 2, :price 0.5M}])
  (total-price my-shopping-list)
  ;; => {:total-before-tax 12.75M, :tax 1.275M, :total-after-tax 14.025M}

  ;; exercise to get results
  (s/exercise-fn `total-price 2)
;; => ([([{:item :Z/f, :quantity 1, :price 0.5M}
;;        {:item :./., :quantity 1, :price 2.0M}
;;        {:item :!/+, :quantity 2, :price 2.0M}
;;        {:item :_/x, :quantity 1, :price 1.5M}
;;        {:item :-/., :quantity 2, :price 1.25M}
;;        {:item :F/l, :quantity 2, :price 2.0M}
;;        {:item :K/l, :quantity 2, :price 0.5M}
;;        {:item :-/_, :quantity 1, :price 2.0M}
;;        {:item :z/., :quantity 2, :price 2.0M}
;;        {:item :*/!, :quantity 2, :price 0.5M}
;;        {:item :I/g, :quantity 2, :price 0.5M}
;;        {:item :_/X, :quantity 1, :price 0.5M}
;;        {:item :C/+, :quantity 1, :price 1.0M}
;;        {:item :./H, :quantity 1, :price 2.0M}
;;        {:item :m/x, :quantity 1, :price 0.5M}
;;        {:item :n/*, :quantity 1, :price 2.0M}
;;        {:item :N/M, :quantity 1, :price 2.0M}])
;;      {:total-before-tax 22.75M, :tax 2.275M, :total-after-tax 25.025M}]
;;     [([{:item :U2/g9, :quantity 2, :price 3.0M}
;;        {:item :SX/PU, :quantity 1, :price 3.75M}
;;        {:item :_/P, :quantity 1, :price 0.5M}
;;        {:item :_b/cf, :quantity 2, :price 0.5M}
;;        {:item :e/aP, :quantity 1, :price 0.5M}
;;        {:item :R/A0, :quantity 1, :price 2.0M}
;;        {:item :e/f., :quantity 1, :price 3.0M}
;;        {:item :an/Dz, :quantity 2, :price 2.0M}
;;        {:item :cS/Q, :quantity 1, :price 0.5M}
;;        {:item :zq/f., :quantity 2, :price 1.0M}
;;        {:item :mO/C, :quantity 2, :price 2.0M}
;;        {:item :_2/?, :quantity 1, :price 2.0M}
;;        {:item :+/W, :quantity 2, :price 1.0M}
;;        {:item :?/i, :quantity 1, :price 2.0M}
;;        {:item :c/a, :quantity 2, :price 0.5M}
;;        {:item :o+/WH, :quantity 2, :price 0.5M}])
;;      {:total-before-tax 24.75M, :tax 2.475M, :total-after-tax 27.225M}])

  ;; run stest/check to make sure it works
  (stest/check `total-price)

  ;;
  )
