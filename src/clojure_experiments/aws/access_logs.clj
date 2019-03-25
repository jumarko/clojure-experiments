(ns clojure-experiments.aws.access-logs
  "Utilities to process Elastic Beanstalk (nginx) access logs (aka 'webrequests')"
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.spec.alpha :as s])
  (:import java.io.FilenameFilter))

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

(comment

  (def parsed-logs (parse-access-log-files "access-logs"))

  (def source-ips (source-ips-per-project parsed-logs))
  

  (run! #(let [[project-id ips] %]
           (println project-id ":" (string/join ", " ips)))
        source-ips)


;; end comment
)

