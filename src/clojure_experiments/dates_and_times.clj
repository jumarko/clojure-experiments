(ns clojure-experiments.dates-and-times
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


(def dtf (DateTimeFormatter/ofPattern "yyyy-MM-dd"))
(def now (LocalDateTime/now))
;; borkdude asked why he needs type hints in this code but actually no type hints (^LocalDateTime) are needed
(def today (.format now dtf))
(def tomorrow
  (-> now
      (.plusDays 1)
      (.format dtf)))


;; https://stackoverflow.com/questions/54958706/clj-time-coerce-java-sql-date-tostring-decrements-date/54969691#54969691
(comment

  (require '[clj-time.coerce :as tc])
  (java.util.TimeZone/setDefault(java.util.TimeZone/getTimeZone "GMT"))
  (.toString (tc/to-sql-date (tc/from-string "2018-09-28")))
  ;; => "2018-09-28"

  (java.util.TimeZone/setDefault(java.util.TimeZone/getTimeZone "GMT-1"))
  (.toString (tc/to-sql-date (tc/from-string "2018-09-28")))
  ;; => "2018-09-27"

  )

