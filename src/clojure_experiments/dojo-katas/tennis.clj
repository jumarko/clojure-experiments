(ns clojure-experiments.dojo-katas.tennis
  "This is a Tennis kata motivated by the Coding Dojo at Kentico office 29.1.2019.
  See http://codingdojo.org/kata/Tennis/")

(defn score
  "Returns human-readable score of the game.
  E.g. '0-15', 'deuce', 'Player1 has an advantage', 'Player1 won the game'."
  [game-board]
  (let [[_ player1-score] (first game-board)
        [_ player2-score] (second game-board)]
    (format "%s-%s" player1-score player2-score)))

;; TODO: how about the idea to track points and only translate them to the score in the `score` function?
(defn- update-score [current-score]
  (if (< current-score 30)
    (+ 15 current-score)
    (+ 10 current-score)))

(defn wins-ball [game-board player]
  (update game-board player update-score))

(defn make-game [player1-name player2-name]
  ;; here we rely on ordering of small hashmaps which is generally bad but in this case it's completely safe
  ;; (we'll only ever have two players, no more)
  {player1-name 0
   player2-name 0})
