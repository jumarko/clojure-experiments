(ns clojure-experiments.books.clojure-brain-teasers.08-ex-nihilo
  "'ex nihilo' in Latin means 'out of thin air'.
  It's demonstrated by a map springing into existence when you
  `(assoc nil ...`).

  In fact, most clojure collection functions are polymorphic
  and does something useful when they get `nil` as an input.

  This behavior makes it easier to program in Clojure
  and removes lots of `nil?` checks and opportunities for errors.

  That said **Java interop** is special - there's no safety net there!
  That includes wrappers such as `clojure.string`.
  "
  (:require [clojure.string :as str]))


(nil? (assoc nil :ex :nihilo))
;; => false

(count nil);; => 0


(contains? nil :some-key);; => false

(get nil :some-key)
;; => nil

(first nil)
;; => nil

;; this one is slightly more interesting!
(rest nil)
;; => ()

;;; That said **Java interop** is special - there's no safety net there!
(str/capitalize nil)
;;=>
;; 1. Unhandled java.lang.NullPointerException
;; Cannot invoke "Object.toString()" because "s" is null

