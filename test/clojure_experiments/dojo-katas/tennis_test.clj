(ns clojure-experiments.dojo-katas.tennis-test
  (:require [clojure-experiments.dojo-katas.tennis :as tennis]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(defn new-game []
  (tennis/make-game "P1" "P2"))

;;; TODO: fixture later
;; check https://stackoverflow.com/questions/31735423/how-to-pass-a-value-from-a-fixture-to-a-test-with-clojure-test
;; (defn prepare-game-fixture []
;;   )

;; (use-fixtures :each prepare-game-fixture)

;; TODO: This feels it should be easier (maybe `iterate` or `reduce`?) but it doesn't matter much after all
(defn- score-n-times [game player-name n]
  (loop [new-game game
         cnt n]
    (if (zero? cnt)
      new-game
      (recur (tennis/wins-ball new-game player-name)
             (dec cnt)))))

(defn- check-score [[player1-name player1-points] [player2-name player2-points] expected-score]
  (testing (format "When player 1 scores %s times and player 2 scores %s times then we expect score: %s"
                   player1-points player2-points expected-score)
    (let [game (new-game)
          player1-game (score-n-times game player1-name player1-points)
          player2-game (score-n-times player1-game player2-name player2-points)
          end-state player2-game]
      (is (= expected-score
             (tennis/score end-state))))))

(deftest simple-game-first-player
  (check-score ["P1" 0] ["P2" 0] "0-0")
  (check-score ["P1" 1] ["P2" 0] "15-0")
  (check-score ["P1" 2] ["P2" 0] "30-0")
  (check-score ["P1" 3] ["P2" 0] "40-0"))

(deftest simple-game-second-player
  (check-score ["P1" 0] ["P2" 1] "0-15")
  (check-score ["P1" 0] ["P2" 3] "0-40"))

(deftest simple-game-both-players
  (check-score ["P1" 1] ["P2" 1] "15-15")
  (check-score ["P1" 1] ["P2" 2] "15-30")
  (check-score ["P1" 2] ["P2" 3] "30-40"))

;;; TODO Tests categories

;; player wins


;; deuce

;; advantage
