(ns four-clojure.073-tic-tac-toe
  "Analyze a Tic-Tac-Toe Board: http://www.4clojure.com/problem/73
  Hard problem.")

(def ex0
  [[:e :e :e]
   [:e :e :e]
   [:e :e :e]])
(def ex1
  [[:x :e :o]
   [:x :e :e]
   [:x :e :o]])
(def ex2
  [[:e :x :e]
   [:o :o :o]
   [:x :e :x]])

;; https://stackoverflow.com/questions/10347315/matrix-transposition-in-clojure
(defn- transpose [m]
  (apply mapv vector m))
#_(transpose ex1)

(defn- diagonals [m]
  (let [max-index (dec (count m))
        first-diag (map-indexed (fn [idx row] (row idx)) m)
        second-diag (map-indexed (fn [idx row] (row (- max-index idx))) m)]
    [first-diag second-diag]))
#_(diagonals ex1)

(defn- win [lst]
  (when-let [s (set lst)]
    (when (and (= 1 (count s))
               (#{:x :o} (first s)))
      (first s))))
(comment 
  (win [:x :x :x])
  (win [:x :o :x])
  (win [:o :o :o])
  (win [:e :e :e]))

(defn winner [board]
  (let [rows board
        cols (transpose board)
        diags (diagonals board)
        all (concat rows cols diags)]
    (->> all
         (map win)
         (remove nil?)
         first)
    ))


;;; everything together
(fn winner [board]
  (let [rows board
        cols (apply mapv vector board)
        max-index (dec (count board))
        diags [(map-indexed (fn [idx row] (row idx)) board)
               (map-indexed (fn [idx row] (row (- max-index idx))) board)]
        all (concat rows cols diags)
        win-fn (fn win [lst]
                 (when-let [s (set lst)]
                   (when (and (= 1 (count s))
                              (#{:x :o} (first s)))
                     (first s))))]
    (->> all
         (map win-fn)
         (remove nil?)
         first)))

(winner ex0)
(winner ex1)
(winner ex2)
