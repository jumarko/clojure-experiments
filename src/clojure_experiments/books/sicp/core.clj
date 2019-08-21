(ns clojure-experiments.books.sicp.core
  "Core reusable functions"
  ;; require other namespaces to get them loaded automatically
  )

;;; helper "get-stack" functions to get better overview of space "orders of growth"
;;; see also `clojure.main/java-loc->source` private function
(defn get-stack
  ([] (get-stack nil))
  ([fn-name]
   (->> (Thread/currentThread)
        .getStackTrace
        seq
        (filter (if-not fn-name
                  (constantly true)
                  #(.contains (.getClassName %)
                              (clojure.string/replace fn-name "-" "_")))))))
(defn get-stack-depth
  [fn-name]
  (-> (get-stack fn-name)
      count
      ;; there are always two elements in stack per fn call - invokeStatic and invoke
      (#(/ % 2))))



