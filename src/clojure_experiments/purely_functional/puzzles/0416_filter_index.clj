(ns clojure-experiments.purely-functional.puzzles.0416-filter-index
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-416-why-do-we-program-in-hard-mode/")

(defn filter-index
  "Filters elements of given sequential collection by calling `pred` on corresponding index,
  return only elements for which their index satisfies the predicate."
  [pred coll]
  (keep-indexed #(when (pred %1) %2) coll))

(filter-index even? "abcdefg")
;; => (\a \c \e \g)

