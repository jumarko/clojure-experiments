(ns clojure-repl-experiments.visualizations.quil-game-of-life
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

(defn life [initial-world]
  (iterate step initial-world))

(def first-gen oscillator)
(def first-gen glider)
;; next gen:
#_(first (life oscillator))


;; setup & draw board
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

