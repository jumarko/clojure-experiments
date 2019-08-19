(ns four-clojure.058-function-composition
  "Problem 58: Function Composition: http://www.4clojure.com/problem/58.
  Write a function that lets you do function composition.
  Don't use `comp`")

;;; First try:
(defn my-comp [& fns]
  (fn [& args]
    (loop [fns-to-apply (reverse fns)
           fn-args-or-result args]
      (if-let [f (first fns-to-apply)]
        (recur (rest fns-to-apply)
               [(apply f fn-args-or-result)]) ; wrap in vector to make the apply work on intermediate results
        (first fn-args-or-result)))))

;;; Better solution?
(defn my-comp [& fns]
  (fn [& args]
    (let [[first-fn & rest-fns] (reverse fns)]
      (reduce
       (fn [acc f] (f acc))
       (apply first-fn args)
       rest-fns))))

;;; Tests
(= [3 2 1] ((my-comp rest reverse) [1 2 3 4]))
(= [3 2 1] ((my-comp rest reverse) [1 2 3 4]))

(= 5 ((my-comp (partial + 3) second) [1 2 3 4]))

(= true ((my-comp zero? #(mod % 8) +) 3 5 7 9))

(= "HELLO" ((my-comp #(.toUpperCase %) #(apply str %) take) 5 "hello world"))
(= "HELLO" ((my-comp #(.toUpperCase %) #(apply str %) take) 5 "hello world"))
