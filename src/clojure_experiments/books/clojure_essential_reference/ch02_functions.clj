(ns clojure-experiments.books.clojure-essential-reference.ch02-functions)

;;; 
(defn with-metas
  "This function has metadata both at the beginning and the end"
  {:my-doc "special docs"}
  ;; notice that the function body must be wrapped in () otherwise the metadata would be returned
  ([x]
   (inc x))
  {:machine-doc "more special docs"})

#_(meta #'with-metas)
;; => {:ns #namespace[clojure-experiments.experiments],
;;     :name with-metas,
;;     :machine-doc "more special docs",
;;     :file
;;     "/Users/jumar/workspace/clojure/clojure-experiments/src/clojure_experiments/experiments.clj",
;;     :my-doc "special docs",
;;     :column 1,
;;     :line 1186,
;;     :arglists ([x]),
;;     :doc "This function has metadata both at the beginning and the end"}


;;; how to use metatadata for basic profiling

(defn ^:bench profile-me [ms]                 ;
  (println "Crunching bits for" ms "ms")
  (Thread/sleep ms))

(defn dont-profile-me [ms]
  (println "not expecting profiling"))

(defn- wrap [f]
  (fn [& args]
    (time (apply f args))))

(defn- make-profilable [v]
  (alter-var-root v (constantly (wrap @v))))

(defn- tagged-by [tag nsname]
  (->> (ns-publics nsname)
       vals
       (filter #(get (meta %) tag))))

(defn bench-ns [nsname]
  (->> (tagged-by :bench nsname)
       (map make-profilable)
       dorun))

(comment

  (profile-me 500)
  ;; Crunching bits for 500 ms

  (bench-ns 'clojure-experiments.books.clojure-essential-reference.ch02-functions)

  (profile-me 500)
  ;; Crunching bits for 500 ms
  ;; "Elapsed time: 502.422309 msecs"

  (dont-profile-me 0)
  ;; not expecting profiling

  ;; end
  )
