(ns clojure-experiments.books.joy-of-clojure.ch14-data-oriented-programming
  (:require [clojure-experiments.books.joy-of-clojure.ch07-fp :as ch7]
            [clojure.data :as d]
            [clojure.edn :as edn]
            [clojure.set :as set]))

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



;;; Simple event sourcing (p. 348 - 357)

;; let's start with a simple snapshot of a state
;; - basebal:
{:ab 5 :h 2 :avg 0.400}

;; this stype is always derived from events which are maps too
{:result :hit}

(defn valid? [event]
  (boolean (:result event)))

;; apply the event to the state
(defn effect [{:keys [ab h] :or {ab 0 h 0}}
              event]
  (let [ab (inc ab)
        h (if (= :hit (:result event))
            (inc h)
            h)
        avg (double (/ h ab))]
    {:ab ab :h h :avg avg}))

(effect {} {:result :hit})
;; => {:ab 1, :h 1, :avg 1.0}

(effect {:ab 599 :h 180}
        {:result :out})
;; => {:ab 600, :h 180, :avg 0.3}

;; it will be nice to validate the effect before applying it:
(defn apply-effect [state event]
  (if (valid? event)
    (effect state event)
    state))
(apply-effect {:ab 600 :h 180 :avg 0.3}
              {:result :hit})
;; => {:ab 601, :h 181, :avg 0.3011647254575707}

;; Finally let's apply a sequence of effects
;; Note: I don't like using anonymous functions like in the book (especially when an error is thrown and you have to investigate the stacktrace)
(defn effect-all [state events]
  (reduce apply-effect state events))

(effect-all {:ab 0 :h 0}
            [{:result :hit}
             {:result :out}
             {:result :hit}
             {:result :out}])
;; => {:ab 4, :h 2, :avg 0.5}

;; let's try 100 random events
(def events (repeatedly
             100
             (fn [] (rand-map
                     1
                     #(-> :result)
                     #(if (< (rand-int 10) 3) ; 30% chance to 'hit'
                        :hit
                        :out)))))
(effect-all {} events)
;; => {:ab 100, :h 29, :avg 0.29}


;; Rewinding is as simple as applying only a subset of events
(effect-all {} (take 50 events))
;; => {:ab 50, :h 13, :avg 0.26}


;; gather historical timeline
(defn fx-timeline [state events]
  (reductions apply-effect state events))
(fx-timeline {} (take 10 events))
;; => ({}
;;     {:ab 1, :h 1, :avg 1.0}
;;     {:ab 2, :h 1, :avg 0.5}
;;     {:ab 3, :h 2, :avg 0.6666666666666667}
;;     {:ab 4, :h 2, :avg 0.5}
;;     {:ab 5, :h 2, :avg 0.4}
;;     {:ab 6, :h 3, :avg 0.5}
;;     {:ab 7, :h 3, :avg 0.4285714285714286}
;;     {:ab 8, :h 4, :avg 0.5}
;;     {:ab 9, :h 4, :avg 0.4444444444444444}
;;     {:ab 10, :h 4, :avg 0.4})


;;; Simulation Testing (p. 352 - 357)

(def PLAYERS #{{:player "Nick" :ability 8/25}
               {:player "Matt" :ability 13/50}
               {:player "Ryan" :ability 19/100}})

(defn lookup [db name]
  ;; note: they use alias `sql` for set in the book
  (first (set/select #(= name (:player %))
                     db)))

(lookup PLAYERS "Nick")
;; => {:player "Nick", :ability 8/25}

(defn update-stats [db event]
  (let [player (lookup db (:player event))
        ;; they used `set/difference` here; I just use `disj`
        less-db (disj db player)]
    (conj less-db
          (merge player (effect player event)))))


(update-stats PLAYERS {:player "Nick" :result :hit})
;; => #{{:player "Matt", :ability 13/50} {:player "Nick", :ability 8/25, :ab 1, :h 1, :avg 1.0}
;;      {:player "Ryan", :ability 19/100} {:player "Nick", :ability 8/25}}

