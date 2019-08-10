(ns clojure-experiments.dojo-katas.tennis-test
  (:require [clojure-experiments.dojo-katas.tennis :as tennis]
            [clojure.test :refer [deftest is testing]]))

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
      (recur (tennis/win-ball new-game player-name)
             (dec cnt)))))

(defn- check-score [[player1-name player1-points] [player2-name player2-points] expected-score]
  (testing (format "When player 1 scores %s times and player 2 scores %s times then we expect score: %s"
                   player1-points player2-points expected-score)
    (let [game-result (-> (new-game)
                          (score-n-times player1-name player1-points)
                          (score-n-times player2-name player2-points))]
      (is (= expected-score
             (tennis/score game-result))))))

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
(deftest simple-game-first-player-wins
  (check-score ["P1" 4] ["P2" 0] "P1 WON!")
  (check-score ["P1" 4] ["P2" 3] "P1 WON!")
  ;; this should still be the winninig for the first player!
  (check-score ["P1" 4] ["P2" 4] "P1 WON!"))

(deftest simple-game-second-player-wins
  (check-score ["P1" 0] ["P2" 4] "P2 WON!")
  ;; this should still be the winninig for the first player!
  (check-score ["P2" 4] ["P1" 4] "P2 WON!"))

(deftest deuce
  (testing "Simple deuce"
    (check-score ["P1" 3] ["P2" 3] "DEUCE"))
  (testing "Deuce when players have more than 40 points"
    (let [game-result (-> (new-game)
                          (score-n-times "P1" 3)
                          (score-n-times "P2" 3)
                          (score-n-times "P1" 1)
                          (score-n-times "P2" 1))]
      (is (= "DEUCE" (tennis/score game-result))))))

;; advantage
(deftest advantage
  (testing "player1 has advantage after first deuce"
    (let [game-result (-> (new-game)
                          (score-n-times "P1" 3)
                          (score-n-times "P2" 3)
                          (score-n-times "P1" 1))]
      (is (= "P1 ADVANTAGE" (tennis/score game-result)))))
  (testing "player2 has advantage after the first deuce"
    (check-score ["P1" 3] ["P2" 4] "P2 ADVANTAGE"))
  (testing "player2 has advantage after three deuces"
    (let [game-result (-> (new-game)
                          (score-n-times "P1" 3)
                          (score-n-times "P2" 4)
                          (score-n-times "P1" 1)
                          (score-n-times "P2" 1)
                          (score-n-times "P1" 1)
                          (score-n-times "P2" 1))]
      (is (= "P2 ADVANTAGE" (tennis/score game-result))))))
