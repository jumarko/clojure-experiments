(ns clojure-experiments.truss
  "Experiments with Truss library: https://github.com/ptaoussanis/truss.
  Motivated by defn podcast episode with Peter Taoussanis.
  See also https://clojureverse.org/t/pragmatic-programmer-should-we-all-use-assert/9106
  and https://gist.github.com/mjmeintjes/e277d014a3f971b3a3da5fddf29aee30"
  (:require [taoensso.truss :as it :refer [have have! have?]]
            [clojure.string :as str]
            ))

;;;; See also Introducing Truss library for Clojure and ClojureScript
;;;;    https://www.youtube.com/watch?v=gMB4Y-EIArA&feature=youtu.be


;;; assertions & preconditions

(defn square [n]
  (assert (integer? n))
  (* n n))

(defn square
  [n]
  {:pre [(integer? n)]}
  (* n n))

(defn square [n]
  (have integer? n)
  (* n n))

(comment
  (square 5)
  (square "5")

  )

;;; another type of assertions
(defn square [n]
  (assert (integer? (/ 2 n)))
  (* n n))

(square 2)
;; this example doesn't throw proper AssertionError because before it can be checked
;; assert macro evaluates the expression and will get "Division by zero"
#_(square 0)

;; contrast it to the `have` macro where you get the proper error message
(defn square [n]
  (have integer? (/ 2 n))
  (* n n))
#_(square 0)


;;; multi-arity have

;; tedious with clojure assertions/preconditions
(defn join [x y]
  {:pre [(string? x)
         (string? y)]}
  (str x y))
(join "hello, " "world")
#_(join "hello, " 5)

;; much better with multi-arity have
(defn join [x y]
  (let [[x y] (have string? x y)]
    (str x y)))
(join "hello, " "world")
#_(join "hello, " 5)


;;; ring handler
;;; let's say we need more info to be able to debug the problem in our handler
;;; e.g. including request data, session, etc.
;;; => use `:data` to add arbitrary debug information
(defn my-handler [ring-req x y]
  (let [[x y] (have integer? x y
                    :data {:ring-req ring-req
                           :foo "bar"
                           ;; our app state
                           ;; thread locals ...
})]
    (* x y)))
#_(my-handler {:foo :bar} 5 nil)

;; thread-local dynamic binding


;;; collections/vectors
(defn full-name [names]
  (assert (every? string? names))
  (str/join " "
            (map str/capitalize names)))
          
#_(full-name ["Stu" "Alice" nil]) ;=> Just 'Assert failed: (every? string? names)' not very useful!

(defn full-name [names]
  (have string? :in names)
  (str/join " "
            (map str/capitalize names)))

#_(full-name ["Stu" "Alice" nil])


;;; arbitrary predicates
(def app-state (atom {}))

(defn foo [x y z]
  (have
   (fn [state]
     ;; do a check based on arbitrary application data
     ,,,)
   @app-state)
  ,,,)


;;; convenient syntax
(defn foo [x]
  (str (have string? x) ", world"))
(foo "hello")

;; let's say we want to have x be either nil or string
;; one way to do it is this:
(defn foo [x]
  (str (have #(or (string? %) (nil? %)) x) ", world"))
(foo nil)

;; but following is more compact:
(defn foo [x]
  (str (have [:or string? nil?] x) ", world"))
(foo nil)

(comment
  
  ;; :and
  (have [:and int? pos?] 0)

  ;; :el
  (have [:el #{:a :b :c}] :d)
  ;; :el can also be simplified to ?
  (have #{:a :b :c} :d)
  ;; perhaps it's useful for vectors?
  (have [:el [:a :b :c]] :d)

  ;; :ks<=
  (have
   [:ks<= #{:a :b :c}]
   {:a "a" :b "b" :c "c"})
  ;; this will pass
  (have
   [:ks<= #{:a :b :c}]
   {:a "a" :b "b"})
  ;; this will fail
  (have
   [:ks<= #{:a :b :c}]
   {:a "a" :b "b" :c "c" :d "d"})

  )


;; (have x) == (have some? x)
