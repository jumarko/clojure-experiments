(ns clojure-experiments.records
  "Experiments with def-records.")

(defrecord Person [first-name last-name])

(def john (->Person "John" "Doe"))
(:first-name john)
;; => "John"
(.first_name john)
;; => "John"

(def big-john (assoc john :middle-name "Big"))
(:middle-name big-john)
;; => "Big"
(comment
  (.middle_name big-john)
  ;; No matching field found: middle_name for class clojure_experiments.records.Person
.)

(.__extmap big-john)
;; => {:middle-name "Big"}
