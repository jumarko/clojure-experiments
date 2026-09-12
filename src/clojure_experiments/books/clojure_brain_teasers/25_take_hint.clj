(ns clojure-experiments.books.clojure-brain-teasers.25-take-hint)

(def ^double PI 3.14159) ;; NOTE: this type hint has no effect!

(= 'double (-> #'PI meta :tag));; => false

;;; Explanation

;; the PI var is not tagged as primitive type double
;; but rather this:
(-> #'PI meta :tag)
;; => #function[clojure.core/double]

;; Remember - type hints are just hints - the actual value doesn't have to be of the claimed type

;; Difference between function hints and var hints: metadata applied to the var are _evaluated_.
;; In this case, `'double` type hint is evaluated to the `double` function which is not a valid type hint!!!
;; => this type hint has no effect!

;; Notice this actually fails!
(comment
  ;; Throws: Unable to resolve classname: clojure.core$double@244987c2
  (Math/abs PI)
  :-)

;;; The proper way -> quote the type hing
;;; Also, you must use full var metadata form because the short-hand doesn't support quoted forms
(def ^{:tag 'double} PI 3.14159)

(-> #'PI meta :tag)
;; => double

;; Now we don't get any reflection warning
(set! *warn-on-reflection* true)
(Math/abs PI);; => 3.14159
;; ... BUT: `PI` here is still a boxed number (var values are always Java objects)
;; - the java code will look like this:
;;        Math.abs(RT.doubleCast(cjd__init.__cbc25_take_hint_PI.getRawRoot()));




;;; :const -> to get rid of unboxing
(def ^{:tag 'double :const true} PI 3.14159)

(Math/abs PI) ; no reflection, primitive value inlined
;; => 3.14159
;; - the java code will look like this:
;;                Math.abs(3.14159);
