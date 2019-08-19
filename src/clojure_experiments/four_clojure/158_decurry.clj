(ns four-clojure.158-decurry
  "http://www.4clojure.com/problem/158#prob-title
  Write a function that accepts a curried function of unknown arity n.
  Return an equivalent function of n arguments.
  You may wish to read this: https://en.wikipedia.org/wiki/Currying")

(defn uncurry [f]
  (fn [& args]
    (reduce
     (fn [f-acc arg]
       (f-acc arg))
     f
     args)))


(= 10 ((uncurry (fn [a]
                  (fn [b]
                    (fn [c]
                      (fn [d]
                        (+ a b c d))))))
       1 2 3 4))

(= 24 ((uncurry (fn [a]
                  (fn [b]
                    (fn [c]
                      (fn [d]
                        (* a b c d))))))
       1 2 3 4))

(= 25 ((uncurry (fn [a]
                  (fn [b]
                    (* a b))))
       5 5))
