(ns clojure-experiments.purely-functional.puzzles.0466-custom-sort-order
  "https://ericnormand.me/issues/466
  Source: https://edabit.com/challenge/5cefoardyvgEb52JB
  Solutions: https://gist.github.com/ericnormand/0efb967277eed772f2a0dda801927375")


(defn sort-with
  "Sort `xs` according to the order defined by `template`.
  See https://edabit.com/challenge/5cefoardyvgEb52JB for full instructions.

   Note: the original exercise suggests that if the character isn't in the template,
  then sort it alphabetically - Eric doesn't mention this because he tries to make it work on more generic items.
  Thus we don't implement this requirement either."
  [template xs]
  ;; I'd like to use sort-by and just found proper 'key-fn'
  ;; So I think it could be something that returns an index of the element in the template?
  (sort-by (fn [item] (.indexOf (vec template) item))
           xs))


(sort-with [:breakfast :lunch :dinner]     #{:lunch :breakfast :dinner}) ;=> 
;; => (:breakfast :lunch :dinner)

(sort-with [2 3 4 :jack :queen :king :ace] [4 2 4 :king 2])
;; => (2 2 4 4 :king)

(sort-with [] [30 20 10])
;; => (30 20 10)
