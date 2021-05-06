(ns clojure-experiments.books.joy-of-clojure.ch17-clojure-way-of-thinking
  "Chapter 17: Clojure changes the way you think"
  (:require [clojure.set :as ra]
            [clojure.string :as str]
            [clojure.xml :as xml]
            [clojure-experiments.books.joy-of-clojure.ch08-macros :refer [contextual-eval]]))



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


;;; Dependency Injection (p. 442 - 446)
;;; We'll use the basis we established with the Abstract Factory pattern
(def lofi {:type :sim :descr "Lowfi sim" :fidelity :low})
(def hifi {:type :sim :descr "Hifi sim" :fidelity :high :threads 2})

(construct :lofi lofi)
;; => #clojure_experiments.books.joy_of_clojure.ch17_clojure_way_of_thinking.LowFiSim{:name :lofi}

;; We'll now extend the idea of construction.
;; Let's start by introducing a couple of protocols
;; to decribe system-level and simulation capabilities

(defprotocol Sys
  (start! [sys])
  (stop! [sys]))

(defprotocol Sim
  (handle [sim msg]))

;; now build the system
(defn build-system [name config]
  (let [sys (construct name config)]
    (start! sys)
    sys))

;; ... and we need something to implement the `Sys` protocol
(extend-type LowFiSim
  Sys
  (start! [this]
    (println "Started a lofi simulator."))
  (stop! [this]
    (println "Stopped a lofi simulator."))

  Sim
  (handle [this msg]
    (* (:weight msg) 3.14)))

;; now start the system
(start! (construct :lofi lofi))
;; => you'll see this printed in the REPL:
;; Started a lofi simulator.

;; ... you should see the same thing if you build the system
(build-system :lofi lofi)
;; Started a lofi simulator.
;; => #clojure_experiments.books.joy_of_clojure.ch17_clojure_way_of_thinking.LowFiSim{:name :lofi}

;; ... ask it to handle a message
(handle (build-system :sim1 lofi) {:weight 42})
;; => 131.88


;; let's extend HiFiSim too
(extend-type HiFiSim
  Sys
  (start! [this]
    (println "Started a hifi simulator."))
  (stop! [this]
    (println "Stopped a hifi simulator."))

  Sim
  (handle [this msg]
    (Thread/sleep 5000)
    (* (:weight msg) 3.1415926535897932384626M)))

(build-system :sim2 hifi)
;; Started a hifi simulator.
;; => #clojure_experiments.books.joy_of_clojure.ch17_clojure_way_of_thinking.HiFiSim{:name :sim2, :threads 2}

(comment
  (handle (build-system :sim2 hifi) {:weight 42})
  ;; Started a hifi simulator.
  ;; ... waiting 5 seconds
  ;; => 131.9468914507713160154292M
  ,)


;; Let's use the low-fidelity model for an immediate answer
;; and then wait for high-fidelity model to evantually replace it with a more precise answer
(def excellent (promise))

(defn simulate [answer fast slow opts]
  ;; calculate hifi on another thread
  ;; - accurate but expensive is delivered later
  (future (deliver answer (handle slow opts)))
  ;; calculate lofi immediatelly
  ;; - cheap but less accurate answer is returned immediatelly
  (handle fast opts))

(simulate excellent
          (build-system :sim1 lofi)
          (build-system :sim2 hifi)
          {:weight 42})
;; low fidelity answer returned immediatelly
;; => 131.88

;; false if you evaluate this immediatelly after the previous expression
(realized? excellent)
;; => false
;; wait at least 5 seconds for hifi to deliver a more precise answer
(comment
  @excellent
  ;; => 131.9468914507713160154292M
  ,)


;; let's now create a mock implementation
(defrecord MockSim [name])

(def starts (atom 0))
(def starts-per-instance (atom {}))

(extend-type MockSim
  Sys
  (start! [this]
    (if (= 1 (get (swap! starts-per-instance update this (fnil inc 0))
                  this))
      (println "Started a mock simulator.")
      (throw (RuntimeException. "Called start! more than once."))))
  (stop! [this]
    (println "Stopped a mock simulator."))

  Sim
  (handle [_ _] 42))


(defmethod construct [:mock nil]
  [nom _]
  (MockSim. nom))

