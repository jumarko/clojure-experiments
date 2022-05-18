(ns clojure-experiments.purely-functional.puzzles.0468-maxie-and-minnie
  "https://ericnormand.me/issues/468
  Solutions https://gist.github.com/ericnormand/4ca47720a954307739aaeb12682de98a"
  (:require [clojure.string :as str]))

;; I copied my solution from Stephen: https://gist.github.com/ericnormand/4ca47720a954307739aaeb12682de98a?permalink_comment_id=4162663#gistcomment-4162663
;; it's a great solution.

(defn- swaps
  "Generate all possible numbers produced by swapping a couple of digits in given number.
  Zero cannot be in the lead position."
  [num]
  (let [numv (vec (str num))]
    (for [j (range 1 (count numv))
          i (range j)
          :let [swapped (assoc numv
                            i (nth numv j)
                            j (nth numv i))]
          :when (not= \0 (first swapped))]
      (parse-long (str/join swapped)))))

(swaps 12345)
;; => (21345 32145 13245 42315 14325 12435 52341 15342 12543 12354)

(swaps 100)
;; => (100)

(defn swapmaxmin [num]
  (let [s (swaps num)]
    [(apply max s) (apply min s)]))

(swapmaxmin 213)
;; => [312 123]
(swapmaxmin 12345)
;; => [52341 12354]
(swapmaxmin 100)
;; => [100 100]


;; manual investigation
(str 123)
123
213
132

12345

21345
32145
42315
52341

13245
14325
15342

12435
12543

12354


