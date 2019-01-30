(ns clojure-experiments.dojo-katas.tennis
  "This is a Tennis kata motivated by the Coding Dojo at Kentico office 29.1.2019.
  See http://codingdojo.org/kata/Tennis/")

(defn score
  "Returns human-readable score of the game.
  E.g. '0-15', 'deuce', 'Player1 has an advantage', 'Player1 won the game'."
  [game]
  (let [winner (:winner game)
        [_ player1-score] (first game)
        [_ player2-score] (second game)]
    (cond
      winner
      (format "%s won the game!" winner)

      (= 40 player1-score player2-score)
      (format "DEUCE")

      :else
      (format "%s-%s" player1-score player2-score))))

;; TODO: how about the idea to track points and only translate them to the score in the `score` function?
(defn- update-score [current-score]
  (if (< current-score 30)
    (+ 15 current-score)
    (+ 10 current-score)))

(defn winner? [game player]
  (< 40 (get game player)))

(defn wins-ball [game player]
  (if (:winner game)
    game
    (let [updated-game (update game player update-score)]
      (if (winner? updated-game player)
        (assoc updated-game :winner player)
        updated-game))))

(defn make-game [player1-name player2-name]
  ;; here we rely on ordering of small hashmaps which is generally bad but in this case it's completely safe
  ;; (we'll only ever have two players, no more)
  {player1-name 0
   player2-name 0})
