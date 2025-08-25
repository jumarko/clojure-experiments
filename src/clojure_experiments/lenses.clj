(ns clojure-experiments.lenses
  (:require
   [lentes.core :as l]
   [tick.core :as t]
   [clojure.string :as str]
   [medley.core :as m]))

;;; Functional Lenses in Practice - Aaron Iba
;;; https://www.youtube.com/watch?v=8K4IdE89IRA
;;; Uses this library: https://github.com/funcool/lentes
;;; - docs here: https://cljdoc.org/d/funcool/lentes/1.4.0-SNAPSHOT/doc/user-guide
;;; Also uses juxt/tick: https://github.com/juxt/tick
;;; Motivation:
;;; ----------
;;; When dealing with external data such as JSON APIs, you might be forced
;;; to convert between external data representation and your clojure data format
;;; (examples below).
;;; To do this, we often write conversion functions.

;; lenses demo
(def str<>long (l/units parse-long str))
(def str<>tick-date (l/units t/date str))

(l/focus str<>long "42")
;; => 42
(l/over str<>long inc "42") ; notice it increments the string as if it was an integer!
;; => "43"

(l/focus str<>tick-date "2025-06-04")
;; => #time/date "2025-06-04"
(l/over str<>tick-date t/inc "2025-06-04")
;; => "2025-06-05"

;; lenses demo #2 - some custom lenses (I'm simulating Aaron's helper functions)
(defn -<>nth-token
  "Given an index and a separator,
  returns a lense that focuses on the nth token of string (state)
  assuming the tokens are separate by the separator."
  [idx separator]
  (l/lens
   (fn [s] (nth (str/split s (re-pattern separator))
                idx))
   (fn [s f] (str/join separator
                       (update (str/split s (re-pattern separator))
                               idx f)))))
(def <>first-name (-<>nth-token 0 " "))
(def <>last-name (-<>nth-token 1 " "))

(def full-name "John McCarthy")
(l/focus <>first-name full-name)
;; => "John"
;; NOTE: this is showing a new operations - put!
(l/put <>first-name "Jenny" full-name)
;; => "Jenny McCarthy"
(l/put <>last-name "McEnroe" full-name)
;; => "John McEnroe"


;; data from JSON api
(def api-person
  {:id "42"
   :birthday "1927-09-04"
   :profile {:full-name "John McCarthy"
             :address {:street "885 Allardice Way"
                       :city "Stanford"
                       :state "CA"
                       :zip-code "94305"}}})

(def app-person
  {:person/id 42
   :person/dob (t/date "1927-09-04")
   :person/first-name "John"
   :person/last-name "McCarthy"
   :person/zip "94305"})

;; Now, to convert api-person to app-person, we need a composed lens:
;; (this is my implementation, not theirs)
(defn- assoc-update
  [map-template s update-f]
  (reduce-kv (fn [result k value-lense]
               (l/put value-lense (get (update-f k) k) result))
             s
             map-template))

(defn -<>associative [map-template]
  (l/lens
   (fn [s] (reduce-kv (fn [result k value-lense]
                        (assoc result k (l/focus value-lense s)))
                      {}
                      map-template))
   (fn [s f] (assoc-update map-template s f))))

(def <>person
  (-<>associative
   {:person/id (comp (l/key :id) str<>long)
    :person/dob (comp (l/key :birthday) str<>tick-date)
    :person/first-name (comp (l/in [:profile :full-name])  <>first-name)
    :person/last-name (comp (l/in [:profile :full-name]) <>last-name)
    :person/zip (l/in [:profile :address :zip-code])}))

(l/focus <>person api-person)
;; => #:person{:id 42,
;;             :dob #time/date "1927-09-04",
;;             :first-name "John",
;;             :last-name "McCarthy",
;;             :zip "94305"}

(def p (l/focus <>person api-person))
(def p' (assoc p :person/first-name "Jenny"))

(l/put <>person p' api-person)
;; => {:id "42",
;;     :birthday "1927-09-04",
;;     :profile
;;     {:full-name "Jenny McCarthy", ; notice UPDATED full-name
;;      :address
;;      {:street "885 Allardice Way",
;;       :city "Stanford",
;;       :state "CA",
;;       :zip-code "94305"}}}
