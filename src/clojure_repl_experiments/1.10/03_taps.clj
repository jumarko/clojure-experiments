(ns clojure-repl-experiments.1.10.03-taps)

;;;; taps: https://github.com/clojure/clojure/blob/master/changes.md#23-tap
;;;; https://www.birkey.co/2018-10-26-datafy-and-tap%3E-in-clojure-1.10.html
;;;; https://quanttype.net/posts/2018-10-18-how-i-use-tap.html
;;;; https://www.reddit.com/r/Clojure/comments/89eeiv/what_little_clojure_tricks_did_you_not_realize/?st=joiclx4c&sh=61c992bd
;;;;  As better `def` inside function calls for debugging?
;;;;  https://www.reddit.com/r/Clojure/comments/7wdlq4/first_cut_of_prepl_%E8%B7%AF_clojureclojure86a158d/dtzgr2p/?st=joicndph&sh=e9a3c077

(def context (StringBuilder.))

(defn ->context [x]
  (doto context
    (.append x)))

;; Then let us add above fn to the tapset
(add-tap ->context)
;; Then from any where of our running code, we can do:
(tap> "******* tap start ********\n ")
(tap> "runing.......................\n")
(tap> "******* tap end **********\n ")

;; It will be executed in a separate dedicated thread and will not
;; block or interfere with our running code. Then we print out the context:
(str context)
;; which results in:
;;******* tap start ********
;; runing.......................
;;******* tap end **********

;; Remember to remove the ->context fn once you are done with that session:
(remove-tap ->context)
;; If there is no fn added to the tap, any values you send to tap will be discarded.
(tap> "your magic") ;; your magic will be discarded.





;;; notes:
;;; - functions don't have equals implemented therefore identity is used for remove-tap
;;; - you could use Vars instead of functions in which case it would be two different fns/taps
;;;     (add-tap #'f1)
;;;     (add-tap #'f2)
;;;     (remove-tap #'f1)
;;;     (remove-tap #'f2)




