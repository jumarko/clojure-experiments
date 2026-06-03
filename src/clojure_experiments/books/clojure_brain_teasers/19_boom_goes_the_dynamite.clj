(ns clojure-experiments.books.clojure-brain-teasers.19-boom-goes-the-dynamite
  "This technique of wrapping an evaluation into a function for later
  execution is called a _thunk_ in functional programming.
  The origin of “thunk” is a joke referring to the past tense of 'think',
  which doesn’t actually make much sense—it’s really a wrapper around future thinking to do.

  Below, Every call to `concat` creates a new delayed computation (lazy-seq), 
  wrapping the prior one and creating a long chain of pending work.
  When the first element is needed, Clojure has to evaluate the whole staircase (chain)
  of pending computations to get the first value.

  See also: https://stuartsierra.com/2015/04/26/clojure-donts-concat/")

(defn doubled
  "Returns a sequence of all i's from 0 to n (exclusive), doubled.
  Ex; (doubled 3) ;; (0 0 1 1 2 2)"
  [n]
  (loop [i 0
         output ()]
    (if (< i n)
      (recur (inc i)
             ;; NOTE: `concat` is the problem here
             (concat output [i i]))
      output)))
(first (doubled 50000))
;; => StackOverflowError!


;; Suggested fix: `conj` + vector
(defn doubled-take2
  [n]
  (loop [i 0
         output []]
    (if (< i n)
      (recur (inc i) (conj output i i))
      output)))
(take 10 (doubled-take2 50000))
;; => (0 0 1 1 2 2 3 3 4 4)

;;; My implementation
(defn my-doubled
  "Returns a sequence of all i's from 0 to n (exclusive), doubled.
  Ex; (doubled 3) ;; (0 0 1 1 2 2)"
  [n]
  (mapcat (fn [x] [x x]) (range n)))
(take 10 (my-doubled 50000))
;; => (0 0 1 1 2 2 3 3 4 4)

