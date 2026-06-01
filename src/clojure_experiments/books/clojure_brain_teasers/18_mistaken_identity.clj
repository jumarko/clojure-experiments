(ns clojure-experiments.books.clojure-brain-teasers.18-mistaken-identity
  "Tricky case of `case` and identity.
  `case` macro is meant for O(1) checking of _constant_ values which are NOT evaluated.
  In the case below, symbols are NOT evaluated (inside `case`).")

(def photo1 :hero)
(def photo2 :villain)

(defn identify
  [photo]
  (case photo
    photo1 "Our hero!"
    photo2 "The dastardly villain"
    "Unknown"))

;; Both of these are false because identify function above returns "Unknown"
(= (identify photo1) "Our hero!")
(= (identify photo2) "The dastardly villain")

;;; Compare to this
(identify 'photo1)
;; => "Our hero!"
(identify 'photo2)
;; => "The dastardly villain"

;;; Fixing the function
(defn identify
  [photo]
  (case photo
    :hero "Our hero!"
    :villain "The dastardly villain"
    "Unknown"))
(identify photo1)
;; => "Our hero!"


;;; MY NOTE: even declaring vars as constants does NOT work.
(def ^:const photo1 :hero)
(def ^:const photo2 :villain)
(defn identify
  [photo]
  (case photo
    photo1 "Our hero!"
    photo2 "The dastardly villain"
    "Unknown"))
(identify photo1);; => "Unknown"
