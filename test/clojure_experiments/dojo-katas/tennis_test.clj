(ns clojure-experiments.dojo-katas.tennis-test
  (:require [clojure-experiments.dojo-katas.tennis :as tennis]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(defn new-game []
  (tennis/make-game "John Doe" "John Smith"))

;;; TODO: fixture later
;; check https://stackoverflow.com/questions/31735423/how-to-pass-a-value-from-a-fixture-to-a-test-with-clojure-test
;; (defn prepare-game-fixture []
;;   )

;; (use-fixtures :each prepare-game-fixture)


(deftest simple-game
  (testing "Zero score"
    (is (= "0-0"
           (tennis/score (new-game)))))
  (testing "Player 1 scores"
    (let [game (-> (new-game) (tennis/wins-ball "John Doe"))]
      (is (= "15-0"
             (tennis/score game)))))
  (testing "Player 1 scores three times in a row"
    (let [game (-> (new-game) (tennis/wins-ball "John Doe") (tennis/wins-ball "John Doe") (tennis/wins-ball "John Doe"))]
      (is (= "40-0"
             (tennis/score game))))))

;;; TODO Tests categories

;; player wins

;; deuce

;; advantage
