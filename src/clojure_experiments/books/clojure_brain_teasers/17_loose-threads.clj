(ns clojure-experiments.books.clojure-brain-teasers.17-loose-threads
  "https://clojure.org/guides/threading_macros")


(-> 10 inc) ;; => 11

(-> 10 #(+ % 1))
;;    class java.lang.Long cannot be cast to class clojure.lang.ISeq

;;; Explanation:
;; 1. The anonymous function gets rewritten to `fn`
;; - this is the expression that the the macro sees
'(fn [x] (+ x 1))
;; 2. thread macro reorganizes the code into:
'(fn 10 [x] (+ x 1))


;;; Fixes
;; 1.Wrap anonymous function with extra parens
(-> 10 (#(+ % 1))) ;; => 11
;; NOTE: this is effectively the following
((fn [x] (+ x 1)) 10) ;; => 11

;; 2. Better: use `as->`
(as-> 10 $ (+ $ 1));; => 11
;; BUT come one! this is the same as the following!
(-> 10 (+ 1))
;; => 11

