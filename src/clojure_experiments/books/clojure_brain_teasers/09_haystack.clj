(ns clojure-experiments.books.clojure-brain-teasers.09-haystack)

(def haystack
  (shuffle (conj (range 100) :needle)))
(contains? haystack :needle)
;; => false

;; that's because haystack is a list/vector
(type haystack)
;; => clojure.lang.PersistentVector
(some #(= :haystack %) haystack)
(contains? haystack 19)
;; => true
(get haystack 19) ; the index of the ':needle' element will be different every time because of `shuffle`
;; => :needle


;;; Ok, but how to find it??

;; maybe use a set
(contains? (set haystack) :needle)
;; => true

;; at worst, linear search
(some #{:needle} haystack) ; NOTE: some doesn't use a chunked sequence so it's lazier than `filter` below
;; => :needle
(first (filter #{:needle} haystack))
;; => :needle
