(ns clojure-experiments.books.joy-of-clojure.ch17-clojure-way-of-thinking
  "Chapter 17: Clojure changes the way you think"
  (:require [clojure.set :as ra]
            [clojure.string :as str]))


;;; 17.1 Thinking in the domain
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; relational algebra in Clojure
(def artists
  #{{:artist "Burial" :genre-id 1}
    {:artist "Magma" :genre-id 2}
    {:artist "Can" :genre-id 3}
    {:artist "Faust" :genre-id 3}
    {:artist "Ikonika" :genre-id 1}
    {:artist "Grouper"}})

(def genres #{{:genre-id 1 :genre-name "Dubstep"}
              {:genre-id 2 :genre-name "Zeuhl"}
              {:genre-id 3 :genre-name "Prog"}
              {:genre-id 4 :genre-name "Drone"}})

(def ALL identity)

(ra/select ALL genres)
;; => #{{:genre-id 4, :genre-name "Drone"}
;;      {:genre-id 2, :genre-name "Zeuhl"}
;;      {:genre-id 3, :genre-name "Prog"}
;;      {:genre-id 1, :genre-name "Dubstep"}}

;; filter by id
(ra/select (fn [{:keys [genre-id]}] (#{1 3} genre-id))
           genres)
;; => #{{:genre-id 3, :genre-name "Prog"}
;;      {:genre-id 1, :genre-name "Dubstep"}}

;; ... to make it more "fluent"
(defn ids [& ids]
  (fn [{:keys [genre-id]}] ((set ids) genre-id)))
(ra/select (ids 1 3) genres)
;; => #{{:genre-id 3, :genre-name "Prog"} {:genre-id 1, :genre-name "Dubstep"}}

;; natural join
(take 2 (ra/select ALL (ra/join artists genres)))
;; => ({:genre-id 2, :genre-name "Zeuhl", :artist "Magma"} {:genre-id 1, :genre-name "Dubstep", :artist "Ikonika"})


;;; SQL-like DSL to generate queries (p. 426 - 431)

;; interesting detection of `unquote`
(read-string "~max")
;; => (clojure.core/unquote max)
(first '~max)
;; => clojure.core/unquote

(defn shuffle-expr [expr]
  (if (coll? expr)
    (if (= (first expr) `unquote) ; can't simply use `~ because that's an incomplete expression
      "?"
      (let [[op & args] expr]
        (str "("
             ;; this puts the operator into infix position
             (str/join (str " " op " ")
                       (map shuffle-expr args))
             ")")))
    expr))

(shuffle-expr `(unquote max))
;; => "?"
(shuffle-expr '~max)
;; => "?"

(shuffle-expr '(= X.a X.b))
;; => "(X.a = X.b)"

(shuffle-expr '(AND (< a 5)
                    (OR (> b 0)
                        (> b ~max))))
;; => "((a < 5) AND ((b > 0) OR (b > ?)))"

(defn process-where-clause [processor expr]
  (str " WHERE " (processor expr)))
(process-where-clause shuffle-expr '(AND (< a 5) (< b ~max)))
;; => " WHERE ((a < 5) AND (b < ?))"

(defn process-left-join-clause [processor table _ expr]
  (str " LEFT JOIN " table " ON " (processor expr)))
(apply process-left-join-clause shuffle-expr '(Y :ON (= X.a Y.b)))
;; => " LEFT JOIN Y ON (X.a = Y.b)"
(let [LEFT-JOIN (partial process-left-join-clause shuffle-expr)]
  (LEFT-JOIN 'Y :ON '(= X.a Y.b)))
;; => " LEFT JOIN Y ON (X.a = Y.b)"

(defn process-from-clause [processor table & joins]
  (apply str " FROM "
       table
       (map processor joins)))
(process-from-clause shuffle-expr 'X
                     (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))
;; => " FROM X LEFT JOIN Y ON (X.a = Y.b)"

(defn process-select-clause [processor fields & clauses]
  (apply str " SELECT " (str/join ", " fields)
         (map processor clauses)))
(process-select-clause shuffle-expr
                       '[a b c]
                       (process-from-clause shuffle-expr 'X
                                            (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))
                       (process-where-clause shuffle-expr '(AND (< a 5) (< b ~max))))
;; => " SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))"

;; now introduce some mapping for better names
(declare apply-syntax)
(def ^:dynamic *clause-map*
  ;; notice that SELECT and FROM use `apply-syntax` because they allow nested clauses
  {'SELECT (partial process-select-clause apply-syntax)
   'FROM (partial process-from-clause apply-syntax)
   'LEFT-JOIN (partial process-left-join-clause shuffle-expr)
   'WHERE (partial process-where-clause shuffle-expr)})

(defn apply-syntax [[op & args]]
  (apply (get *clause-map* op) args))

;; the DSL 'trigger's on SELECT so we can implement that as a macro
(defmacro SELECT
  {:style/indent 1} ; this is in 2-spaces factors - see https://docs.cider.mx/cider/indent_spec.html
  [& args]
  {:query (apply-syntax (cons 'SELECT args))
   :bindings (vec (for [n (tree-seq coll? seq args) ; notice `tree-seq` usage
                        :when (and (coll? n)
                                   (= (first n) `unquote))]
                    (second n)))})

;; now use it
(defn example-query [max]
  (SELECT [a b c]
    (FROM X
          (LEFT-JOIN Y :ON (= X.a Y.b)))
    (WHERE (AND (< a 5) (< b ~max)))))
(example-query 9)
;; => {:query " SELECT a, b, c FROM X LEFT JOIN Y ON (X.a = Y.b) WHERE ((a < 5) AND (b < ?))"
;;     :bindings [9]}

;; try tree-seq to understand it more:
(tree-seq
 coll?
 seq
 '(SELECT [a b c]
    (FROM X
          (LEFT-JOIN Y :ON (= X.a Y.b)))
    (WHERE (AND (< a 5) (< b ~max)))))
;; => ((SELECT [a b c] (FROM X (LEFT-JOIN Y :ON (= X.a Y.b))) (WHERE (AND (< a 5) (< b ~max))))
;;     SELECT
;;     [a b c]
;;     a
;;     b
;;     c
;;     (FROM X (LEFT-JOIN Y :ON (= X.a Y.b)))
;;     FROM
;;     X
;;     (LEFT-JOIN Y :ON (= X.a Y.b))
;;     LEFT-JOIN
;;     Y
;;     :ON
;;     (= X.a Y.b)
;;     =
;;     X.a
;;     Y.b
;;     (WHERE (AND (< a 5) (< b ~max)))
;;     WHERE
;;     (AND (< a 5) (< b ~max))
;;     AND
;;     (< a 5)
;;     <
;;     a
;;     5
;;     (< b ~max)
;;     <
;;     b
;;     ~max
;;     clojure.core/unquote
;;     max)
