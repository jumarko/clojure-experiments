;; COmmented out because core.cache cause problems
;; (ns clojure-experiments.cache
;;   (:require [clojure.core.cache :as cache]))

;; ;;; Very useful cache implementation motivated by the discussion on slack:
;; ;;;  noisesmith the nice thing with core.cache is that it doesn't force a specific storage -
;; ;;;  it just takes a hash-map and returns a new one, and you can put this in a local that propagates
;; ;;;  via recur, or put it in a ref, or an atom, or even a proper db if you serialize it right - it should just work
;; ;;;  (as long as you always use the new hash-map it returns of course)
;; ;;;  noisesmith I use core.cache in front of mongo documents encoded via transit
;; ;;;  noisesmith the core.cache protocols do all the cache ttl logic, and transit handles the serialization, and mongo the storage
;; ;;;  noisesmith also the simplest solution would be to use an atom for your cache,
;; ;;;  but if you start to hit contention slowdowns you could use multiple refs instead of an atom with multiple keys in it


;; ;;; Also, read through the source of clojure.core.cache - it's very instructive

;; (def C (cache/fifo-cache-factory {:a 1, :b 2}))

;; (if (cache/has? C :c)
;;   (cache/hit C :c)
;;   (cache/miss C :c 42))
;; ;;=> {:a 1, :b 2, :c 42}

;; (cache/evict C :b)
;; ;;=> {:a 1}


