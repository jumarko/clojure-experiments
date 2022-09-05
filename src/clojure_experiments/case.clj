(ns clojure-experiments.case
  "Various experiments with `clojure.core/case`.
  It's a macro that can be surprising,
  since the test expressions are supposed to be compile-time literals.")


;; clojure pills screencast
(case 1 (inc 0) "1" (dec 1) "0" :default)

;; this doesn't give you what you think!
(def one 1)
(case 1
  one :one
  :not-one)
;; => :not-one

(case 'one
  one :one
  :not-one)
;; => :one

;; it doesn't work even if you make the def a constant?
(def ^:const real-one 1)
(case 1
  real-one :one
  :not-one)

;; LiSP slack: https://lisp2022.slack.com/archives/C03C3NMCM7T/p1661369906742909
(def one 1)
(case 1 one "one" :else)
;; => :else
(case 1 (1 2) "one" nil)
;; => "one"
(case 1 #{1 2} "one" :else)
;; => :else
(case 1 [1 2] "one" :else)
;; => :else
(case 1 (one 2) "one" :else)
;; => :else

;; Chouser
;; this is explained in the docstring:
;;   Note that since
;;    lists are used to group multiple constants that map to the same
;;    expression, a vector can be used to match a list if needed. The
;;    test-constants need not be all of the same type.

(case 1 (:or 1 2) :low :high)
;; => :low
(case 1 (10 20 30) :low :high)
;; => :high
(case 1 (10 20 1) :low :high)
;; => :low

;; petr.mensik
(def all-statuses {:won {:a 1}
                   :lost-project {:a 2}
                   :lost-invoice {:a 3}})
(def my-status {:a 1})
#_(let [contact-status (case my-status
                         (:won all-statuses) (println "won")
                         (:lost-project all-statuses) (println "lost project")
                         (:lost-invoice all-statuses) (println "lost invoice")
                         nil)]
    (println contact-status))



;;; https://dev.clojure.org/jira/browse/CLJ-2275
;;; case fails for vectors with negative numbers
(case -1 -1 true false)
;;=> true
(case [-1] [-1] true false)
;;=> true
(case (int -1) -1 true false)
;;=> true
(case [(int 1)] [1] true false)
;;=> true
(case [(int -1)] [-1] true false)
;;=> false
