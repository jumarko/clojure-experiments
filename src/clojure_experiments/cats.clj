(ns clojure-experiments.cats
  "For playing with `cats` library: https://github.com/funcool/cats
  Category theory and algebraic abstractions."
  (:require cats.builtin
            [cats.context :as ctx]
            [cats.core :as cats]
            [cats.monad.maybe :as maybe]))

;;; Semigroups: algrebraic data structure with an associative binary operation (mappend)
 
;; Most of the built-in collections are semigroups because
;; their associative operation is analogous to Clojure's into.
(cats/mappend [1 2 3] [4 5 6]) ;=> [1 2 3 4 5 6]

;; we can use maybe with semigroups
(cats/mappend (maybe/just [1 2 3])
              (maybe/just [4 5 6]))
;;=> #<Just [1 2 3 4 5 6]>



;;; Monoid: a semigroup with an identity element (mempty)
;;; For the Clojure collections, mepty is analogous to `empty`

;; we can mappend multiple Maybe with Nothing being an identity element:
(cats/mappend (maybe/just [1 2 3])
              (maybe/nothing)
              (maybe/just [4 5 6])
              (maybe/nothing))
;;=> #<Just [1 2 3 4 5 6]>



;;; Functor: represents some sort of computational context
;;; It has one unique function: `fmap`
;;;    (fmap [f fv])
;;; `fmap` takes a plain function and a value wrapped in a functor context.
;;; It extracts the inner value, applies the function and returns
;;; the result wrapped in the same type as the second parameter.
;;;
;;; Functor context is any type that acts as "Box"
;;; and implements `Context` and `Functor` protocols.

;; e.g. `fmap` over `Maybe` instance
(cats/fmap inc (maybe/just 2)) ;=> #<Just 3>

;; Nothing represents the absence of value.
;; It's a safe replacement for `nil`.
(cats/fmap inc (maybe/nothing)) ;=> #<Nothing>

;; Compared to following which raise NPE
#_(inc nil)

;; lets use `fmap` with plain Clojure vector
(cats/fmap inc [1 2 3]) ;=> [2 3 4]
;; compare it to plan `map` which returns lazy seqFrom
(map inc [1 2 3])



;;; Applicative functors: http://funcool.github.io/cats/latest/#applicative
;;; Similar to Functor but with the ability to execute function
;;; which is wrapped in the same context as the value
;;; Abstraction consists of two functions: `fapply` and `pure`.
;;;    `(fapply [af av])`


;; Imagine you have some factory function that, depending on the language,
;; returns a greeter function, and you only support a few languages.
(defn make-greeter
  [^String lang]
  (case lang
    "es" (fn [name] (str "Hola " name))
    "en" (fn [name] (str "Hola " name))
    nil))

;; Now, before using the greeter you need to always check
;; whether result is nil or valid value.
#_(make-greeter "sk") ;=> NPE

;; Let's convert the factory to Maybe
(defn make-greeter
  [^String lang]
  (case lang
    "es" (maybe/just (fn [name] (str "Hola " name)))
    "en" (maybe/just (fn [name] (str "Hello " name)))
    (maybe/nothing)))

;; Now you can apply the result to any value without defensive nil check
(cats/fapply (make-greeter "es") (maybe/just "Juraj")) ;=> #<Just "Hola Juraj">
(cats/fapply (make-greeter "sk") (maybe/just "Juraj")) ;=> #<Nothing>

;; `pure` function allows you to put some value
;; in side-effect-free context of the current type
;; `pure` is more explained in the next section



;;; Foldable: http://funcool.github.io/cats/latest/#foldable
;;; Generic abstraction for data structures that can be folded.
;;; Consists of two funtions: `foldl` and `foldr`
;;; `foldl` is also known as `reduce` or `inject`
;;; Both functions have the identical signature and differs
;;; in how they traverse the data structure

;; foldl
(cats/foldl (fn [acc v]
              (println "foldl: acc=" acc ", v=" v)
              (+ acc v))
            0
            [1 2 3 4 5])
;; and foldr
(cats/foldr (fn [v wc]
              (println "foldr: v=" v ", wc=" wc)
              (+ v wc))
            0
            [1 2 3 4 5])

;; In languages with strict evaluation,
;; `foldr` doesn't have many applications,
;; because it tends to consume all the stack

;; `foldl` can be aplied to Maybe and more
(cats/foldl #(cats/return (+ %1 %2))
            1
            (maybe/just 2))

;; `foldm` function i analogous to `foldl` with the difference
;; that it's aware of monad context (i.e. it works with reducing functions returning monads)
(defn m-div
  [x y]
  (if (zero? y)
    (maybe/nothing)
    (maybe/just (/ x y))))
(cats/foldm m-div 1 [1 2 3]) ;=> #<Just 1/6>
(cats/foldm m-div 1 [1 0 3]) ;=> #<Nothing>


(reduce (fn [acc v]
          (println acc v)
          (if-let [result (and acc v)]
            result
            (reduced false)))
        true
        [true false false false])


;;; Traversable: http://funcool.github.io/cats/latest/#traversable
;;; generic abstraction for data structures that can be traversed from left to right
;;; running an Applicative action for each element.
;;; Traversables must also be Functors and Foldables.
;;; Since Traversables use the Applicative's `pure` function
;;; the context of of the applicative must be set when using `traverse` function

;; Example: we have a vector with numbers that we want to map to a Maybe value
;; and we want to aggregate the result in a Maybe.
;; If any of the actions fails the resulting aggregate will be Nothing,
;; but if it succeeds we preserve the resulting vector in `just`.
(defn just-if-even
  [n]
  (if (even? n)
    (maybe/just n)
    (maybe/nothing)))

(ctx/with-context maybe/context
  (cats/traverse just-if-even []))

(ctx/with-context maybe/context
  (cats/traverse just-if-even [2 4]))

(ctx/with-context maybe/context
  (cats/traverse just-if-even [1 2]))

(ctx/with-context maybe/context
  (cats/traverse just-if-even [2 3]))



;;; Monads: http://funcool.github.io/cats/latest/#monads
;;; Like functors and applicatives, monads deal with data in contexts.
;;; Additionaly, monads can also transform contexts by unwrapping data,
;;; applying functions to it and putting new values in a completely different context.
;;; Monad abstraction consists of two fuctions: `bind` and `return`.
;;;    (bind [mv f])
;;; In monad, the function is responsible for wrapping a returned value in a context

(cats/bind (maybe/just 1)
           (fn [v] (maybe/just (inc v))))

;; Key feature: any computation executed within the context of bind (monad)
;; knows the context type implicitly
;; You can use `return` in this case (if you don't know the type of the context).
(cats/bind (maybe/just 1)
           (fn [v]
             (cats/return (inc v))))

;; mlet function for easier composition
(cats/mlet [a (maybe/just 1)
            b (maybe/just (inc a))]
           (cats/return (* b 2)))



;;; MonadZero: http://funcool.github.io/cats/latest/#monadzero
;;; Some monads have the notion of an identity element similar to Monoids

;; E.g. for Maybe, the identity element is Nothing
;; doesn't work in our version of cats library
#_(cats/mzero maybe/maybe-monad)
