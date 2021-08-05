(ns clojure-experiments.clojure-inside-out.v03-code
  "https://learning.oreilly.com/videos/clojure-inside-out/9781449368647/9781449368647-video152788")

;;; metadata
(def v [1 2 3])

(def trusted-v (with-meta v {:source :trusted}))

(:source (meta trusted-v))
;; => :trusted

(:source (meta v))
;; => nil


;;; multiple arities
(defn larger
  ([x] x)
  ([x y] (if (> x y) x y))
  ([x y & more] (apply larger (larger x y) more)))



;;; Exercise at the end of the Code Part 2 (29:40)
;;; Take StringUtils.isBlank function and rewrite it in Clojure

