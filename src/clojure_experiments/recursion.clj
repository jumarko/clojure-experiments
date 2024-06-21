(ns clojure-experiments.recursion)

;;; When is `recur` in "tail position"??

;; `cond` doesn't work?
;; => Yes it does!
(loop [i 10]
  (println i)
  (cond
    (= i 7) (recur (- i 4))
    (= i 4) (recur (- i 2))
    (= i 3) (recur (inc i))
    (> i 0) (recur (dec i))
    :else :done))

;; you cannot recur from within catch block:
;; - What is the technical or ideological reason for not allowing `recur` from inside a `catch` block? https://ask.clojure.org/index.php/13924/technical-ideological-reason-allowing-recur-inside-catch
;;   -> try can have a finally which runs after the catch, so you can't recur from inside a catch as it is not necessarily the tail position.

(comment
  (defn my-try-catch-loop []
    (loop [i 0]
      (try
        (parse-long (str i))
        (catch Exception e
          ;; compilation error: Can only recur from tail position
          (if (< i 10)
            i
            (recur (inc i)))))))
  ;;
  )
