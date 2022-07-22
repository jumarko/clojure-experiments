(ns clojure-experiments.idioms)


;; clojurians - #idioms channel: https://clojurians.slack.com/archives/C03M5U2LLAC/p1658364375721519
(comment 
  (-> (symbol "clojure-experiments.foo" "bar")
      requiring-resolve
      ;; how to write the following line?
      (apply [{:a 1 :b 2 :c 3}]))
  .)
;; better alternative?
(let [foo-bar (requiring-resolve (symbol "clojure-experiments.foo" "bar"))]
  (foo-bar {:a 1 :b 2 :c 3}))

