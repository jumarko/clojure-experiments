(ns clojure-experiments.retry)

;; see also https://github.com/joegallo/robert-bruce
;; hiredman: I use trampoline to roll my own retry loops all the time

;; my naive attempt for a retry function
(defn retry [{:keys [attempts] :or {attempts 0} :as opts}
             f
             & args]
  (if (pos? attempts)
    (try (apply f args)
         (catch Exception e
           #(apply retry (-> opts
                             (update :attempts dec)
                             (assoc :error e))
                   f
                   args)))
    (throw (ex-info "No more retries" {:opts opts}))))

(comment 
  (trampoline retry
              {:attempts 3}
              (fn [] (/ 1 0)))
  ,)
