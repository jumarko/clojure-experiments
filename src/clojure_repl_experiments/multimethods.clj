(ns clojure-repl-experiments.multimethods
  "inspired by Lambda island episode 45. and mainly 46. A la Carte Polymorphism.")

(defmulti testik "doc" {:added "1.0"} :type
  :default :my-default)
(defmethod testik ::animal [params]
  "Hello")
(defmethod testik :my-default [params]
  "DEFAULT")
(testik "ahoj")
;; => "DEFAULT"

;;; using global hierarchy
;;;
(testik {:type ::animal})
;; => "Hello"
(testik {:type ::dog})
;; => "DEFAULT"

(derive ::dog ::animal)
(testik {:type ::dog})
;; => "Hello"

(underive ::dog ::animal)
(testik {:type ::dog})
;; => "DEFAULT"

;;; using custom hierarchy
(def testik nil)

;; we could also use an atom to hold a hierarchy
;; but vars are usually ok for multimethod hierarchies
(def my-hierarchy (make-hierarchy))
(defmulti testik "doc" {:added "1.0"} :type
  :hierarchy #'my-hierarchy)
(defmethod testik :animal [params]
  "Hello")
(defmethod testik :default [params]
  "DEFAULT")

(defn my-derive! [child parent]
  (alter-var-root #'my-hierarchy derive child parent))
(defn my-underive! [child parent]
  (alter-var-root #'my-hierarchy underive child parent))
(my-derive! :dog :animal)

(isa? my-hierarchy :dog :animal)
;; => true
(testik {:type :dog})
;; => "Hello"

(my-underive! :dog :animal)
(testik {:type :dog})
;; => "DEFAULT"
