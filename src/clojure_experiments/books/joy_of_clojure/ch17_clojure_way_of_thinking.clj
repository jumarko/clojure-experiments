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


;;; 17.3 Invisible Design Patterns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Observer pattern
;; `add-watch` and `remove-watch` forms basis for the observer pattern
(defmacro defformula [nm bindings & formula]
  `(let ~bindings
     (let [formula# (agent ~@formula)
           update-fn# (fn [key# ref# old# new#]
                        (send formula# (fn [_#] ~@formula)))]
       (doseq [r# ~(mapv bindings
                         (range 0 (count bindings) 2))]
         (add-watch r# :update-formula update-fn#))
       (def ~nm formula#))))

(def h (ref 25))
(def ab (ref 100))
(defformula avg
  [at-bats ab, hits h]
  (float (/ @hits @at-bats)))
;; => macroexpands to:
;; (let [at-bats ab hits h]
;;   (let [formula__67577__auto__ (agent
;;                                  (float
;;                                    (/ (deref hits) (deref at-bats))))
;;         update-fn__67578__auto__ (fn 
;;                                    [key__67579__auto__
;;                                     ref__67580__auto__
;;                                     old__67581__auto__
;;                                     new__67582__auto__]
;;                                    (send
;;                                      formula__67577__auto__
;;                                      (fn 
;;                                        [___67583__auto__]
;;                                        (float
;;                                          (/
;;                                            (deref hits)
;;                                            (deref at-bats))))))]
;;     (doseq [r__67584__auto__ [at-bats hits]]
;;       (add-watch
;;         r__67584__auto__
;;         :update-formula
;;         update-fn__67578__auto__))
;;     (def avg formula__67577__auto__)))

@avg
;; => 0.25

(dosync (ref-set h 33))
@avg
;; => 0.33



;;; Abstract factory pattern
(def config
  '{:systems {:pump {:type :feeder :descr "Feeder system"}
              :sim1 {:type :sim :fidelity :low}
              :sim2 {:type :sim :fidelity :high :threads 2}}})

;; we can define "constructor" functions
(defn describe-system [name cfg]
  [(:type cfg) (:fidelity cfg)])
(describe-system :pump {:type :feeder :descr "Feeder system"})
;; => [:feeder nil]

(defmulti construct describe-system)
(defmethod construct :default [name cfg]
  {:name name :type (:type cfg)})

(defn construct-subsystems  [sys-map]
  (for [[name cfg] sys-map]
    (construct name cfg)))

(construct-subsystems (:systems config))
;; => ({:name :pump, :type :feeder} {:name :sim1, :type :sim} {:name :sim2, :type :sim})

;; now we can create more specific types for certain subsystems
(defmethod construct [:feeder nil] [_ cfg]
  (:descr cfg))
(construct-subsystems (:systems config))
;; => ("Feeder system" {:name :sim1, :type :sim} {:name :sim2, :type :sim})

(defrecord LowFiSim [name])
(defrecord HiFiSim [name threads])

(defmethod construct [:sim :low]
  [name cfg]
  (->LowFiSim name))
(defmethod construct [:sim :high]
  [name cfg]
  (->HiFiSim name (:threads cfg)))

(construct-subsystems (:systems config))
;; => ("Feeder system"
;;     #clojure_experiments.books.joy_of_clojure.ch17_clojure_way_of_thinking.LowFiSim{:name :sim1}
;;     #clojure_experiments.books.joy_of_clojure.ch17_clojure_way_of_thinking.HiFiSim{:name :sim2, :threads 2})


