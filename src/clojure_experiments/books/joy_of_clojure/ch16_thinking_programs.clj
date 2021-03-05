(ns clojure-experiments.books.joy-of-clojure.ch16-thinking-programs
  (:require [clojure-experiments.books.joy-of-clojure.ch05-collections :as ch05]
            [clojure.set :as set]
            [clojure.string :as str]))

;;; Sudoku solver (p. 395+)

(def b1 '[3 - - - - 5 - 1 -
          - 7 - - - 6 - 3 -
          1 - - - 9 - - - -
          7 - 8 - - - - 9 -
          9 - - 4 - 8 - - 2
          - 6 - - - - 5 - 1
          - - - - 4 - - - 6
          - 4 - 7 - - - 2 -
          - 2 - 6 - - - - 3])

(defn prep [board]
  (map #(partition 3 %)
       (partition 9 board)))

(defn print-board [board]
  (let [row-sep (apply str (repeat 37 "-"))]
    (println row-sep)
    (dotimes [row (count board)]
      (print "| ")
      (doseq [subrow (nth board row)]
        (doseq [cell (butlast subrow)]
          (print (str cell "   ")))
        (print (str (last subrow) " | ")))
      (println)
      (when (zero? (mod (inc row) 3))
        (println row-sep)))))

;; alternative implementation
(defn print-board [board]
  (let [row-sep (apply str (repeat 37 "-"))]
    (println row-sep)
    (dotimes [row (count board)]
      (let [subrow-strings (map #(str/join "   " %) (nth board row))
            row-string (str "| " (str/join " | " subrow-strings)" | ")]
        (print row-string))
      (println)
      (when (zero? (mod (inc row) 3))
        (println row-sep)))))

#_(->> b1 prep print-board)

(defn rows [board sz]
  (partition sz board))

(rows b1 9)
;; => ((3 - - - - 5 - 1 -)
;;     (- 7 - - - 6 - 3 -)
;;     (1 - - - 9 - - - -)
;;     (7 - 8 - - - - 9 -)
;;     (9 - - 4 - 8 - - 2)
;;     (- 6 - - - - 5 - 1)
;;     (- - - - 4 - - - 6)
;;     (- 4 - 7 - - - 2 -)
;;     (- 2 - 6 - - - - 3))

;; Note that I changed the implementation from the book
;; to use `(mod index 9)` instead of `(/ index 9)`
(defn row-for [board index sz]
  (nth (rows board sz)
       #_(/ index 9)
       (mod index 9)))

(row-for b1 0 9)
;; => (3 - - - - 5 - 1 -)
(row-for b1 1 9)
;; => (- 7 - - - 6 - 3 -)
(row-for b1 3 9)
;; => (7 - 8 - - - - 9 -)

(defn column-for [board index sz]
  (let [col-index (mod index sz)]
    (map #(nth % col-index)
         (rows board sz))))
(column-for b1 0 9)
;; => (3 - 1 7 9 - - - -)
(column-for b1 1 9)
;; => (- 7 - - - 6 - 4 2)
(column-for b1 3 9)
;; => (- - - - 4 - - 7 6)


(defn subgrid-for [board i]
  (let [rows (rows board 9)
        sgcol (/ (mod i 9) 3)
        sgrow (/ (/ i 9) 3)
        grp-col (column-for (mapcat #(partition 3 %) rows) sgcol 3)
        grp (take 3 (drop (* 3 (int sgrow)) grp-col))]
    (flatten grp)))
(subgrid-for b1 0)
;; => (3 - - - 7 - 1 - -)
(subgrid-for b1 1)
;; => (3 - - - 7 - 1 - -)
(subgrid-for b1 2)
;; => (3 - - - 7 - 1 - -)
(subgrid-for b1 3)
;; => (- - 5 - - 6 - 9 -)
(subgrid-for b1 4)
;; => (- - 5 - - 6 - 9 -)
(subgrid-for b1 5)
;; => (- - 5 - - 6 - 9 -)
(subgrid-for b1 6)
;; => (- 1 - - 3 - - - -)
(subgrid-for b1 7)
;; => (- 1 - - 3 - - - -)
;; ...
(subgrid-for b1 80)
;; => (- - 6 - 2 - - - 3)


(defn numbers-present-for
  "Gathers all the numbers present in the row, column, and subgrid for a given cell."
  [board i]
  (set
   (concat (row-for board i 9)
           (column-for board i 9)
           (subgrid-for board i))))

(numbers-present-for b1 1)
;; => #{7 1 4 - 6 3 2}

;; now try to place a new number
(numbers-present-for (assoc b1 1 8) 1)
;; => #{7 1 4 - 6 3 2 8}

(defn possible-placements [board index]
  (set/difference (set (range 1 10))
                  (numbers-present-for board index)))
(possible-placements b1 1)
;; => #{9 5 8}

;; this uses `pos` function from chapter five: https://github.com/joyofclojure/book-source/blob/master/src/clj/joy/logic/manual_constraints.clj#L59-L60

(defn solve [board]
  (if-let [[i & _] (and (some '#{-} board)
                        (ch05/pos '#{-} board))]
    (flatten (map #(solve (assoc board i %))
                  (possible-placements board i)))
    board))

(comment
  ;; unfortunately, this doesn't work
  (-> b1
      solve
      prep
      print-board)

  ,)


;;; Unification (p. 400+)

(defn lvar?
  "Determines if a value represents a logic variable."
  [x]
  (boolean (when (symbol? x)
             (re-matches #"^\?.*" (name x)))))
(lvar? '?x)
;; => true
(lvar? 'a)
;; => false
(lvar? 2)
;; => false

(defn satisfy1 [l r knowledge]
  (let [L (get l knowledge l)
        R (get r knowledge r)]
    (cond
      (= L R) knowledge
      (lvar? l) (assoc knowledge L R)
      (lvar? r) (assoc knowledge R L)
      :default nil)))
(satisfy1 '?something 2 {})
;; => {?something 2}

;; this is more subtle
(satisfy1 '?x '?y {})
;; => {?x ?y}

(->> {}
     (satisfy1 '?x '?y) ;; => {?x ?y}
     (satisfy1 '?y 1)
     )
;; => {?x ?y, ?y 1}


;;; satisfying seqs
(satisfy1 '(1 2 3) '(1 ?something 3) {})
;; => nil

;; we need an improved version of satisfy1
(defn satisfy [l r knowledge]
  (let [L (get l knowledge l)
        R (get r knowledge r)]
    (cond
      (not knowledge) nil
      (= L R) knowledge
      (lvar? l) (assoc knowledge L R)
      (lvar? r) (assoc knowledge R L)
      (every? seq? [L R]) (satisfy (rest L)
                                   (rest R)
                                   ;; build upon the knowledge from unification of the first elements of the seqs
                                   (satisfy (first L) (first R) knowledge))

      :default nil)))

(satisfy '(1 2 3) '(1 ?something 3) {})
;; => {?something 2}


