(ns clojure-experiments.destructuring
  "Playing with Clojure destructuring.
  https://clojure.org/guides/destructuring")





;;; interesting behavior of when-let when using destructuring
;;; there's just no way to use `when-let`/`if-let` on destructured values
(def foo {:a 1})

(when-let [a (:a foo)] (println a))
;; => prints "1", returns nil

(when-let [{:keys [a]} foo] (println a))
;; => prints "1", returns nil

(when-let [a (:a (assoc foo :a nil))] (println a))
;; => returns nil

(when-let [{:keys [a]} (assoc foo :a nil)] (println a))
;; => prints "nil", returns nil



(defn private-fun
  [opts]
  ,,,)

(defn public-fun
  [{:keys [timeout-ms] :as opts
    :or {timeout-ms 1000}}]
  ;; wrong! because default value of timeout-ms is not conveyed in opts.
  (private-fun opts))

(defn public-fun2
  [{:keys [timeout-ms] :as opts}]
  ;; bad!  the user dunno the default value from the parameter hint in lsp.  I can add the information to the 
  ;; docstring. but it is indeed extra work.
  (private-fun (merge {:timeout-ms 1000} opts)))
