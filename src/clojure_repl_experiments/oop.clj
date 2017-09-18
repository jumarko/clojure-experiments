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
