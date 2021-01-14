(ns clojure-experiments.purely-functional.puzzles.0410-nesting
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-410-dont-encode-your-policy/")

(defn nest
  [xs n]
  (map #(repeat n %) xs))

;; Examples
(comment
 (nest [:a :b :c] 2) ;=> ((:a :a) (:b :b) (:c :c))

 (nest [] 10) ;=> ()

 (nest [1 2 3 4] 1) ;=> ((1) (2) (3) (4))

 (nest [1 2 3] 0) ;=> (()()())
 ,)
