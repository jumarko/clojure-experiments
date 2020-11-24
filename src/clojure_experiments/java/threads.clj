(ns clojure-experiments.java.threads)

;; you can change thread stack size if JVM supports it:
;; - https://stackoverflow.com/questions/64829317/how-to-extend-stack-size-without-access-to-jvm-settings
;; - https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/Thread.html#%3Cinit%3E(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)

(defn rec [n]
  (if (pos? n)
    (+ 1 (rec (dec n)))
    0))

;; this will fail with the default thread stack size
#_(rec 10000)

;; but even larger number succeeds with larger stack size (5 MB?)
(comment 
  (.start (Thread. nil #(println (rec 50000)) "extended stack" 5000000))
  )
