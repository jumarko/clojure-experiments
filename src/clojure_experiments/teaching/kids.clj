(ns clojure-experiments.teaching.kids)


;;; Say hello
(println "Hello, Elenka")

;; Say many times
(comment
 (dotimes [i 10]))

;;; Count
(defn count!
  ([from to] (count! from to 1000))
  ([from to sleep-interval-millis]
   (println)
   (time
    (doseq [i (range from (inc to))]
      (print " " i)
      (when (zero? (mod i 10))
        (newline))
      (flush)
      (when (zero? (mod i 100))
        (println "-------------------------------------------------"))
      (Thread/sleep sleep-interval-millis)))))

;; count!
(comment
  (count! 1 1000)

  ;; count really fast:
  (count! 1 1000 10)
  )
