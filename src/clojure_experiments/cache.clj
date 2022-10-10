;; COmmented out because core.cache cause problems
(ns clojure-experiments.cache
  (:require [clojure.core.cache :as cache]
            [clojure.core.memoize :as memo]))

;;; Very useful cache implementation motivated by the discussion on slack:
;;;  noisesmith the nice thing with core.cache is that it doesn't force a specific storage -
;;;  it just takes a hash-map and returns a new one, and you can put this in a local that propagates
;;;  via recur, or put it in a ref, or an atom, or even a proper db if you serialize it right - it should just work
;;;  (as long as you always use the new hash-map it returns of course)
;;;  noisesmith I use core.cache in front of mongo documents encoded via transit
;;;  noisesmith the core.cache protocols do all the cache ttl logic, and transit handles the serialization, and mongo the storage
;;;  noisesmith also the simplest solution would be to use an atom for your cache,
;;;  but if you start to hit contention slowdowns you could use multiple refs instead of an atom with multiple keys in it


;;; Also, read through the source of clojure.core.cache - it's very instructive

(def C (cache/fifo-cache-factory {:a 1, :b 2}))

(if (cache/has? C :c)
  (cache/hit C :c)
  (cache/miss C :c 42))
;;=> {:a 1, :b 2, :c 42}

(cache/evict C :b)
;;=> {:a 1}

(def C1 (cache/fifo-cache-factory {:a 1, :b 2}))
(cache/through-cache C1 :c (fn [k] (get {:c 10} k)))
;; => {:a 1, :b 2, :c 10}

(defn my-get [x]
  (Thread/sleep 1000)
  x)
(def fetch-data (memo/ttl my-get :ttl/threshold 10000))
(comment

  (time (fetch-data 10))
  "Elapsed time: 1004.518406 msecs"
  ;; => 10
  (time (fetch-data 10))
  ;;"Elapsed time: 0.080276 msecs"
  ;; after 10 more seconds
  (time (fetch-data 10))
  ;; "Elapsed time: 1003.009729 msecs"

  ,)



;;; core.memoize - custom cache key
;;; https://github.com/clojure/core.memoize/blob/master/docs/Using.md#overriding-the-cache-keys
(defn- my-cache-key [args]
  (prn "cache-key args: " args)
  (drop 2 args))

(defn
  ^{:clojure.core.memoize/args-fn my-cache-key}
  cache-key
  "ahoj"
  [a b c]
  (println "calling!" a b c)

  )

(meta #'cache-key)
;; => {:clojure.core.memoize/args-fn #function[clojure.core/partial/fn--5908] ...

(def memo-cache-key
  ;; note that we must pass the var ref here
  (memo/ttl #'cache-key :ttl/threshold 10000))

(memo-cache-key 1 2 3)
(memo-cache-key 10 20 3)
(memo-cache-key 10 30 3)
(memo-cache-key 10 20 30)

