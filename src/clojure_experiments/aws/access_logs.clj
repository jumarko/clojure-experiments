{(ns clojure-experiments.aws.access-logs
   "Utilities to process Elastic Beanstalk (nginx) access logs (aka 'webrequests')"
   (:require [clojure.java.io :as io]
             [clojure.string :as string]
             [clojure.spec.alpha :as s])
   (:import java.io.FilenameFilter))}

(def sample-line "10.0.0.141 - - [01/Feb/2019:22:07:03 +0000] \"GET /projects/4064/created HTTP/1.1\" 200 3120 \"-\" \"Amazon CloudFront\" \"100.12.186.227, 34.226.14.150\"")

(def ip-address-pattern "(\\d+\\.\\d+\\.\\d+\\.\\d+)")
(def timestamp-pattern "\\[(.+)\\]")
(def fetch-url-pattern "\"(GET|POST|HEAD|DELETE|PUT|OPTIONS|PATCH|CONNECT|TRACE)\\s+(\\S+).*\"")
(def response-status-and-size-pattern "(\\d+) (\\d+)")
(def source-destination-ip-pattern (format "\"%s, %s\"" ip-address-pattern ip-address-pattern))
(def access-log-line-pattern (re-pattern (str "^" ip-address-pattern
                                              ".+" timestamp-pattern
                                              ".+" fetch-url-pattern
                                              " " response-status-and-size-pattern
                                              ".+" source-destination-ip-pattern)))

(defn parse-access-log-line
  "Parses given line expected to be in the access og format defined by `access-log-line-pattern`.
  Returns parsed data as a map.
  Returns nil if not matched.
  See `sample-line`."
  [line]
  (let [[host-ip timestamp request-method request-url response-status response-size source-ip destination-ip]
        (drop 1 (re-find access-log-line-pattern line))]
    (when host-ip
      {:host-ip host-ip
       :timestamp timestamp
       :request-method request-method
       :request-url request-url
       :response-status response-status
       :response-size response-size
       :source-ip source-ip
       :destination-ip destination-ip})))

#_(parse-access-log-line sample-line)

(s/def ::access-log-record
  (s/nilable (s/keys :req-un [::host-ip ::timestamp ::request-method ::request-url
                    ::response-status ::response-size ::source-ip ::destination-ip])))
(s/fdef parse-access-log
  :args (s/cat ::access-log-lines (s/coll-of string?))
  :ret (s/coll-of ::access-log-record))
(defn parse-access-log [access-log-lines]
  (->> access-log-lines
       (mapv parse-access-log-line)
       (remove nil?)
       vec))

(defn- ends-with-filter [suffix]
  (reify FilenameFilter
    (accept [_this dir filename]
      (string/ends-with? filename suffix))))

(defn- list-log-files [dir]
  (-> (io/file dir)
      (.listFiles (ends-with-filter ".txt"))
      vec))

(defn parse-access-log-files
  "Parses all txt files in given directory an returns
  a map with filenames (without suffix) as keys and access logs content as values."
  [dir]
  (let [log-files (list-log-files dir)]
    (->> log-files
         (mapv (fn [access-log-file]
                 (let [file-name (.getName access-log-file)
                       file-name-without-suffix (subs file-name 0 (string/last-index-of file-name "."))
                       ;; empty file will make line-seq return nil
                       parsed-records (some-> access-log-file io/reader line-seq parse-access-log)]
                   [file-name-without-suffix
                    (or parsed-records [])])))
         (into (sorted-map)))))

(defn source-ips-per-project [parsed-log-records]
  (map (fn [[project-id project-log-records]]
         [project-id (->> project-log-records (map :source-ip) distinct)])
       parsed-log-records))

(defn read-messages-from-json-file
  "This is useful when you fetch logs via `aws logs` cli, e.g.
  `aws logs filter-log-events --log-group-name codescene-web-prod-webrequests --start-time 1548979200000 --end-time 1554076799000 --filter-pattern '\"/projects/2667\"' > 2019-02-01_2019-03-31_project-2667.log`"
  [file-path]
  (let [full-content (slurp file-path)
        parsed-json (json/parse-string full-content true)]
    (->> parsed-json
         :events
         (mapv :message))))

(defn rich-data [{:keys [timestamp request-url response-status source-ip]}]
  (format "%s,%s,%s,%s" timestamp source-ip response-status request-url ))

(comment

  (def parsed-logs (parse-access-log-files "access-logs"))

  (def source-ips (source-ips-per-project parsed-logs))
  

  (run! #(let [[project-id ips] %]
           (println project-id ":" (string/join ", " ips)))
        source-ips)


  ;;; fetching logs via AWS CLI tools is better for automation - can be used like this:
  ;; access log from 1.2.2019 to 31.3.2019
  ;; `aws logs filter-log-events --log-group-name xxx-webrequests --start-time 1548979200000 --end-time 1554076799000 --filter-pattern '"/projects/2667"' > 2019-02-01_2019-03-31.log`
  (def json-messages (read-messages-from-json-file "2019-02-01_2019-03-31.log"))
  (def parsed-json-messages (parse-access-log json-messages))
  (def rich-data-output (mapv rich-data parsed-json-messages))
  (run! println rich-data-output)
  (spit "2019-02-01_2019-03-31.csv" (string/join "\n" rich-data-output))
  (def source-ips-output (->> parsed-json-messages (mapv :source-ip) distinct))
  (run! println source-ips-output)
  
  (def messages-by-source-ip (group-by :source-ip parsed-json-messages))
  ;; check all but the first one which is very common
  (run! println (drop 1 messages-by-source-ip))



;; end comment
)

