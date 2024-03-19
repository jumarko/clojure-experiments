(ns clojure-experiments.retry
  (:require
   [clojure.math :as math]
   [clj-http.client :as http]))

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



;;; Emil's retry attempt
(defn- exponential-backoff [x]
  (math/pow 2 x))
(exponential-backoff 3)

(defn retry-loop
  "Because of the IP address whitelisting required to access staging, we need to
  wait for the IP whitelisting to complete. It takes about ~30 seconds until it
  has propagated. Retrying with exponential backoff."
  [f request-url request retries]
  (loop [x 0]
    (when (< x retries)
      (let [result (f request-url request)]
        (if (= 403 (result :status))
          (do
            (println "Got status 403 - expected while waiting for staging firewall to open, on retry number " x + " - trying again in " (exponential-backoff x))
            (Thread/sleep (long (exponential-backoff x)))
            (recur (inc x)))
          result)))))

;; alternative to the `retry-loop` function above
(comment 
  (let [retries 30
        wait-time 2000
        url "https://staging.codescene.io"
        req {:throw-exceptions? false}
        f (fn [] (http/get url req))]
    (->> (repeatedly retries f)
         (drop-while (fn [resp]
                       (when (= 403 (:status resp))
                         (println "Got status 403 - expected while waiting for staging firewall to open")
                         (Thread/sleep wait-time)
                         true)))
         first)))

