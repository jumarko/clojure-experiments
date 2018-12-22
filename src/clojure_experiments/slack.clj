(ns clojure-experiments.slack
  (:require [clj-slack.channels :as channels]
            [clj-slack.users :as users]))

;; Note: you can get the quick token by going to the slack web UI, open Chrome DevTools,
;; click "Elements", find "xoxs-"
(def clojurians {:api-url "https://slack.com/api" :token "XXX"})

(defn clojure-channel []
  (->> (channels/list clojurians)
       :channels
       (filter #(= "clojure" (:name %)))
       (take 1)
       (map (juxt :id :name))
       ))


#_(clj-slack.channels/history clojurians "C03S1KBA2")
