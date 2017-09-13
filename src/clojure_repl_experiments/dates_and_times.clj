(ns clojure-repl-experiments.dates-and-times
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

;;; Round to the nearest hour
;;; Inspired by https://stackoverflow.com/questions/46178419/clojure-get-hour-only-from-time-string
;;; and https://stackoverflow.com/questions/25552023/round-minutes-to-ceiling-using-java-8
(def date-format (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))

(defn- round-to-nearest-hour [date-time-string]
  (let [date-time (LocalDateTime/parse date-time-string date-format)]
    (if (>= 30 (.getMinute date-time))
      date-time
      (.plusHours date-time 1))))

(.getHour (round-to-nearest-hour "2017-08-30 09:01:48"))
(.getHour (round-to-nearest-hour "2017-08-30 09:31:48"))

