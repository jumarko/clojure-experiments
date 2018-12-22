(ns clojure-experiments.ui-tests
  (:require [etaoin.api :as e]
            [etaoin.keys :as ek]))


;; firefox crashes at `(e/fill driver ...)`
#_(def driver (e/firefox)) ;; here, a Firefox window should appear

(comment 
  (s/def ::text-x (s/coll-of number? :kind vector?))
  (s/def ::text-y (s/coll-of number? :kind vector?))
  (s/def ::text-str string?)
  (s/def ::svg-text (s/keys :req [::text-x ::text-y ::text-str ]))
  (doto (e/chrome)
    ;; let's perform a quick Wiki session
    (e/go "https://en.wikipedia.org/")
    (e/wait-visible [{:id :simpleSearch} {:tag :input :name :search}])

    ;; search for something
    (e/fill {:tag :input :name :search} "Clojure programming language")
    (e/fill {:tag :input :name :search} ek/enter)
    (e/wait-visible {:class :mw-search-results})

    ;; I'm sure the first link is what I was looking for
    (e/click [{:class :mw-search-results} {:class :mw-search-result-heading} {:tag :a}])
    (e/wait-visible {:id :firstHeading})

    ;; let's ensure
    (e/get-url ) ;; "https://en.wikipedia.org/wiki/Clojure"

    (e/get-title ) ;; "Clojure - Wikipedia"

    (e/has-text? "Clojure") ;; true

    ;; navigate on history
    (e/back )
    (e/forward )
    (e/refresh )
    (e/get-title )
    ;; "Clojure - Wikipedia"

    ;; stops browser and HTTP server
    (e/quit )))