;; => #{{:player "Matt", :ability 13/50}
;;      {:player "Nick", :ability 8/25, :ab 1, :h 1, :avg 1.0}
;;      {:player "Ryan", :ability 19/100}}

(defn commit-event [db event]
  #_(prn "Update db with event: " event)
  #_(prn "db before: " db)
  (let [db-after (dosync (alter db update-stats event))]
    #_(prn "db after: " db)
    db-after)
)

(commit-event (ref PLAYERS) {:player "Nick" :result :hit})
;; => #{{:player "Matt", :ability 26} {:player "Ryan", :ability 19} {:player "Nick", :ability 32, :ab 1, :h 1, :avg 1.0}}

;; now to finally demonstrate simulation testing
(defn rand-event [{ability :ability}]
  (let [able (numerator ability)
        max (denominator ability)]
    (rand-map 1
              #(-> :result)
              #(if (< (rand-int max) able)
                 :hit
                 :out))))

(defn rand-events [total player]
  (take total
        (repeatedly #(assoc (rand-event player)
                            :player
                            (:player player)))))

(rand-events 3 {:player "Nick" :ability 32/100})
;; => ({:result :out, :player "Nick"} {:result :hit, :player "Nick"} {:result :out, :player "Nick"})


;; Now we can use agents to simulate players
;; - our domain maps nicely onto the notion of autonomous actors operating asynchronously

(def agent-for-player
  ;; memoize here allows you to use `player-name` as an index to a table of agents
  ;; without having to explicitly manage and look up agents in the table!
  (memoize
   (fn [player-name]
     (doto (agent [])
         (set-error-handler! #(println "ERROR: " %1 %2))
         (set-error-mode! :fail)))))

(defn feed
  "Apply given event to the database (should be a ref).
  Returns an agent that will perform the update."
  [db event]
  (let [a (agent-for-player (:player event))]
    ;; I think send-off would make more sense if we were making changes to a real database
    (send a
          (fn [state]
            (commit-event db event)
            (conj state event)))))

(defn feed-all [db events]
  (doseq [event events]
    (feed db event))
  db)

(let [db (ref PLAYERS)]
  (feed-all db (rand-events 100 {:player "Nick" :ability 32/100}))
  ;; Thread/sleep is needed otherwise I don't see much updates
  (Thread/sleep 10)
  db)
;; => #<Ref@6fe285c: 
;;      #{{:player "Matt", :ability 13/50}
;;        {:player "Ryan", :ability 19/100}
;;        {:player "Nick", :ability 8/25, :ab 100, :h 33, :avg 0.33}}>

;; you can apply the events manually to check your expectations
(effect-all {} @(agent-for-player "Nick"))
;; this is the result when you experiment with it and invoke it more than once
; (memoize remembers the previous invocations)
; => {:ab 230, :h 40, :avg 0.3076923076923077}


;; top-level `simulate` function to generate events for all players
;; and apply them in "interleaved" order
(defn simulate [total-per-player players]
  (let [events (apply interleave
                      (for [player players] (rand-events total-per-player player)))
        results (feed-all (ref players) events)]
    (apply await (map #(agent-for-player (:player %))
                      players))
    @results))

(simulate 2 PLAYERS)
;; => #{{:player "Matt", :ability 13/50, :ab 2, :h 0, :avg 0.0}
;;      {:player "Nick", :ability 8/25, :ab 2, :h 0, :avg 0.0}
;;      {:player "Ryan", :ability 19/100, :ab 2, :h 0, :avg 0.0}}

(simulate 400 PLAYERS)
;; => #{{:player "Ryan", :ability 19/100, :ab 400, :h 77, :avg 0.1925}
;;      {:player "Matt", :ability 13/50, :ab 400, :h 101, :avg 0.2525}
;;      {:player "Nick", :ability 8/25, :ab 400, :h 125, :avg 0.3125}}

;; you can as well check with effect-all:
(effect-all {} @(agent-for-player "Nick"))

