(ns clojure-experiments.books.clojure-brain-teasers.23-partial-recall
  "remember: in clojure, all funciton parameters are evaluated before the function is invoked.
  this might make it confusing when using functions such as `partial`, `comp`, `complement`, and `juxt`.")

(defn sentence [subject object]
  (str "Is " subject " a " object "?"))

(def planet "Mars")

(def f1 (partial sentence planet))
(def f2 (fn [object] (sentence planet object)))

;; redefine
(def planet "Earth")

(= (f1 "dream") (f2 "dream"))
;; => false

;;; Rationale:

;; `partial` captures the value of `planet` var at the point when it's defined
;; - Remember: in Clojure, all funciton parameters are evaluated before the function is invoked.
(f1 "dream")
;; => "Is Mars a dream?"

;; `fn` is more dynamic and evaluates the value of `planet` var when it's called
(f2 "dream")
;; => "Is Earth a dream?"


(comment
  ;; see current namespace mappings
  (ns-interns *ns*)
  ;; => {f1 #'clojure-experiments.books.clojure-brain-teasers.23-partial-recall/f1,
  ;;     sentence  #'clojure-experiments.books.clojure-brain-teasers.23-partial-recall/sentence,
  ;;     planet #'clojure-experiments.books.clojure-brain-teasers.23-partial-recall/planet,
  ;;     f2 #'clojure-experiments.books.clojure-brain-teasers.23-partial-recall/f2}

  :-)
