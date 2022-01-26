(ns clojure-experiments.purely-functional.puzzles.0447-longest-alternating-substring
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-447-domain-model-fit/
  Solutions: https://gist.github.com/ericnormand/642261cb154219ec6c1fb01c6cab33f9")

(defn- multiply-digits [number]
  (apply * (mapv (comp parse-long str) (str number))))

(defn sum-prod
  "Takes one or more numbers as arguments.
  Sum them, then multiply the digits.
  If the answer is one digit long, it’s done.
  If it’s more than one digit, repeat and multiply the digits again."
  [& numbers]
  (let [sum (apply + numbers)]
    (loop [num sum]
      (let [product (multiply-digits num)]
        (if (< 9 product)
          (recur product)
          product)))))

(sum-prod 4)
;; => 4
(sum-prod 10)
;; => 0
(sum-prod 11 12)
;; => 6
(sum-prod 12 16 223)
;; => 0
