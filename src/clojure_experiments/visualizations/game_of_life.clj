(ns clojure-experiments.visualizations.game-of-life
  "See also
  - https://github.com/jackdbd/game-of-life
  - http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/
    -> https://web.archive.org/web/20250707220119/http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/
  - https://ericnormand.substack.com/p/what-problem-are-you-solving
  - Clojure Programming book - Ch3: https://learning.oreilly.com/library/view/clojure-programming/9781449310387/ch03.html#collections-work
    - section 'Revisiting a classic: Conway’s Game of Life'
    - It first shows a quite complex implementation...
    - ... then search for 'Getting to the next level' inside -> that's the simplified implementation.
  "
  (:require [quil.core :as q]
            [quil.middleware :as qm]
            [quil.middlewares.bind-output :as qout]))

;;;; Visualize game of life with Quil
;;;; 


(def window-width 900)
(def window-height 900)

;;; Examples of initial worlds
(def oscillator #{[1 2] [1 1] [1 0]})
(def glider #{[1 3] [2 1] [2 3] [3 2] [3 3]})

(defn neighbors [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not= 0 dx dy)]
    [(+ x dx) (+ y dy)]))
(neighbors [1 1])

(defn should-live? [is-alive? neighbors-count]
  (or (= 3 neighbors-count)
      (and is-alive? (= 2 neighbors-count))))
(should-live? true 2)

(defn step [current-gen]
  (for [[cell neighbors-count] (frequencies (mapcat neighbors current-gen))
        :when (should-live? ((set current-gen) cell) neighbors-count)]
    cell))
(step (step oscillator))

;;; Rationale for the implementation:
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; See 'Clojure Programming' book, ch3, 'Getting to the next level'
;; Getting to the next level. The problem with what we’ve done so far is that it stays close to the spirit of the original implementation.
;; However, there’s a way to find a far more elegant approach. For this to happen, we have to take a deep breath, step back, and really examine the rules for the Game of Life.
;;
;; At each step in time, the following transitions occur:
;;  - Any live cell with fewer than two live neighbours dies, as if caused by under-population.
;; - Any live cell with two or three live neighbours lives on to the next generation.
;; - Any live cell with more than three live neighbours dies, as if by overcrowding.
;; - Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
;;
;; This expression of the rules does not mention rows, columns, or indices.
;; It only talks about cells and neighbours; to be more precise, it talks about living cells, neighbours,
;; and dead cells that are in the vicinity of living cells.
;; Hence, the two main concepts are living cells and neighborhood:
;; dead cells are neighbour cells that are not alive, so they can be derived from neighbourhood and living cells.
;;
;; If we stick to these two concepts, the only state of the world is the set of living cells.
;; To generate each successive state, we simply have to first compute all living cells’ neighbours
;; and then count how many times a given “neighbour cell” occurs (it occurs as many times as it has living neighbours).



;; super-concise step inlining should-live?
(defn step [current-gen]
  (for [[cell n] (frequencies (mapcat neighbors current-gen))
        :when (or (= n 3) (and (= n 2) ((set current-gen) cell)))]
    cell))


(defn life [initial-world]
  (iterate step initial-world))

(def first-gen oscillator)
(def first-gen glider)
;; next gen:
#_(first (life oscillator))


;;; setup & draw board
(def cell-width 50)

(defn setup-game []
  (q/frame-rate 2)
  (q/background 255)
  first-gen)

(defn end-of-board?
  [board-width board-height state]
  (some (fn [[x y]] (or (< board-width (* x cell-width))
                        (< board-height (* y cell-width))))
        state))

(defn update-game [current-gen]
  (let [next-gen (step current-gen)]
    (if (end-of-board? window-width window-height next-gen)
      first-gen
      next-gen))
  )

(defn draw-board []
  ;; vertical lines
  (doseq [next-line-x (range cell-width window-width cell-width)]
    (q/line next-line-x 0 next-line-x window-height))
  ;; horizontal lines
  (doseq [next-line-y (range cell-width window-width cell-width)]
    (q/line 0 next-line-y window-width next-line-y)))

(defn- draw-cells [current-gen]
  (q/fill 255 0 0)
  (doseq [[x y] current-gen]
    (q/rect (* x cell-width)
            (* y cell-width)
            cell-width
            cell-width))
  )

(defn draw-game [current-gen]
  (q/clear)
  (q/background 255)
  ;; TODO: is it necessary to redraw board every time?
  ;; perhaps just draw it the `setup-game` function
  (draw-board)
  (draw-cells current-gen)
  )

(comment
  (q/defsketch game-of-life
    :setup setup-game
    :update update-game
    :size [window-width window-height]
    :draw draw-game
    :features [:resizable]
    :middleware [qm/pause-on-error qout/bind-output qm/fun-mode])

  )

