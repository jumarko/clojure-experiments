(ns clojure-experiments.purely-functional.puzzles.0360-waldo
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-360-tip-fold-left-vs-fold-right/")


;;; fold left vs. fold right

;; this is fold right: we first traverse (recurse) the list then apply the function from the right.
;; My note: this is very weird description and not how the things really work!?
(defn eager-map [f ls]
  (if (empty? ls)
    ()
    (cons (f (first ls))
          (map f (rest ls)))))
;; => not tail position
;; => eager
(eager-map inc (range 10))
;; => (1 2 3 4 5 6 7 8 9 10)

;; Now making it lazy
(defn lazy-map [f ls]
  (lazy-seq
   (if (empty? ls)
     ()
     (cons (f (first ls))
           (map f (rest ls))))))
;; => lazy and tail call optimized
(lazy-map inc (range 10))
;; => (1 2 3 4 5 6 7 8 9 10)


;; reduce is left  => left fold:
;; calls f first, then recurses:
(defn my-reduce [f init ls]
  (if (empty? ls)
    init
    (recur f
           (f init (first ls))
           (rest ls))))
(my-reduce + 0 (range 10))
;; => 45



;;; Challenge: Where's Waldo:
;;; Given a vector of vectors representing a grid, find a given value and the path into the grid
;;; that will get to it.

(defn wheres-waldo [val grid]
  (->> grid
       (map-indexed
        (fn [i nested-vector]
          ;; indexOf returns -1 if the item isn't found
          [i (.indexOf nested-vector val)]))
       (filter (fn [[_top-level-index nested-index]]
                 (not= -1 nested-index)))
       first))

(wheres-waldo :W ;; look for :W
              [[:A :B :C]
               [:D :E :F]
               [:G :H :I]
               [:J :K :L]
               [:M :N :O]
               [:P :Q :R]
               [:S :T :U]
               [:V :W :X]
               [:Y :and :Z]])
;;=> [7 1]

(comment
  
  ;; The "big" idea is to use `.indexOf`: https://stackoverflow.com/questions/4830900/how-do-i-find-the-index-of-an-item-in-a-vector
  (->> [[:A :B :C]
        [:D :E :F]
        [:G :H :I]
        [:J :K :L]
        [:M :N :O]
        [:P :Q :R]
        [:S :T :U]
        [:V :W :X]
        [:Y :and :Z]]
       (map-indexed
        (fn [i nested-vector]
          ;; indexOf returns -1 if the item isn't found
          [i (.indexOf nested-vector :W)])
        )
       (filter (fn [[first-index second-index]]
                 (not= -1 second-index)))
       first)

  ;;
  )
