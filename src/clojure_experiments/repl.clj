(ns clojure-experiments.repl
  (:require [clojure.java.io :as io]))

;;; here's a primitive REPL
;;; Suggested by Ch. Grand and recommended by guys from Clojure vienna meetup: 14.3.2018
(defn simple-repl []
  (->> (read)
       eval
       println
       (while true)))

;;; reading clojure file - example from Getting Clojure book:
(defn read-source [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (loop [result []]
      (let [expr (read r false :eof)]
        (if (= expr :eof)
          result
          (recur (conj result expr)))))))

#_(def my-source (read-source "/Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/repl.clj"))

;; another REPL by Russ - doesn't work properly for some reason!!!
(defn russ-repl []
  (loop []
    (println (eval (read)))
    (recur)))

;;; Here's Russ' eval implementation
(defn eval-symbol
  "Just lookup symbols in current namespace."
  [expr]
  (.get (ns-resolve *ns* expr)))

(declare reval)

(defn eval-vector
  "Recursively evaluates vector's content."
  [expr]
  (vec (map reval expr)))

(defn eval-list
  "Recursively evaluates the content of the list and call it as a function."
  [expr]
  (let [evaled-items (map reval expr)
        f (first evaled-items)
        args (rest evaled-items)]
    (apply f args)))

(defn reval [expr]
  (cond
    (string? expr) expr
    (keyword? expr) expr
    (number? expr) expr
    (symbol? expr) (eval-symbol expr)
    (vector? expr) (eval-vector expr)
    (list? expr) (eval-list expr)
    :else :completely-confused))


(defn my-when [test body-fn]
  (if test
    (body-fn)))

(comment
  
  (my-when (> 1 0) #(println "Hello"))
  (my-when (< 1 0) #(println "Hello")))

(defmacro my-when-2 [test & body]
  `(if ~test
     (do ~@body)))

(comment
  (my-when-2 (> 1 0) (println "Hello"))
  (my-when-2 (> 1 0) (println "Hello") (println "another thing") (println "finally")))

(comment
  (def counter (ref 0 ))
  (dosync
   (commute counter (fn [x] (println "Hello") (inc x)))))


;;; Get the source code for any function passed in as a parameter

(defn var-source
  "This is almost an exact copy of `clojure.repl/source-fn` but
  accepting var instead of symbol in current ns (*ns*)."
  [v]
  (when-let [filepath (:file (meta v))]
    (when-let [strm (.getResourceAsStream (clojure.lang.RT/baseLoader) filepath)]
      (with-open [rdr (java.io.LineNumberReader. (java.io.InputStreamReader. strm))]
        (dotimes [_ (dec (:line (meta v)))] (.readLine rdr))
        (let [text (StringBuilder.)
              pbr (proxy [java.io.PushbackReader] [rdr]
                    (read [] (let [i (proxy-super read)]
                               (.append text (char i))
                               i)))
              read-opts (if (.endsWith ^String filepath "cljc") {:read-cond :allow} {})]
          (if (= :unknown *read-eval*)
            (throw (IllegalStateException. "Unable to read source while *read-eval* is :unknown."))
            (read read-opts (java.io.PushbackReader. pbr)))
          (str text))))))

(defn fn-source
  "Returns source of given function (object)."
  [f]
  (let [[ns-name sym-name]
        (-> (str f)
            (clojure.repl/demunge)
            (clojure.string/replace #"@.*$" "")
            (clojure.string/split #"/"))
        ns (the-ns (symbol ns-name))
        fn-var (ns-resolve ns (symbol sym-name))]
    (var-source fn-var)))

(comment
  (fn-source tap>)
;; => "(defn tap>\n  \"sends x to any taps. Will not block. Returns true if there was room in the queue,\n  false if not (dropped).\"\n  {:added \"1.10\"}\n  [x]\n  (force tap-loop)\n  (.offer tapq (if (nil? x) ::tap-nil x)))"
  )
