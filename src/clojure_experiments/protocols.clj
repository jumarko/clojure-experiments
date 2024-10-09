(ns clojure-experiments.protocols
  "Examples showing the clojure protocols, starting with Sean's Devlin talk
  'Clojure Protocols': https://www.youtube.com/watch?v=kQhOlWXXl2I"
  (:import org.joda.time.DateTime))

;;;; Sean's 'Clojure Protocols' talk: https://www.youtube.com/watch?v=kQhOlWXXl2I
;;;; ================================

;;; Dateable protocol
;;;
(defprotocol Dateable
  (to-ms [t]))

;; this shows an alternative syntax which is probably obsolete
(extend java.lang.Number
  Dateable
  {:to-ms identity})

(extend-protocol Dateable
  java.util.Date
  (to-ms [d] (.getTime d))

  java.util.Calendar
  (to-ms [c] (to-ms (.getTime c)))

  java.sql.Timestamp
  (to-ms [t] (.getTime t))

  DateTime
  (to-ms [jdt] (.getMillis jdt))

  clojure.lang.IPersistentVector
  (to-ms [[y m d h mi s ms z]] )
  )

;; examples
(comment
  (to-ms (java.util.Date.))

  (to-ms (DateTime.))

  (to-ms (java.util.Calendar/getInstance))
  )

;; dispatched fns
(defn long-time
  ([] (.getTime (java.util.Date.)))
  ([t] (to-ms t)))

(defn date
  ([] (java.util.Date.))
  ([t] (java.util.Date. (long-time t))))

(defn joda-date
  ([] (DateTime.))
  ([t] (DateTime. (long-time t))))

;; predicates
(defn compare-time
  "Compares two times objects."
  [a b]
  (.compareTo (date a) (date b)))

(sort compare-time [(java.util.Date.) 10001 (DateTime. 2018 1 1 10 10 10)])


;;; Bug in Clojure 1.10 with meta-based dispatch
;;; https://dev.clojure.org/jira/browse/CLJ-2426
(defprotocol Foo (foo [x]))
(foo (with-meta [42] {`foo (fn [x] :boo)}))
;; => :boo

;; but `satisfies?` doesn't work
(satisfies? Foo (with-meta [42] {`foo (fn [x] :boo)}))
;; => false

(defrecord FooImpl [] Foo (foo [x] :impl))
(satisfies? Foo (->FooImpl))
;; => true


;;; extenders: https://clojuredocs.org/clojure.core/extenders
(defprotocol P (id [this]))
(extend-protocol P
  String
  (id [this] this)
  clojure.lang.Symbol
  (id [this] (name this))
  clojure.lang.Keyword
  (id [this] (name this)))

(extenders P)
;; => (java.lang.String clojure.lang.Symbol clojure.lang.Keyword)

;; unfortunately, it doesn't cover defrecord and deftype
(defrecord PP []
    P
    (id [this] (str this ":" this)))

(deftype PPP []
    P
  (id [this] (str this ":" this ":" this)))

(extenders P)
;; => (java.lang.String clojure.lang.Symbol clojure.lang.Keyword)

(extends? P PPP)
;; => true
