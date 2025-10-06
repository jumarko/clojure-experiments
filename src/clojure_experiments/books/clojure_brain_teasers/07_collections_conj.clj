(ns clojure-experiments.books.clojure-brain-teasers.07-collections-conj)

;;; conj behaves differently for vectors vs lists
;;; It depends on what's the most efficient way for the data structure.

;; list -> prepended
;; - inserting at the beginning always takes a constant amount of time—you make a new
;;   link that simply refers to the head of the existing chain. 
(conj '(:colosseum :vatican) :pantheon :trevi-fountain)
;; => (:trevi-fountain :pantheon :colosseum :vatican)

;; vector -> appended
(conj [:colosseum :vatican] :pantheon :trevi-fountain)
;; => [:colosseum :vatican :pantheon :trevi-fountain]


;; For fun, try sets => no particular order
(conj #{:colosseum :vatican} :pantheon :trevi-fountain)
;; => #{:vatican :trevi-fountain :colosseum :pantheon}
