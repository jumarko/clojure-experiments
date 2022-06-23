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


;;; defrecord's method bodies are _not_ closures! https://clojuredocs.org/clojure.core/defrecord
;;; > Note that method bodies are not closures,
;;; > the local environment includes only the named fields,
;;; > and those fields can be accessed directly.
(defrecord simple [s]
  clojure.lang.IFn
  (invoke [this] (str "I'm crazy: " s)))

(def ss (->simple "ahoj"))

(ss)
;; => "I'm crazy: ahoj"

((assoc ss :s "hello"))
;; => "I'm crazy: hello"
