(ns clojure-experiments.books.living-clojure.training.week4-katas
  "See https://github.com/gigasquid/wonderland-clojure-katas")


;;; Day 1: Alphabet cipher: https://github.com/gigasquid/wonderland-clojure-katas/tree/master/alphabet-cipher
(def substitution-chart )
(defn encode-char [k m]
  (let [[k0 m0] (mapv #(- (int %) (int \a))
                      [k m])]
    (char (+ (mod (+ (int k0) (int m0))
                  26)
             (int \a)))))
(encode-char \s \m)
;; => \e
(defn encode [keyword message]
  (apply str
         (mapv encode-char (apply concat (repeat keyword)) message)))
(encode "scones" "meetmebythetree")
;; => "egsgqwtahuiljgs"


