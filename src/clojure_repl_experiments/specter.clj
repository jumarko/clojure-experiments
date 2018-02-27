(ns clojure-repl-experiments.specter
  (:require [com.rpl.specter
             :refer
             [ALL MAP-VALS setval srange transform traverse-all]]))

;;; Understanding Specter: Clojure's missing piece
;;; https://www.youtube.com/watch?v=rh5J4vacG98&feature=youtu.be

;;; 1.1 Clojure
(mapv inc #{1 2 3 4})

;;; 1.2 Specter - preserves data type
(transform ALL inc [1 2 3 4])


;;; 2.2 Specter
(def data [{:a 1 :b 2} {:c 3} {:d 4}])
(transform [ALL MAP-VALS even?] inc data)

;; START
[{:a 1 :b 2} {:c 3} {:d 4}]

;; ALL
{:a 1 :b 2}
{:c 3}
{:d 4}

;; MAP-VALS
1
2
3
4

;; even?
2
4

;; <Navigation complete>
;; inc
3
5

;; <Reconstruct>

;; reverse even?
1
3
3
5

;; reverse MAP-VALS
{:a 1 :b 3}
{:c 3}
{:d 5}

;; reverse ALL
[{:a 1 :b 3} {:c 3} {:d 5}]



;;; http://nathanmarz.com/blog/clojures-missing-piece.html
;;; manipulating data via the composition of navigators should be a fundamental skill for all functional programmers.

(setval [:a (srange 2 4)] [] {:a [1 2 3 4 5]})
;;=> {:a [1 2 5]}

(transform [:a (srange 1 5)] reverse {:a [1 2 3 4 5 6]})
;;=> {:a [1 5 4 3 2 6]}

;; Transducers
;; Using Vanilla Clojure
(transduce
 (comp (map :a) (mapcat identity) (filter odd?))
 +
 [{:a [1 2]} {:a [3]} {:a [4 5]}])
;; => 9

;; The same logic expressed with Specter
(transduce
 (traverse-all [:a ALL odd?])
 +
 [{:a [1 2]} {:a [3]} {:a [4 5]}])
;;=> 9
