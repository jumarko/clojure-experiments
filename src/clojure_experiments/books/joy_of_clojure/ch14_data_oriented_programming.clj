(ns clojure-experiments.books.joy-of-clojure.ch14-data-oriented-programming
  (:require [clojure.data :as d]
            [clojure.edn :as edn]
            [clojure-experiments.books.joy-of-clojure.ch07-fp :as ch7]))


;;; Useful discussion of Place-Oriented Programming in the first pages
;;; * Place oriented programming is fundamentally limited - it destroys time and thus information by default (special care needed to preserve the history)
;;; * it mutates the values in place => values, on the other hand, are immutable (even composite types)
;;; * strict separation between code and data leads to awkward data manipulation in languages like Java

;;; Advantages of values (p. 339 - 343)
;;; General advantages:
;;; - Resonable: you don't need to worry about change over tim
;;; - Equality: equal values stay equal foreover
;;; - Inexpensive: no need for _defensive copying_ when you use values
;;; - Flatness: composite value types contains values all the way down
;;; - Sharing: values across multiple threads (or in CACHE) is safe

;; 1. Values can be reproduced
;; E.g. this is just a map - compare to Java HashMap construction which requires code
{:a 1 :b 2}

;; 2. Values can be reproduced and fabricated (p. 339/340)
(rand-int 1024)
;; => 893

;; generating a more complex structure
(def ascii (map char (range 65 (+ 65 26))))
(defn rand-str [sz alphabet]
  (apply str (repeatedly sz #(rand-nth alphabet))))
(rand-str 10 ascii)
;; => "GDJXXSNJMH"

(def rand-sym #(symbol (rand-str %1 %2)))
(def rand-keyword #(keyword (rand-str %1 %2)))

(rand-keyword 10 ascii)
;; => :WZQJOHCINJ
(rand-sym 10 ascii)
;; => LVLUKLBCMQ

;; we can now build composite structures like vectors or maps
(defn rand-vec [& generators]
  (into [] (map #(%) generators)))
(rand-vec #(rand-sym 5 ascii)
          #(rand-keyword 10 ascii)
          #(rand-int 1024))
;; => [FNMXO :TYYTKECQQH 638]

(defn rand-map [sz kgen vgen]
  (into {}
        (repeatedly sz #(rand-vec kgen vgen))))
(rand-map 3
          #(rand-keyword 5 ascii)
          #(rand-int 1000))
;; => {:EOHGE 981, :PYWEE 848, :FYXGR 984}


;; 3. Values facilitate testing
(assert (= [1 2 3] (conj [1 2] 3)))

;; show a diff if the test fails
(d/diff [1 2 3] [1 2 4])
;; => [[nil nil 3] [nil nil 4] [1 2]]


;; 4. Values facilitate debugging
;; Using values, well-placed logging might save you
(defn filter-rising [segments]
  (clojure.set/select
   (fn [{:keys [p1 p2]}]
     (> 0
        (/ (- (p2 0) (p1 0))
           (- (p2 1) (p1 1)))))
   segments))

(filter-rising #{{:p1 [0 0] :p2 [1 1]}
                 {:p1 [4 15] :p2 [3 21]}})
;; => #{{:p1 [4 15], :p2 [3 21]}}


;;; Tagged literals (p. 343 - 347)


#inst "1969-08-18"
;; => #inst "1969-08-18T00:00:00.000-00:00"

;; example from Chapter 7 - we now provide #unit/length data literal
(def distance-reader
  (partial ch7/convert
           {:m 1
            :km 1000
            :cm 1/100
            :mm [1/10 :cm]}))

;; Note: you need to add the tag definiton to data_readers.clj and restart the REPL
#unit/length [1 :km]
;; => 1000

;; #unit/length [1 :gm]
;;=> Invalid unit ':gm' not present in the context.


;; It's possible to update `*data-readers*` at runtime:
(def time-reader
  (partial ch7/convert
           {:sec 1
            :min 60
            :hr [60 :min]
            :day [24 :hr]}))
(binding [*data-readers* {'unit/time time-reader}]
  (read-string "#unit/time [1 :min 30 :sec]"))
;; => 90


;; you can bind `*default-data-reader-fn*` to handle all the other tags
(binding [*default-data-reader-fn* #(-> {:tag %1 :payload %2})]
  (read-string "#nope [:doesnt-exist]"))
;; => {:tag nope, :payload [:doesnt-exist]}


;; EDN and custom tag literals

;; built-in literals are supported by default
(edn/read-string "#inst \"2020-10-01\"")
;; => #inst "2020-10-01T00:00:00.000-00:00"

;; ... but custom literals need explicit options map
#_(edn/read-string "#unit/length [1 :km]")
;; => No reader function for tag unit/length

(edn/read-string
 {:readers {'unit/length distance-reader}}
 "#unit/length [1 :km]")
;; => 1000


;; ... and you can also provide `:default` for edn/read-string
(edn/read-string
 {:readers {'unit/length distance-reader}
  :default vector}
 "#what/the :huh?")
;; => [what/the :huh?]


