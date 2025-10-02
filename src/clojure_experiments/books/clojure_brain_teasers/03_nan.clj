(ns clojure-experiments.books.clojure-brain-teasers.03-nan
  (:require [clojure.math :as math]))

;; ##NaN does not represent any specific number, so comparing two ##NaN values is like
;; comparing two unknown values, and this must be false.
(= ##NaN ##NaN)
;; => false
(== ##NaN ##NaN);; => false

;;; Examples of NaNs
(math/sqrt -1.0)
;; => ##NaN

(/ 0.0 0.0)
;; => ##NaN
