(ns clojure-experiments.performance.tufte
  "Some 'performance logging' with tufte: https://github.com/ptaoussanis/tufte
  See also https://cuddly-octo-palm-tree.com/posts/2022-01-23-opt-clj-2/"
  (:require [taoensso.tufte :refer [defnp p profiled profile] :as tufte]))


;;; Profile with tufte: https://github.com/ptaoussanis/tufte#how-does-tufte-compare-to-hugoduncancriterium


(comment
  ;; We'll request to send `profile` stats to `println`:
  (tufte/add-basic-println-handler! {})

;;; Let's define a couple dummy fns to simulate doing some expensive work
  (defn get-x [] (Thread/sleep 500)             "x val")
  (defn get-y [] (Thread/sleep (long (rand-int 1000))) "y val")


  ;; with simple cider profiling: `cider-profile-ns-toggle` and `cider-profile-summary`
  (dotimes [_ 5]
    (p :get-x (get-x))
    (p :get-y (get-y)))
  ;; |                                         :name | :n | :sum |   :q1 |  :med |   :q3 |   :sd |  :mad |
  ;; |-----------------------------------------------+----+------+-------+-------+-------+-------+-------|
  ;; | #'clojure-experiments.performance.tufte/get-x |  5 | 2.5s | 503ms | 504ms | 504ms |   1ms | 847µs |
  ;; | #'clojure-experiments.performance.tufte/get-y |  5 | 2.6s | 355ms | 499ms | 499ms | 319ms | 223ms |


  ;; With tufte - notice that it's nicer because it contains more statistics and also percentages
  ;; - but you have to manually instrument it :(
  (profile ; Profile any `p` forms called during body execution
   {} ; Profiling options; we'll use the defaults for now
   (dotimes [_ 5]
     (p :get-x (get-x))
     (p :get-y (get-y))))

  ;; pId           nCalls        Min      50% ≤      90% ≤      95% ≤      99% ≤        Max       Mean   MAD      Clock  Total

  ;; :get-y             5    78.23ms   849.94ms   991.97ms   991.97ms   991.97ms   991.97ms   669.73ms  ±48%     3.35s     57%
  ;; :get-x             5   500.31ms   502.48ms   504.45ms   504.45ms   504.45ms   504.45ms   502.28ms   ±0%     2.51s     43%

  ;; Accounted                                                                                                   5.86s    100%
  ;; Clock                                                                                                       5.86s    100%


  .)

