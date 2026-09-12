(ns clojure-experiments.books.clojure-brain-teasers.24-abrac-or-dabra
  "Using a binding introduced by destructuring within destructuring itself
  might produce unexpected results.
  This is internal implementation detail and it changes between Clojure 1.12 vs 1.13")


(def flip {:head :tail, :tail :head})

(let [empty {}

      left nil, right nil
      {:keys [left right] :or {left :head, right (flip left)}} empty
      right1 right


      left nil, right nil
      {:keys [right left] :or {left :head, right (flip left)}} empty
      right2 right

      ]
  (println "Results:" right1 right2)
  (= right1 right2))
;; => true (with Clojure 1.13+) !!!
;; => false (with Clojure 1.12) !!!

;;; Explanation:
;; The :keys [left right] portion of the destructuring is equivalent to:
(let [empty {}
      left (get empty :left)
      right (get empty :right)]
  [left right]
  ,,, )
;; => [nil nil]

;; ... including :or, it then looks like this
(let [empty {}, left nil, right nil
      left (get empty :left :head)
      right (get empty :right (flip left))]
  [left right]
  ,,, )
;; => [:head :tail]
;; ... or
(let [empty {}, left nil, right nil
      right (get empty :right (flip left))
      left (get empty :left :head)
      ]
  [left right]
  ,,, )
;; => [:head nil]

