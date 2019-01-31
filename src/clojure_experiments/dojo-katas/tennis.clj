(ns clojure-experiments.dojo-katas.tennis
  "This is a Tennis kata motivated by the Coding Dojo at Kentico office 29.1.2019.
  See http://codingdojo.org/kata/Tennis/"
  (:require [clojure.spec.alpha :as s]))

(defn- winner [player1-points player2-points]
  (assert (and player1-points player2-points)
          "points must be defined")
  (cond
    (and (<= 4 player1-points)
         (<= 2 (- player1-points player2-points)))
    0

    (and (<= 4 player2-points)
         (<= 2 (- player2-points player1-points)))
    1))

(defn- add-ball-winner
  "This is a reducing function which gets points aggregated so far for both players,
  and then adds the new `ball-winner` to the mix
  effectively incrementing either first player's points or second player's points."
  [[player1-points player2-points :as points-so-far] ball-winner]
  (assert (contains? points-so-far ball-winner)
          (str "Unknown player: " ball-winner " NOT IN: " (keys points-so-far)))
  (let [updated-points (update points-so-far ball-winner inc)]
    (if (winner player1-points player2-points)
      (reduced updated-points) ; there's no point to continue further
      updated-points)))


;;; Public interface
;;;;;;;;;;;;;;;;;;;;;;;


(defn win-ball [game player]
  (update game
          :scores
          (fn [scores]
            (conj
             scores
             (cond
               (= (:player1 game) player)
               0

               (= (:player2 game) player)
               1

               :else
               (throw (ex-info "Invalid player!" {:player player
                                                  :valid-game-players [(:player1 game)
                                                                       (:player2 game)]})))))))

(defn- points-to-score [points]
  (case points
    0 0
    1 15
    2 30
    3 40))

(defn- deuce? [player1-points player2-points]
  (and (= player1-points player2-points)
       (< 2 player2-points)))

(defn- advantager [player1-points player2-points]
  (when (and (< 2 player1-points) (< 2 player2-points))
    (cond
      (= 1 (- player1-points player2-points))
      0

      (= 1 (- player2-points player1-points))
      1
      )))

(defn winner-name [game winner-index]
  (case winner-index
    0 (:player1 game)
    1 (:player2 game)))

(s/def ::game (s/keys :req-un [::player1 ::player2 ::scores]))
(s/fdef score
  :args (s/cat :game ::game))
(defn score
  "Returns human-readable score of the game.
  E.g. '0-15', 'deuce', 'Player1 has an advantage', 'Player1 won the game'."
  [game]
  (let [scores (:scores game)
        player1 (:player1 game)
        player2 (:player2 game)
        [player1-points player2-points] (reduce
                                         add-ball-winner
                                         [0 0]
                                         scores)]
    (if-let [winner-index (winner player1-points player2-points)]
      (format "%s WON!" (winner-name game winner-index))
      (cond 
        (deuce? player1-points player2-points)
        "DEUCE"

        (advantager player1-points player2-points)
        (let [adv-index (advantager player1-points player2-points)]
          (format "%s ADVANTAGE" (winner-name game adv-index)))

        :else
        (format "%s-%s" (points-to-score player1-points) (points-to-score player2-points)))
      ;; TODO add "DEUCE"
      ;; TODO add "ADVANTAGE"
)))

(s/fdef make-game
  :args (s/cat :player1 string? :player2 string?)
  :ret ::game)
(defn make-game [player1-name player2-name]
  {:player1 player1-name
   :player2 player2-name
   :scores []})
