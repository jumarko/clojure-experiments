(ns clojure-experiments.books.joy-of-clojure.ch11-parallelism
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.core.reducers :as r]
            [clojure-experiments.books.joy-of-clojure.ch10-mutation-and-concurrency :refer [dothreads!]])
  (:import (java.util.regex Pattern)))

;;; Futures (p. 263 - 268)

;; futures represent processes that have yet to be computed
(time (let [x (future (do (Thread/sleep 500) (+ 41 1)))]
        [@x @x])) ; only computed once
"Elapsed time: 504.575508 msecs"
;; => [42 42]

;; Futures example: finds the toal number of occurences of a stirng in a given set of Twitter tweets
(defn feed->zipper [uri-str]
  (->> (xml/parse uri-str)
       zip/xml-zip))

;; normalize to support both RSS and ATOM formats
(defn normalize [feed]
  (if (= :feed (:tag (first feed)))
    feed
    (zip/down feed) ; normalize to a similar XML tree
    ))

(defn feed-children [uri-str]
  (->> uri-str
       feed->zipper
       normalize
       zip/children
       (filter (comp #{:item :entry} :tag))))

;; ... once we have normalized child nodes, we want the title text
(defn title [entry]
  (some->> entry
           :content
           (some #(when (= :title (:tag %)) %))
           :content
           first))

#_(map title (feed-children "http://feeds.feedburner.com/ElixirLang"))

;; ... let's now create a function to count the occurences
(defn count-text-task [extractor txt feed]
  (let [items (feed-children feed)
        re (re-pattern (str "(?i)" txt))]
    (->> items
         (map extractor)
         (mapcat #(re-seq re %))
         count)))

;; try it!
(comment
  (count-text-task
   title ; extractor function
   "Erlang"
   "http://feeds.feedburner.com/ElixirLang")
;; => 0

  (count-text-task
   title ; extractor function
   "Elixir"
   "http://feeds.feedburner.com/ElixirLang")
;; => 39

  ;;
  )


;; What if we want to parallelize the operation for multiple feeds?
(def feeds #{"http://feeds.feedburner.com/ElixirLang"
             "http://blog.fogus.me/feed"
             "https://codescene.com/blog/index.xml"})

;; spread the tasks into futures manually
;; GOTCHA: this runs the serially!
(let [results (for [feed feeds]
                (future (let [_ (println "Counting" feed)
                              result (count-text-task title "Elixir" feed)]
                          (println "Counted" feed ":" result)
                          result)))]
  (println "Start `map deref`")
  (reduce + (map deref results)))
;; you will see this seriali execution:
;;
;; Start `map deref`
;; Counting https://codescene.com/blog/index.xml
;; Counted https://codescene.com/blog/index.xml : 0
;; Counting http://blog.fogus.me/feed
;; Counted http://blog.fogus.me/feed : 0
;; Counting http://feeds.feedburner.com/ElixirLang
;; Counted http://feeds.feedburner.com/ElixirLang : 39

;; ... we need `doall` to achieve desired result
(let [results (doall (for [feed feeds]
                 (future (let [_ (println "Counting" feed)
                               result (count-text-task title "Elixir" feed)]
                           (println "Counted" feed ":" result)
                           result))))]
  (println "Start `map deref`")
  (reduce + (map deref results)))
;; 
;; Start `map deref`
;; Counting http://blog.fogus.me/feed
;; Counting http://feeds.feedburner.com/ElixirLang
;; Counting https://codescene.com/blog/index.xml
;; Counted https://codescene.com/blog/index.xml : 0
;; Counted http://feeds.feedburner.com/ElixirLang : 39
;; Counted http://blog.fogus.me/feed : 0


;; let's build a helper macro:
(defmacro as-futures
  {:style/indent 1} ; this is in 2-spaces factors - see https://docs.cider.mx/cider/indent_spec.html
  [[a args] & body]
  (let [parts (partition-by #{'=>} body)
        [acts _ [res]] (partition-by #{:as} (first parts))
        [_ _ task] parts]
    `(let [~res (for [~a ~args]
                  (future ~@acts))]
       ~@task)))

(defn occurences [extractor tag feeds]
  #_(let [results (for
                    [feed feeds]
                  (future (count-text-task extractor tag feed)))]
    (reduce + (map deref results)))
  ;; Is this really better than the macroexpanded form above?!
  ;; And of course, it suffers from the serial execution issue too!
  (as-futures [feed feeds]
    (count-text-task extractor tag feed)
    :as results
    =>
    (reduce + (map deref results))))

#_(occurences title "released" feeds)
;; => 28

;; I'd just do it like this:
#_(->> feeds
       ;; use `mapv` to trigger execution immediatelly
     (mapv (fn [feed] (future
                        (let [_ (println "Counting" feed)
                              result (count-text-task title "Elixir" feed)]
                          (println "Counted" feed ":" result)
                          result))))
     (map deref)
     (apply + ))



;;; Promises (p. 268 - 271)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def x (promise))
(def y (promise))
(def z (promise))
(comment
  (dothreads! #(deliver z (+ @x @y)))
  (dothreads! #(do (Thread/sleep 2000) (deliver x 52)))
  (dothreads! #(do (Thread/sleep 4000) (deliver y 86)))
  (time @z)
  "Elapsed time: 4003.923663 msecs"
;; => 138
  )


;; callback-style API accepting a callback function (`k`) to be executed after the RPC call is done
(defn feed-items [k feed]
  (k (for [item (filter (comp #{:entry :item} :tag)
                        (feed-children feed))]
           (-> item :content first :content))))
#_(feed-items count "http://blog.fogus.me/feed")
;; => 5

;; you can use promise if you want **blocking behavior**
;; when dealing with callback-style API
#_(let [p (promise)]
  (feed-items #(deliver p (count %))
              "http://blog.fogus.me/feed")
  @p)

;; ... we can also create a generic function
;; for all similar callback-style APIs
(defn cps->fn [f k]
  (fn [& args]
    (let [p (promise)]
      (apply f (fn [x] (deliver p (k x))) args)
      @p)))
(def count-items (cps->fn feed-items count))
#_(count-items "http://blog.fogus.me/feed")
;; => 5




;;; Reducers (p. 273 - 274)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def big-vec (vec (range (* 1000 1000))))

(time (reduce + big-vec))
;; After a few trials the best timeis about 15 ms
;; "Elapsed time: 15.595368 msecs"

(time (r/fold + big-vec))
;; After a few trials the best timeis about 8 ms
;; "Elapsed time: 8.038619 msecs"


;; Try larger bucket size "n"
;; => can't do much better than 7 ms
(time (r/fold 65536 + + big-vec))
;; "Elapsed time: 7.299596 msecs"

;; with 10M elements the difference is larger
(def big-vec (vec (range (* 1000 1000 10))))

;; this one varies a lot - from ~150 ms to ~400 ms
(time (reduce + big-vec))
;; "Elapsed time: 244.825118 msecs"

;; reducers are faster and more consistent
(time (r/fold + big-vec))
;; "Elapsed time: 61.528724 msecs"

