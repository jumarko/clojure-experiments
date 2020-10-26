(ns clojure-experiments.books.joy-of-clojure.ch08-macros
  (:require [clojure.walk :as walk]
            [clojure.xml :as xml]))

;;; eval (p. 175)

(eval 42)
;; => 42

(eval '(list 1 2))
;; => (1 2)

;; This will fail with ClassCastException
#_(eval (list 1 2))


;; Now something more exciting - evaluate a condition that is a function call
(eval (list (symbol "+") 1 2))
;; => 3
;; it may help to see that this returns the list
(list (symbol "+") 1 2)
;; => (+ 1 2)
;; vs. just using this which is basically calling `(eval 3)`
(eval (+ 1 2))
;; => 3


;; contextual-eval -> local bindings
;; to mitigate issues with `eval` which uses global bindings
(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `'~v])
                    ctx)]
      ~expr)))
;; here the let bindings will be: [a (quote 1) b (quote 2)]
(contextual-eval '{a 1 b 2} '(+ a b))
;; => 3


;;; Control structures (p. 178)

;; do-until macro
(defmacro do-until
  [& clauses]
  (when clauses
    ;; Don't be confused: this `when` will get macroexpanded to `if` via `macroexpand-all`
    (list 'clojure.core/when (first clauses)
          ;; ... and this `if` will disappear because it's evaluated when the macro executes at compile time
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException. "do-until requires an even number of forms")))
          ;; "calling" do-until recursively
          (cons 'do-until (nnext clauses)))))

(macroexpand-1 '(do-until true (prn 1) false (prn 2)))
;; => (clojure.core/when true (prn 1) (do-until false (prn 2)))

;; just for fun you can try decompile to Java code
(require '[clj-java-decompiler.core :refer [decompile disassemble] :as decompiler])
#_(decompile (do-until true (prn 1) false (prn 2)))
(walk/macroexpand-all '(do-until true (prn 1) false (prn 2)))
;; => (if true (do (prn 1) (if false (do (prn 2) nil))))

(do-until
 (even? 2) (println "Even")
 (odd? 3) (println "Odd")
 (zero? 1) (println "You'll never see me")
 :lollipop (println "Truthy thing"))
;; Even
;; Odd
;; => nil

(macroexpand-1 
 '(do-until
   (even? 2) (println "Even")
   (odd? 3) (println "Odd")
   (zero? 1) (println "You'll never see me")
   :lollipop (println "Truthy thing")))
;; => (clojure.core/when (even? 2) (println "Even") (do-until (odd? 3) (println "Odd") (zero? 1) (println "You'll never see me") :lollipop (println "Truthy thing")))

(walk/macroexpand-all
 '(do-until
   (even? 2) (println "Even")
   (odd? 3) (println "Odd")
   (zero? 1) (println "You'll never see me")
   :lollipop (println "Truthy thing")))
;; => (if (even? 2) (do (println "Even") (if (odd? 3) (do (println "Odd") (if (zero? 1) (do (println "You'll never see me") (if :lollipop (do (println "Truthy thing") nil))))))))


;;; unless (p. 179)
(defmacro unless [condition & body]
  `(when-not ~condition
     ~@body))
;; implementation from the book uses if + not + do
(defmacro unless [condition & body]
  `(if (not ~condition)
     (do ~@body)))

(unless (even? 3) "Now we see it...")
;; => "Now we see it..."
(unless (even? 2) "Now we don't.")
;; => nil


;;; def-watched (p. 181)
;;; - calling a function whenever a var changes
(defmacro def-watched [name & value]
  `(do
     (def ~name ~@value)
     (add-watch (var ~name)
                :re-bind
                (fn [~'key ~'r old# new#]
                  (println old# " -> " new#)))))
(def-watched x 2)
(alter-var-root #'x inc)

(macroexpand-1 '(def-watched x 2))
;; => (do
;;     (def x 2)
;;     (clojure.core/add-watch
;;      #'x
;;      :re-bind
;;      (clojure.core/fn
;;       [key r old__25921__auto__ new__25922__auto__]
;;       (clojure.core/println old__25921__auto__ " -> " new__25922__auto__))))


;;; Using macros to change forms (p. 182 - 185)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; internally, we'll use this structure:
(comment
  {:tag <form>
   :attrs {}
   :content [<nodes]})

;;; Modeling domain - start with outer-level element
(defmacro domain
  {:style/indent 1}
  [name & body]
  ;; notice how we quote the map
  `{:tag :domain
    :attrs {:name (str '~name)}
    :content [~@body]}) ; body has to be inside vector otherwise we wouldn't be able to have more than one group inside a domain

;; next we'll define `grouping`s that go inside the domain's body
(declare handle-things)
(defmacro grouping
  {:style/indent 1}
  [name & body]
  `{:tag :grouping
    :attrs {:name (str '~name)}
    :content [~@(handle-things body)]})

(declare grok-attrs grok-props)

(defn handle-things [things]
  (for [t things]
    {:tag :thing
     :attrs (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                [c]
                [])}))

(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond
            (list? a) [:isa (str (second a))]
            (string? a) [:comment a]))))

(defn grok-props [props]
  (when props
    {:tag :properties
     :attrs nil
     :content (apply vector (for [p props]
                              {:tag :property
                               :attrs {:name (str (first p))}
                               :content nil}))}))
(def d
  (domain man-vs-monster
    (grouping people
      (Human "A stock human")
      (Man (isa Human)
           "A man, baby"
           [name]
           [has-beard?]))
    (grouping monsters
      (Chupacabra
       "A fierce, yet elusive creature"
       [eats-goats?]))))
d
;;=> 
{:content
 [{:content
   [{:tag :thing,
     :attrs {:name "Human", :comment "A stock human"},
     :content [{:tag :properties, :attrs nil, :content []}]}
    {:tag :thing,
     :attrs {:name "Man", :isa "Human", :comment "A man, baby"},
     :content
     [{:tag :properties,
       :attrs nil,
       :content
       [{:tag :property, :attrs {:name "name"}, :content nil}
        {:tag :property, :attrs {:name "has-beard?"}, :content nil}]}]}],
   :attrs {:name "people"},
   :tag :grouping}
  {:content
   [{:tag :thing,
     :attrs {:name "Chupacabra", :comment "A fierce, yet elusive creature"},
     :content
     [{:tag :properties,
       :attrs nil,
       :content [{:tag :property, :attrs {:name "eats-goats?"}, :content nil}]}]}],
   :attrs {:name "monsters"},
   :tag :grouping}],
 :attrs {:name "man-vs-monster"},
 :tag :domain}

;; let's convert this to XML!
(xml/emit d)
;; <?xml version='1.0' encoding='UTF-8'?>
;; <domain name='man-vs-monster'>
;;   <grouping name='people'>
;;     <thing name='Human' comment='A stock human'>
;;       <properties>
;;       </properties>
;;     </thing>
;;     <thing name='Man' isa='Human' comment='A man, baby'>
;;       <properties>
;;         <property name='name'/>
;;         <property name='has-beard?'/>
;;        </properties>
;;     </thing>
;;   </grouping>
;;   <grouping name='monsters'>
;;     <thing name='Chupacabra' comment='A fierce, yet elusive creature'>
;;       <properties>
;;         <property name='eats-goats?'/>
;;        </properties>
;;     </thing>
;;   </grouping>
;; </domain>


