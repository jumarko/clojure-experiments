(ns clojure-experiments.aws.waf
  (:require
   [babashka.fs :as fs]
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.java.io :as io]))

(comment
  ;; Create WAF.json by concatenating all the logs, e.g.
  ;;   cd /Users/jumar/Work/Codescene/CLOUD/Cloudfront/waf-prod/2022-11-29_prod
  ;;   fd -t f | xargs cat > ../ALL.json
  (def portal-logs
    (->> (slurp "/Users/jumar/Work/Codescene/CLOUD/Cloudfront/waf-staging/ALL.json")
         (str/split-lines)
         (map json/read-str)))

  ;; filter BLOCK actions and show clientIp and timestampe
  (->> portal-logs
       (filter (fn [{:strs [action]}]
                 (= "BLOCK" action)))
       (map (fn [{:strs [httpRequest timestamp]}]
              {:timestamp (java.util.Date. timestamp)
               :clientIp (get httpRequest "clientIp")
               :uri (get httpRequest "uri")}))))

;; see https://docs.aws.amazon.com/waf/latest/developerguide/logging-fields.html
(defn simple-log-record [{:strs [action httpRequest timestamp]}]
  (let [{:strs [args clientIp country headers uri]} httpRequest]
    {:action action
     :client-ip clientIp
     :country country
     :timestamp (java.util.Date. timestamp)
     :uri uri
     :query-string args
     :headers headers}))

(defn- read-waf-log [log-file]
  (->> (io/reader log-file)
       line-seq
       (map json/read-str)
       ;; this is not completely correct here because clients may want full data but let's see...
       (map simple-log-record)))

(defn read-waf-logs
  "Expects a directory with waf logs where there are subdirectories for every hour.
  Inside those, there are files with JSON records"
  [directory]
  (apply concat
         (for [dir (fs/list-dir directory)
               log-file-path (fs/list-dir dir)]
           (read-waf-log (.toFile log-file-path)))))

(comment 
  (def web-logs   (read-waf-logs "/Users/jumar/Work/Codescene/CLOUD/Cloudfront/waf-prod/2022-11-29_prod"))
  (first web-logs)

  (def client-ip "194.147.251.104")
  (def azure-requests (time (->> web-logs
                                 (filter (fn [req] (= client-ip (:client-ip req))))
                                 (map (fn [req] (dissoc req :headers :country :client-ip)))
                                 (sort-by :timestamp))))
  
  .)
