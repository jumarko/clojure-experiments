(ns clojure-repl-experiments.rock-paper-scissors
  "Rock-Paper-Scissors game from braveclojure: https://codereview.stackexchange.com/questions/189067/rock-paper-scissors-game-in-clojure")


(ns rps.core
  (:gen-class))

(defn get-input
  "Waits for user to enter text and hit enter, then cleans the input"
  ([] (get-input ""))
  ([default]
   (let [input (clojure.string/trim (read-line))]
     (if (empty? input) default input))))

(defn get-random-choice
  "Let the computer pick a random choice"
  [choices]
  (-> choices keys rand-nth))

(defn update-player-choice
  "Add r / p / s to the list of choices"
  [players player choice]
  (update-in players [player :choices] conj choice))

(defn get-round-winner
  "This function returns a keyword of the
   winning player or nil if it is a draw"
  [user-choice computer-choice]
  (cond
    (= user-choice computer-choice) nil
    (or (and (= user-choice :r) (= computer-choice :s))
        (and (= user-choice :p) (= computer-choice :r))
        (and (= user-choice :s) (= computer-choice :p))) :user
    :else :computer))

(defn increment-player-score
  "Increment the winner's score"
  [players winner]
  (update-in players [winner :score] inc))

(defn update-player-scores
  "If there is a winner, update the winner's score
  otherwise return the original state of players"
  [players winner]
  (if (not (nil? winner))
    (increment-player-score players winner)
    players))

(defn get-round-winner-name
  "Display the name of the round winner"
  [players winner]
  (get-in players [winner :name]))

(defn game-is-on
  "Determine if the game is still on by
  checking that both scores are < 3"
  [players]
  (every? #(-> % :score (< 3)) (vals players)))

(defn generate-players
  "Return a simple object of players in the game"
  ([user-name] (generate-players user-name "Computer"))
  ([user-name computer-name]
   {:user {:score 0
           :choices []
           :name user-name}
    :computer {:score 0
               :choices []
               :name computer-name}}))

(defn display-scores
  "Display the scores and end the game"
  [players]
  (let [user-score (get-in players [:user :score])
        comp-score (get-in players [:computer :score])
        user-won? (> user-score comp-score)
        user-name (get-in players [:user :name])
        comp-name (get-in players [:computer :name])]
    (println (format "%s won the game with the score of %s to %s"
                     (if user-won? user-name comp-name)
                     (if user-won? user-score comp-score)
                     (if user-won? comp-score user-score)))))

(defn display-round-intro
  "A helper function that displays i.e. Rock vs Scissors"
  [choices user-choice computer-choice]
  (println (format "%s vs %s" (get choices user-choice) (get choices computer-choice))))

(defn display-question
  "Display the key question - Rock, Paper, Scissors?"
  [choices]
  (let [question (->> choices
                      (map #(format "%s(%s)" (second %) (-> (first %) name)))
                      (interpose ", ")
                      (apply str))]
    (println (str question "?"))))

(defn play-round
  "The core game logic"
  [players choices]
  (display-question choices)
  (let [user-choice (-> (get-input) keyword)
        computer-choice (get-random-choice choices)
        round-winner (get-round-winner user-choice computer-choice)
        updated-players (-> players
                            (update-player-choice :user user-choice)
                            (update-player-choice :computer computer-choice)
                            (update-player-scores round-winner))]

    (display-round-intro choices user-choice computer-choice)
    (if (nil? round-winner)
      (println "Draw")
      (println (format "%s has won the round" (get-round-winner-name players round-winner))))

    (if (game-is-on updated-players)
      (play-round updated-players choices)
      (display-scores updated-players))))

(defn ask-for-name
  "Get the name from the user"
  []
  (println "What is your name?")
  (let [user-name (get-input)
        players   (generate-players user-name)
        choices   {:r "Rock"
                   :p "Paper"
                   :s "Scissors"}]
    (play-round players choices)))

(defn -main
  "Start the game"
  [& args]
  (println "Let the games begin")
  (ask-for-name))
