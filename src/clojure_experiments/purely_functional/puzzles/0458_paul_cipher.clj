(ns clojure-experiments.purely-functional.puzzles.0458-paul-cipher
  (:require [clojure.string :as str]))
"https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-459-revisiting-the-open-closed-principle/"

;; Treat all letters as uppercase, and convert them to uppercase if needed.
;; The first alphabetical character of the string will not change.
;; All subsequent alphabetical characters are shifted toward Z by the alphabetical position of the preceding alphabetical character.
;; Non-alphabetical characters are left as-is.

(defn shift
  "Shifts character `c` by the alphabetical position
  of its predecessor `p`."
  [p c]
  (if (every? #(Character/isAlphabetic (int %)) [p c])
    (let [b (dec (int \A))]
      (char (+ (mod (+ (- (int c) b)
                       (- (int p) b))
                    (- (int \Z) b))
               b)))
    c))

(shift \A \C)
;; => \D
(shift \T \T)
;; => \N

(defn shift-word [w]
  (apply str (first w)
         (mapv shift w (rest w))))

(defn encode [plain]
  (or (some-> (not-empty plain)
              (str/upper-case)
              shift-word)
      plain))

(encode "") ;=> ""
(encode "a") ;=> "A"
(encode "hello") ;=> "HMQXA"
(encode "newsletter") ;=> "NSBPEQYNYW"
;; this isn't clear from the specification - what should happen to alphabetical chars
;; that are preceeded with non-alphabetical char?
;; (such as 'h' preceeded by the space)
(encode "1 hug") ;=> "1 HCB"


(defn decode [encoded]
  )

(decode "NSBPEQYNYW")

(decode "1 HCB")
