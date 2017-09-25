(ns clojure-repl-experiments.oop
  "Object Oriented concepts - as applied. to Clojure.")

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
