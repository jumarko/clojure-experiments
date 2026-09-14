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
(defn encode [keyword plain-message]
  (apply str
         (mapv encode-char (apply concat (repeat keyword)) plain-message)))
(encode "scones" "meetmebythetree")
;; => "egsgqwtahuiljgs"

(defn decode-char [k m]
  (let [[k0 m0] (mapv #(- (int %) (int \a))
                      [k m])]
    ;; this is the only difference from `encode-char` -> we do `(- (int m0) (int k0))` instead of `(+ (int k0) (int m0))` 
    (char (+ (mod (- (int m0) (int k0))
                  26)
             (int \a)))))

(defn decode [keyword encoded-message]
  (apply str
         (mapv decode-char (apply concat (repeat keyword)) encoded-message))
  )

(decode "scones" "egsgqwtahuiljgs")
;; => "meetmebythetree"


;; REFACTOR: get rid of the duplication between `decode-char` and `encode-char` 

(defn coding-char [k m op]
  (let [[k0 m0] (mapv #(- (int %) (int \a))
                      [k m])]
    (char (+ (mod (op (int m0) (int k0))
                  26)
             (int \a)))))

(defn encode-char [k m]
  (coding-char k m +))
(encode "scones" "meetmebythetree")
;; => "egsgqwtahuiljgs"

(defn decode-char [k m]
  (coding-char k m -))
(decode "scones" "egsgqwtahuiljgs")
;; => "meetmebythetree"
