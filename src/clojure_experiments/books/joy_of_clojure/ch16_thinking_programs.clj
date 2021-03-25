(ns clojure-experiments.books.joy-of-clojure.ch16-thinking-programs
  "See also https://github.com/swannodette/logic-tutorial"
  (:require [clojure-experiments.books.joy-of-clojure.ch05-collections :as ch05]
            [clojure.core.logic :as logic]
            [clojure.core.logic.pldb :as pldb]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.walk :as walk]))


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

(satisfy '(?x 2 3 (4 5 ?z))
         '(1 2 ?y (4 5 6))
         {})
;; => {?x 1, ?y 3, ?z 6}

;; satisfies when possible
(satisfy '(?x 10000 3) '(1 2 ?y) {})
;; => nil


;;; let's define `subst` function that performs the substitution - bindings returned by `satisfy`
(defn subst [term binds]
  (walk/prewalk
   (fn [expr]
     (if (lvar? expr)
       (or (binds expr) expr)
       expr))
   term))

(subst '(1 ?x 3) '{?x 2})
;; => (1 2 3)

(subst '((((?x)))) '{?x 2})
;; => ((((2))))

(subst '{:a ?x
         :b [1 ?x 3]}
       '{?x 2})
;; => {:a 2, :b [1 2 3]}

;; with incomplete "knowledge"
(subst '(1 ?x 3) {})
;; => (1 ?x 3)
(subst '(1 ?x 3) '{?x ?y})
;; => (1 ?y 3)

;;; Now define the last piece, 'meld' function, which combbines two seqs together
(defn meld [term1 term2]
  (->> {} ;; 'unification' combines two terms in an empty context
       (satisfy term1 term2)
       (subst term1)))

(meld '(1 ?x 3) '(1 2 ?y))
;; => (1 2 3)

(meld '(1 ?x) '(?y (?y 2)))
;; => (1 (1 2))


;;; But simple unification doesn't solve many problems:

;; e.g. here it  cannot see that ?y is one
(satisfy '(1 ?x) '(?y (?y 2)) {})
;; => {?y 1, ?x (?y 2)}


;;; core.logic (p. 407)
;;; See https://github.com/clojure/core.logic
;;; and also https://github.com/clojure/core.unify
;;; and https://github.com/swannodette/logic-tutorial
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; let's mimic our `satisfy` funtion with `logic/==`
(logic/run* [answer]
  (logic/== answer 5))
;; => (5) ; the return values maps one for one with estabilished binding (only `answer` here)

(logic/run* [val1 val2]
  (logic/== {:a val1 :b 2}
            {:a 1 :b val2}))
;; => ([1 2]) ; val1 is 1, val2 is 2

;; let's see how it works if the value is uknown
(logic/run* [?x ?y]
  (logic/== ?x ?y))
;; => ([_0 _0]) ; _0 represents a logic variable

;; what if unification fails?
(logic/run* [?q]
  (logic/== ?q 1)
  (logic/== ?q 2))
;; => ()

;; `logic/conde` can be used to unify terms in multiple universes
(logic/run*
 [george m]
 (logic/conde
  [(logic/== george :born)]
  [(logic/== george :unborn)]))
;; => (:born :unborn)

;;; Relations (p. 409)
;; note `defrel` replaced by `pldb/db-rel` in newer core.logic releases: https://github.com/swannodette/logic-tutorial/pull/15/files
(pldb/db-rel orbits orbital body)

;; this new syntax using `pldb/db` uses representation as tuples [orbits :mercury: sun], etc.
;; you could also use `pldb/db-fact` one-by-one: https://github.com/swannodette/logic-tutorial#question--answer
;; - `pldb/db-fact` accepts existing database of facts as the first argument
(def orbitals
  (pldb/db
   [orbits :mercury :sun]
   [orbits :venuse :sun]
   [orbits :earth :sun]
   [orbits :mars :sun]
   [orbits :jupiter :sun]
   [orbits :saturn :sun]
   [orbits :uranus :sun]
   [orbits :neptune :sun]))

;; find all the planets that orbit anything
;; notice usage of `logic/fresh` and `pldb/with-db`
;; Note: there's also `pldb/with-dbs` - see https://github.com/swannodette/logic-tutorial for more
(pldb/with-db orbitals
  (logic/run* [q] ; logical variable used for query output
    (logic/fresh [orbital body] ; logical variables used only in the local scope
      (orbits orbital body)
      (logic/== q orbital))))
;; => (:saturn :earth :uranus :neptune :venuse :mars :jupiter :mercury)

;;; Subgoals
(pldb/db-rel stars star)
(def stars-db (pldb/db [stars :sun]))

;; using `body` here is confusing because we used it to mean "star" in the `orbits` relation
;; and now we are using it as "planet"
(defn planeto [body]
  (logic/fresh [star]
               (stars star)
               (orbits body star)))

;; notice that we have to use `pldb/with-dbs`
(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (planeto :earth)))
;; => (_0) ; _0 means there's a match ...
;; ... let's output true if it's a planet
(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (planeto :earth)
    (logic/== q true)))
;; => (true)
(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (planeto :sun)
    (logic/== q true)))
;; => ()

;; let's distinguish between planets and satellites
(defn satelliteo [body]
  (logic/fresh [p]
    (orbits body p)
    (planeto p)))

(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (satelliteo :sun)))
;; => ()
(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (satelliteo :earth)))
;; => ()
(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (satelliteo :earth)))

(def orbitals (pldb/db-fact orbitals orbits :moon :earth))
(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (satelliteo :moon)))
;; => (_0)

;; let's add more data points
(def orbitals (pldb/db-facts orbitals
                             [orbits :phobos :mars]
                             [orbits :deimos :mars]
                             [orbits :io :jupiter]
                             [orbits :europa :jupiter]
                             [orbits :callisto :jupiter]
                             [orbits :ganymede :jupiter]))
(pldb/with-dbs [orbitals stars-db]
  (logic/run* [q]
    (satelliteo :io)))
;; => (_0)
