(ns clojure-experiments.purely-functional.puzzles.0390-factors-to-strings
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-390-the-elements-of-flow-in-a-repl-environment/"
  (:require [clojure.string :as string]))


;; first version - the easy solution not using power operator, just multiplication "x"
(defn factors->strings
  "Given a number's prime factors returns a string representation of the number and it's factorization such as:
  [2 2 2 3] => \"24 = 2^3 * 3\"."
  [prime-factors]
  (let [n (apply * prime-factors)
        ;; using sorted map to make sure we start with the lowest factor
        factors-freqs (into (sorted-map) (frequencies prime-factors))
        factors-string (string/join " x " prime-factors)]
    factors-string))


(factors->strings [2 2 2 3])
;; => "24 = 2 x 2 x 2 x 3"

;; Now experimenting iteratively outside of any function...
(->> (frequencies [2 2 2 3])
     (into (sorted-map))
     (map (fn [[factor freq]]
            (str factor (when (> freq 1) (str "^" freq)))))
     (string/join " x "))
;; => "2^3 x 3"

;; Wrapping this into the function
(defn factors->strings
  "Given a number's prime factors returns a string representation of the number and it's factorization such as:
  [2 2 2 3] => \"24 = 2^3 * 3\"."
  [prime-factors]
  (let [n (apply * prime-factors)
        factors-string (->> (frequencies prime-factors)
                            ;; using sorted map to make sure we start with the lowest factor
                            (into (sorted-map))
                            (map (fn [[factor freq]]
                                   (str factor (when (> freq 1) (str "^" freq)))))
                            (string/join " x "))]
    (format "%d = %s" n factors-string)))

(factors->strings [2 2 2 3])
;; => "24 = 2^3 x 3"

(factors->strings [7])
;; => "7 = 7"

(factors->strings [2 2 7])
;; => "28 = 2^2 x 7"

(factors->strings [2 2 3 3 7 9 11 13 15 17 19 21])
;; => "32998345380 = 2^2 x 3^2 x 7 x 9 x 11 x 13 x 15 x 17 x 19 x 21"
;; try the same without `sorted-map`
(comment
  (defn factors->strings
    "Given a number's prime factors returns a string representation of the number and it's factorization such as:
  [2 2 2 3] => \"24 = 2^3 * 3\"."
    [prime-factors]
    (let [n (apply * prime-factors)
          factors-string (->> (frequencies prime-factors)
                              (map (fn [[factor freq]]
                                     (str factor (when (> freq 1) (str "^" freq)))))
                              (string/join " x "))]
      (format "%d = %s" n factors-string)))

  (factors->strings [2 2 3 3 7 9 11 13 15 17 19 21])
  ;; => "32998345380 = 7 x 15 x 21 x 13 x 17 x 3^2 x 2^2 x 19 x 11 x 9"
  )

