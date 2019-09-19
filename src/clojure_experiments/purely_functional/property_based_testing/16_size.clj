(ns clojure-experiments.purely-functional.property-based-testing.16-size
  "Behind the scenes: size
  https://purelyfunctional.tv/lesson/behind-the-scenes-size/"
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clj-http.client :as http]
            [cheshire.core :as json])
  )


;;; What is size?
;;; "Size" is a "measure of range from which values are randomly chosen"
;;; - numbers: smallest is 0, bigger is bigger (further from 0)
;;; - strings: smallest is "", bigger is longer string
;;; - collections: smallest is (), bugger has more elements
;;; - recursive structures: smallest is empty/unnested, bigger is bigger and deeply nested


;;; Some generators don't take 'size' arguments
;;; gen/choose -> choose randomly
;;; elements -> just choose from given elements
;;; one-of -> just select one


;;; Samples:

(gen/sample gen/nat 40)
;; => (0 1 2 3 2 0 4 1 6 8 9 6 6 1 4 15 16 9 3 1 13 17 15 18 6 7 4 8 8 21 13 28 20 20 5 24 19 32 37 2)

;; it gets wrapped after 200
(drop 190 (gen/sample gen/nat 201))
;; => (35 47 116 100 109 139 21 91 170 92 0) ;; notice 0 at the end!

;;; gen/sized:

;; you can inspect how the 'size' works with `gen/sized`
(def gen-size (gen/sized (fn [size] (gen/return size))))
(drop 190 (gen/sample gen-size 210))
;; => (190 191 192 193 194 195 196 197 198 199 0 1 2 3 4 5 6 7 8 9)

;; check `gen/nat` implementation too:
(gen/sized (fn [size] (gen/choose 0 size)))


;;; gen/resize
;; make a new generator based on nat and always use 10 as its size
(def gen-10-nat (gen/resize 10 gen/nat))
;; notice the result doesn't start from 0!
(gen/sample gen-10-nat 10)
;; => (7 5 5 0 3 7 8 5 3 7)

(gen/sample (gen/vector gen-size) 10)
;; => ([] [] [2] [3 3] [4 4 4 4] [] [] [7 7 7 7 7 7 7] [8 8 8 8 8] [9 9 9 9 9 9 9 9])
;; notice that in this example elements in generated vectors always determine the current 'size' parameter value
;; => it measnt that the size is passed to the underlying gen/vector's generator,
;;    NOT gen/vector itself!!!

;; => if we want vectors of custom size we need to use `gen/resize`
(gen/sample (gen/resize 5 (gen/vector gen/nat)) 10)
;; => ([1 5 2] [] [1 4] [] [4 4 3] [] [2] [0] [2 5 3 2] [1 2 3 4 2])


;;; gen/scale (gen/size + gen/resize)
;; let's say I want natural numbers growing faster then the size param
(def gen-fast-nat (gen/scale (fn [size] (* size size size))
                             gen/nat))
(gen/sample (gen/tuple gen/nat gen-fast-nat gen-size))
;; => ([0 0 0] [1 1 1] [2 8 2] [3 6 3] [1 19 4] [0 125 5] [3 4 6] [6 227 7] [8 30 8] [8 355 9])



