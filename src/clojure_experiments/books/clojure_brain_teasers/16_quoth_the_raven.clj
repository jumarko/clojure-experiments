(ns clojure-experiments.books.clojure-brain-teasers.16-quoth-the-raven
  "Symbol resolution and quoting.
  RESOURCES:
  - https://clojure.org/reference/evaluation
  - https://clojure.org/reference/special_forms#quote")

(def raven "nevermore")

;; symbols are names that resolve to something:
(= raven "nevermore")
;; => true

;; but sometimes we want the actual name (not resolve it) => quoting:
(= 'raven (symbol "raven"))
;; => true

;; syntax quote produces a fully-qualified symbol in the current namespace:
;; - useful for macros
(= `raven (symbol "clojure-experiments.books.clojure-brain-teasers.16-quoth-the-raven/raven"))
;; => true

