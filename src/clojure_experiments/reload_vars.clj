(ns clojure-experiments.reload-vars
  "Experiments with vars and reloadability.
  Motivated by Clojurians slack discussion: https://clojurians.slack.com/archives/C03S1KBA2/p1643466684411559?thread_ts=1643396807.132739&cid=C03S1KBA2")


;;; https://clojurians.slack.com/archives/C03S1KBA2/p1643466684411559?thread_ts=1643396807.132739&cid=C03S1KBA2
;; Joshua Suskalo: @jumar when you are making like a data literal or something and you put a function name in it and the data structure is evaluated (and since it's a data structure it evaluates to itself), that means that the function name is looked up as a var, and because it's a var it gets dereferenced, and what's in the var is a function object. 
;;     * So now you have a data structure with a function object in it. 
;;     * If you now go and change the function and re-evaluate it, a brand new function object is created and put into the var.
;;     *  The data structure isn't updated because it doesn't know about the var, it just has a function object that points to what the programmer now thinks of as outdated code.
;; * This is the root cause of all the var indirection reloadability woes. 
;;     * Any time you pass a function as an argument to the construction of a persistent object, or to the start of a long-lived process, you'll run into this "problem".
;; * In these cases the way to fix the problem is to introduce a level of indirection, so that whenever the code wants to call the function, it has to first look up the var. 
;;     * Well as it turns out vars implement the IFn interface, and what they do when called as a function is dereference themselves and then call the result as a function. 
;;     * So in most of these cases you can get away with just passing a function with a var quote, and now you're not passing a function, you're passing a var.
;; * The reason this sometimes gets confusing is because you can have one snippet of code where different lines are executed at different times, either because it's in a macro, or because some code is in a lambda and some is not. 
;;     * Vars are looked up when the code is run, not when it's compiled (besides to just validate that you're not referencing an unbound var), so that means that you can have code in a lambda that "reloads just fine" while other code does not, and that's all because of when the evaluation happens.


(defn reload-me [x]
  x)

(defn using-it [f]
  (f 10))

;;; partial captures the value of `reload-me`:
(def us (partial using-it reload-me))
(us)
;; => 10

;; this call will keep using the old value of reload-me
(defn reload-me [x]
  (inc x))
(us)
;; => 10

;; ... until you re-eval the above def
(def us (partial using-it reload-me))
(us)
;; => 11


;;; if you use an anonymous function, than it's ok
(def us #(using-it reload-me))
(us)
;; => 11
(defn reload-me [x]
  (inc (inc x)))
;; No need to redefine `us` here!
(us)
;; => 12


;;; if you store it in a data structure it's also captured
(def data-f (:f {:f reload-me}))
(data-f 10)
;; => 12
(defn reload-me [x]
  (inc (inc (inc x))))
;; This should print 13, but prints 12:
(data-f 10)
;; => 12
