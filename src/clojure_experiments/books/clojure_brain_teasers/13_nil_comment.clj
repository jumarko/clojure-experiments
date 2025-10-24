(ns clojure-experiments.books.clojure-brain-teasers.13-nil-comment
  "The `comment` macro actually evaluates and returns `nil`.

  if you are debugging source code and trying to omit an expression,
  the discard reader symbol #_ is a much better choice.

  See also 'Rich Comment Blocks' in Stuart Halloway's block 'Running with Scissors':
  https://www.youtube.com/watch?v=Qx0-pViyIDU&t=1229s)


(map inc [1 2 3 (comment 4) 5])
;; =>
;; 1. Caused by java.lang.NullPointerException
;; Cannot invoke "Object.getClass()" because "x" is null
;; Numbers.java: 1099  clojure.lang.Numbers/ops
;; Numbers.java:  139  clojure.lang.Numbers/inc
;; core.clj:  929  clojure.core/inc

