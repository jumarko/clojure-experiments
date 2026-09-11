(ns clojure-experiments.books.joy-of-clojure.ch18-mistaken-identity
  )

(def photo1 :hero)
(def photo2 :villain)

(defn identify [photo]
  (case photo
    photo1 "Our hero!"
    photo2 "The dastardly villain"
    "Unknown"))

;; Both `identify` calls return "Unknown"
(= (identify photo1) "Our hero!")
;; => false
(= (identify photo2) "The dastardly villain")
;; => false

;; Rationale:
;;;;;;;;;;;;;;;
;; case is specifically used for determining whether an expression matches one of a set
;;   of values in constant time and evaluates the corresponding expression.
;; The key to the puzzle is that case always expects constant values as cases,
;;   and these are not evaluated — they are typically things like numbers, strings,
;;   keywords, or literal collections of these. 


;;; Ways to fix it
(defn identify
  [photo]
  (case photo
    :hero "Our hero!"
    :villain "The dastardly villain"
    "Unknown"))
(= (identify photo1) "Our hero!")
;; => true
(= (identify photo2) "The dastardly villain")
;; => true

;; or
(defn identify
  [photo]
  (cond
    (= photo photo1) "Our hero!"
    (= photo photo2) "The dastardly villain"
    :else "Unknown"))

(= (identify photo1) "Our hero!")
;; => true
(= (identify photo2) "The dastardly villain")
;; => true
