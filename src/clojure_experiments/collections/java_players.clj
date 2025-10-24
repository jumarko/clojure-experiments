(ns clojure-experiments.collections.java-players
  "This is based on the following Clojurians slack question: [10/2025]
  https://clojurians.slack.com/archives/C053AK3F9/p1761221470330399

  "
  (:import
   (java.util.stream Collectors)))

;;; GOAL: create a map of position -> player with highest rank at that position
;;
;; In Clojure, I could do multiple passes to massage the values into the shape I want:
;; Question:
;;   But is there a way in Clojure to do something more like the Java version,
;;   where I just compose the end result I want and then feed the players through?

(defonce players
  (for [[i r] (map-indexed vector (shuffle (range 20)))]
    {:position (rand-nth [:a :b :c])
     :name (str "player-" i)
     :rank r}))
players
;; => ({:position :c, :name "player-0", :rank 18}
;;     {:position :c, :name "player-1", :rank 0}
;;     {:position :b, :name "player-2", :rank 14}
;;     {:position :b, :name "player-3", :rank 12}
;; ....


;;; Java solution
;; This doesn't work!
#_(.collect
 (.stream players)
 (Collectors/groupingBy :position
                        (Collectors/collectingAndThen
                         (Collectors/maxBy #(- (:rank %2) (:rank %1)))
                         :name)))


;;; Clojure solution
(-> (group-by :position players)
    (update-vals (fn [position-players]
                   (:name (apply max-key :rank position-players)))))
;; => {:c "player-0", :b "player-4", :a "player-2"}
