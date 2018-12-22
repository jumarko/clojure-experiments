(ns clojure-experiments.oop
  "Object Oriented concepts - as applied. to Clojure."
  (:require [clojure.core.async :as async :refer [<!! >!!]]))

;;; Clojure Tutorials - OOP Lesson 1 - Basic of Object Oriented Programming
;;; https://www.youtube.com/watch?v=6tFSrnK7vmw&t=3s

;; definition of Object Oriented Programming:
;; http://www.dictionary.com/browse/object-oriented-programming
"A Schematic paradigm for computer programming in which
the linear concepts of procedures and tasks
are replaced by the concepts of objects and messages.
An object includes a package of data and a description
of operations that can be performed on that data.
A message specifies one of the operations, but unlike a procedure,
does not describe how the operation should be carried out.
C++ is an example of object oriented programming language."


(def my-object
  {:ops {:add-score (fn add-score [state score]
                      (update-in state [:data :scores] conj score))
         :sum-scores (fn sum-scores [state]
                       (apply + (get-in state [:data :scores])))}
   :data {:scores []}})

(defn send-msg [obj msg-type & args]
  (let [op (get-in obj [:ops msg-type])]
    (apply op obj args)))

(-> my-object
    (send-msg :add-score 42)
    (send-msg :add-score 1)
    (send-msg :sum-scores))

;; and we can be polymorphic!
(def my-object2
  (assoc-in my-object [:ops :avg-score]
            (fn avg-score [state]
              (let [scores (get-in state [:data :scores])]
                (/ (apply + scores)
                   (count scores))))))
(-> my-object2
    (send-msg :add-score 42)
    (send-msg :add-score 1)
    (send-msg :avg-score))



;;; Clojure Tutorials: OOP Lesson 2 - dispatching methods
;;; https://www.youtube.com/watch?v=e8mLbcfbl1g&t=2s

;; we can rewrite our previous example using Clojure constructs
(defprotocol Scoring
  (add-score [this score])
  (sum-scores [this]))

(defrecord ScoreNow [scores]
  Scoring
  (add-score [this score]
    (assoc this :scores (conj scores score)))
  (sum-scores [this]
    (apply + scores)))

(-> (->ScoreNow [])
    (add-score 42)
    (add-score 1)
    (sum-scores))

;; we can examine protocol if we want
Scoring


;; let's do a little experiment defining the protocol for our previous hash map
(defprotocol IObject
  (send-msg [this msg args]))

(def my-object
  {:ops {:add-score (fn add-score [state score]
                      (update-in state [:data :scores] conj score))
         :sum-scores (fn sum-scores [state]
                       (apply + (get-in state [:data :scores])))
         :default (fn default [state & rest]
                    (println "CAN'T EXECUTE"))}
   :data {:scores []}})


(extend-protocol IObject
  clojure.lang.PersistentArrayMap
  (send-msg [obj msg args]
    (let [op (get-in obj [:ops msg]
                     (get-in obj [:ops :default]))]
      (apply op obj args))))

(send-msg my-object :sum-sc [])
(send-msg my-object :sum-scores [])



;;; Clojure Tutorials - OOP Lesson 3 
;;; https://www.youtube.com/watch?v=0RZ7DbEwkHo

;; Back to the definition of Object Oriented Programming:
;; http://www.dictionary.com/browse/object-oriented-programming
"A Schematic paradigm for computer programming in which
the linear concepts of procedures and tasks
are replaced by the concepts of objects and messages.
An object includes a package of data and a description
of operations that can be performed on that data.
A message specifies one of the operations, but unlike a procedure,
does not describe how the operation should be carried out.
C++ is an example of object oriented programming language."

;; let's try to implement `print-obj`
(defn print-obj [prefix]
  (let [c (async/chan)]
    (async/thread
      (loop []
        (when-some [[cmd data] (<!! c)]
          (case cmd
            :print (do (print prefix data)
                       (recur))
            (recur)))))
    c))

(let [o (print-obj "Printed -> ")]
  (>!! o [:print 42])
  (async/close! o))

;; let's try it diferrently
;; here we can see that we have local state `scores`
;; and list of supported message in `case` body.
(defn scoring-obj []
  (let [c (async/chan)]
    (async/thread
      (loop [scores []]
        (when-some [[cmd data] (<!! c)]
          (case cmd
            :add-score (recur (conj scores data))
            :sum-scores (do (>!! data (apply + scores))
                            (recur scores))
            (do
              (println "Bad Command : " cmd data)
              (recur scores))))))
    c))

(let [o (scoring-obj)]
  (>!! o [:add-score 42])
  (>!! o [:add-score 1])
  (let [reply-chan (async/chan)]
    (>!! o [:sum-scores reply-chan])
    (println "Summed: " (<!! reply-chan)))
  (async/close! o))

;; The previous implementation is pretty much the actor model!
;;
;; We as functional programmers often shy away from this because:
;; - State is opaque / hidden in the object (`scores`)
;; - In reality, you never know what's going to happen when you send a message.
;; - There's no return value returned from "send message" operation
;;
;; We could easily pack our object and send it to the another machine.
;;
;; Actors combines the worst aspects of OOP (mutable state)
;; with asynchronous programming which makes them even worse!



;;; Clojure Tutorials - OOP Lesson 4
;;; https://www.youtube.com/watch?v=fThssYBkkqI&t=77s
;;; Looking at how dispatch works in OO languages

;; People often look at following code defining an interface
;; and say: "Ah, this is OOP!"
;; But it really isn't - there's nothing here that specifies
;; where these functions are stored
(defprotocol IScores
  (add-score [this score])
  (sum-scores [this]))
IScores
;==>
#_{:on clojure_repl_experiments.oop.IScores,
 :on-interface clojure_repl_experiments.oop.IScores,
 :sigs {:add-score {:name add-score, :arglists ([this score]), :doc nil},
        :sum-scores {:name sum-scores, :arglists ([this]), :doc nil}},
 :var #'clojure-experiments.oop/IScores,
 :method-map {:sum-scores :sum-scores, :add-score :add-score},
 :method-builders {#'clojure-experiments.oop/add-score #function[clojure-experiments.oop/eval95042/fn--95043], #'clojure-experiments.oop/sum-scores #function[clojure-experiments.oop/eval95042/fn--95056]}}

;; Those functions are not members of objects that define their implementation
;; These are just polymorphic functions
;; So we can do something like this:
(let [impls (atom {})]
  (defn add-score [this val]
    ((@impls (type this)) this val))
  (defn extend-add-score [tp f]
    (swap! impls assoc tp f)))

(extend-add-score (type [])
                  conj)
(add-score [] 1)

(extend-add-score (type {})
                  (fn [acc i] (assoc acc i i)))
(add-score {} 1)

;; And protocols are pretty similar => they are not OOP
;; they are just polymorphic functions that dispatch on the type
;; of the first argument

;; we can do the same thing with multimethods:
(defmulti add-scores (fn [acc _] (type acc)) )
(defmethod add-scores (type [])
  [acc val]
  (conj acc val))
(add-scores [] 1)

;; Because functions are stored in protocol itself, they are namespaced.
;; => you can extend existing types to protocol
;; => you can have multiple fns with the same name implemented by one object
