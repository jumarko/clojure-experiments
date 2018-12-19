(ns clojure-repl-experiments.java
  "Anything related to java interop.

  Really cool talk is 'Java Made (Somewhat) Simple' by Ghadi Shayban: https://www.youtube.com/watch?v=-zszF8bbXM0")

;;; instace of two classes with the same name
;;; aren't generally equal because they came from different classloaders!
(defrecord MyInfo [a b c])
(def a (->MyInfo 1 2 3))
(def b (->MyInfo 1 2 3))
(= a b)
;; => true

(defrecord MyInfo [a b c])
(def c (->MyInfo 1 2 3))
(= a c)
;; => false
