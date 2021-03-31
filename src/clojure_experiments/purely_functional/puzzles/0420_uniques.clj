(ns clojure-experiments.purely-functional.puzzles.0420-uniques
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-420-say-what-you-mean/")


(defn uniques
  "Removes elements that appear twice in the collection."
  [coll]
  (let [freqs (frequencies coll)]
    (remove #(= 2 (get freqs %))
            coll)))

(uniques [])
;;=> ()
(uniques [1 2 3])
;;=> (1 2 3)
(uniques [1 1 2 3])
;;=> (2 3)
(uniques [1 2 3 1 2 3])
;;=> ()
(uniques [1 2 3 2])
;;=> (1 3)