(def config {:type :mock :lib 'clojure-experiments.books.joy-of-clojure.ch17-clojure-way-of-thinking})

(defn initialize [name cfg]
  (let [lib (:lib cfg)]
    ;; maybe `serialized-require` or `requiring-resolve` would be preferred for thread-safety
    (require lib)
    (build-system name cfg)))

(comment
  (handle (initialize :mock-sim config)
          {})
  ;;=> 42

  ;; if you try again it fails even if it's another instace
  ;; - unless you use `starts-per-instance` atom instaed
  (handle (initialize :mock-sim2 config)
          {})
  ;;=> 42
  (handle (initialize :mock-sim2 config)
          {})
  ;; Called start! more than once.

  )


;;; Error handling and debugging (p. 447)

;; using dynamic binding for error handling

(defn traverse [node f]
  (when node
    (f node)
    (doseq [child (:content node)]
      (traverse child f))))

(traverse {:tag :flower :attrs {:name "Tanpopo"} :content []}
          println)
;; => nil

(def DB
  (->
   "<zoo>
      <pongo>
        <animal>organutan</animal>
      </pongo>
      <panthera>
        <animal>Spot</animal>
        <animal>lion</animal>
        <animal>Lopshire</animal>
      </panthera>
    </zoo>"
   .getBytes
   (java.io.ByteArrayInputStream.)
   xml/parse))

(traverse DB println)


;; let's handle intruders like Sopt and Lopshire
(defn ^:dynamic handle-weird-animal
  [{[name] :content}]
  (throw (Exception. (str name " must be 'dealt with'"))))

;; but first we need a function to make the actual delegation
(defmulti visit :tag)

;; needed to deal with other tags like :zoo, etc.
(defmethod visit :default [_])

(defmethod visit :animal [{[name] :content :as animal}]
  (case name
    ;; Note: could be `cond` and use `#{"Spot" "Lopshire"}
    "Spot" (handle-weird-animal animal)
    "Lopshire" (handle-weird-animal animal)
    (println name)))

;; the default error handling
(traverse DB visit)
;; 1. Unhandled java.lang.Exception
;;     Spot must be 'dealt with'

;; custom error handling
(binding [handle-weird-animal (fn [{[name] :content}] (println name "is harmless"))]
  (traverse DB visit))


;; we can be more sophisticated in error handling:
(defmulti handle-weird (fn [{[name] :content}] name))
(defmethod handle-weird "Spot" [_]
  (println "Transporting Spot to the circus"))
(defmethod handle-weird "Lopshire" [_]
  (println "Signing Lopshire to a book deal."))

;; notice it works across threads
(future
  (binding [handle-weird-animal handle-weird]
    (traverse DB visit)))
;; => prints:
;; organutan
;; Transporting Spot to the circus
;; lion
;; Signing Lopshire to a book deal.



;;; 17.4.2 Debugging
;;; Natural progression for Clojure newcomers when it comes to debugging
;;; 1. (println)
;;; 2. macros to make (1) easier
;;; 3. Some variations on debugging as described in this section
;;; 4. IDEs, monitoring & profiling tools

(defn div [n d] (int (/ n d)))


;; this fails...
#_(div 10 0)

;; ... so let's debug it

;; we would like to have `break` macro that will pause the execution

;; let's first override `clojure.main/repl` reader
(defn readr [prompt exit-code]
  (let [input (clojure.main/repl-read prompt exit-code)]
    (if (= input ::tl)
           exit-code
           input)))

;; now play
(comment
  ;; you will see stdin promtp and when you press enter this will return the form you entered
  ;; type [1 2 3]
  (readr #(print "invisible=> ") ::exit)
;; => [1 2 3]

  ;; type ::tl
  (readr #(print "invisible=> ") ::exit)
  ;; => :clojure-experiments.books.joy-of-clojure.ch17-clojure-way-of-thinking/exit

  ,)


;; overriding evaluator - :eval
(defmacro local-context []
  (let [symbols (keys &env)]
    ;; instead of `(quote ~sym) the usual way is just `'sym ? would that work?
    (zipmap (map (fn [sym] `'~sym)
                 symbols)
            symbols)))

(local-context)
;; => {}

(let [a 1 b 2 c 3]
  (let [b 200]
    (local-context)))
;; => {a 1, b 200, c 3}

;; no we want to evaluate forms in the local context
(defmacro break []
  `(clojure.main/repl
    :prompt #(print "debug=> ")
    :read readr
    :eval (partial contextual-eval (local-context))))

(defn div [n d] (break) (int (/ n d)))

(comment
  
  ;; type `(local-context)` then `::tl`
  (div 10 0)

  ,)

;; try multiple breakpoints
(defn keys-apply [f ks m]
  (break)
  (let [only (select-keys m ks)]
    (break)
    (zipmap (keys only) (map f (vals only)))))

(comment
  
  ;; try to enter this:
  ;; only
  ;; ks
  ;; ::tl
  ;; only
  ;; ::tl
  (keys-apply inc [:a :b] {:a 1 :b 2 :c 3})

  ,)

;; finally let's use `break` in the body of a macro
(defmacro awhen [expr & body]
  (break)
  `(let [~'it ~expr]
     (if ~'it
       (do (break) ~@body))))

(comment

  (awhen [1 2 3] (it 2))
;; => 3

  ,
  )
