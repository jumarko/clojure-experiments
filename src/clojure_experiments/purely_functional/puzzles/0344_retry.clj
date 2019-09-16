(ns clojure-experiments.purely-functional.puzzles.0344-retry
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-344-tip-thank-a-clojure-oss-dev-today/")


;;; Write a functiont that will re-try given function 3 times (retry when the fn throws an exception)
;;; and re-throws if no success after that.


(defn retry
  ([f] (retry 3 f))
  ([n f]
   (fn [& args]
     (loop [i 0]
       (let [[result exception] (try
                                  [(apply f args)]
                                  (catch Exception e
                                    ;; here we could just 'recur' or throw but cannot do that (compiler error)
                                    (println "WARN: got an exception when trying to call f with given args. " (.getMessage e))
                                    [::error e]))]
         (if (= ::error result)
           (if (< i n)
             (recur (inc i))
             (do (println "ERROR: max retries " n " reached!")
                 (throw exception)))
           result))))))

;; throws ArithmeticException
#_((retry /) 1 0)

;; success case
((retry /) 10 2)
;; => 5

;; eventually succeeds
((retry (let [counter (atom 1)
              max-failures 3] ;; change this to 4 or more to get ArithmeticException
          (fn div-failing-only-2-times [& args]
            (try
              (apply / args)
              (catch Exception e
                (if (> @counter max-failures)
                  (do (println "I'm not gonna failed anymore")
                      :sentinel)
                  (do
                    (swap! counter inc)
                    (throw e))))))))
 1 0)
;; => :sentinel
