(ns advent-of-clojure.2018.04-closet
  "https://adventofcode.com/2018/day/4"
  (:require
   [clojure.test :refer [deftest testing is]]
   [advent-of-clojure.2018.input :as io])
  (:import
   (java.time LocalDateTime)
   (java.time.format DateTimeFormatter DateTimeParseException)))


;;;; Supply closet - prototype suit manufacturing lab
;;;; Guards protecting the close changing shifts
;;;; always sleep somewhen between 00:00 and 00:59
;;;; Find the guards most likely to be asleep at a specific time


(def test-shifts-data
  [{:time (LocalDateTime/parse "1518-11-01T23:58")
    :guard-id 99
    :action :begins-shift}
   {:time (LocalDateTime/parse "1518-11-02T00:40")
    :action :falls-asleep}
   {:time (LocalDateTime/parse "1518-11-02T00:50")
    :action :wakes-up}

   {:time (LocalDateTime/parse "1518-11-03T00:05")
    :guard-id 10
    :action :begins-shift}
   {:time (LocalDateTime/parse "1518-11-03T00:24")
    :action :falls-asleep}
   {:time (LocalDateTime/parse "1518-11-03T00:29")
    :action :wakes-up}

   {:time (LocalDateTime/parse "1518-11-04T00:02")
    :guard-id 99
    :action :begins-shift}
   {:time (LocalDateTime/parse "1518-11-04T00:36")
    :action :falls-asleep}
   {:time (LocalDateTime/parse "1518-11-04T00:46")
    :action :wakes-up}

   {:time (LocalDateTime/parse "1518-11-05T00:03")
    :guard-id 99
    :action :begins-shift}
   {:time (LocalDateTime/parse "1518-11-05T00:45")
    :action :falls-asleep}
   {:time (LocalDateTime/parse "1518-11-05T00:55")
    :action :wakes-up}

   ;; note that this is chronologically the first day but put it here to test proper sorting
   {:time (LocalDateTime/parse "1518-11-01T00:00")
    :guard-id 10
    :action :begins-shift}
   {:time (LocalDateTime/parse "1518-11-01T00:30")
    :action :falls-asleep}
   {:time (LocalDateTime/parse "1518-11-01T00:55")
    :action :wakes-up}
   {:time (LocalDateTime/parse "1518-11-01T00:05")
    :action :falls-asleep}
   {:time (LocalDateTime/parse "1518-11-01T00:25")
    :action :wakes-up}

   ])

;;; Strategy/Puzzle 1: find ID of the guard that has the most minutes asleep
;;;   and multiply that by the minute that guard spent asleep the most.
;;; Note: the real input isn't sorted! https://adventofcode.com/2018/day/4/input


(def actions
  {"begins shift" :begins-shift
   "wakes up" :wakes-up
   "falls asleep" :falls-asleep})

(def shift-pattern #"\[([\d- :]+)\] (?:Guard #(\d+) )?(begins shift|wakes up|falls asleep)")

;; Check https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
(def datetime-format (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm"))

(defn- str->date-time
  "Converts a string date-time representation to the instance of `LocalDateTime`
  or nil if the format isn't valid"
  [date-time-str]
  (try
    (LocalDateTime/parse date-time-str datetime-format)
    (catch DateTimeParseException e
      ;; ignore and return nil
)))
      

(str->date-time "1518-11-02 00:00")

(defn- parse-shift
  "Parses single shift line which can be of following types:
    [1518-11-02 00:00] Guard #433 begins shift
    [1518-11-05 00:20] wakes up
    [1518-11-03 00:10] falls asleep
  "
  [shift-line]
  (when-let [[_ time guard-id action] (re-matches shift-pattern shift-line)]
    (let [proper-action (get actions action)
          proper-time (str->date-time time)]
      (when (and proper-action proper-time)
        (cond-> {:time (str->date-time time)
                 :action proper-action}
          guard-id (assoc :guard-id (Integer/parseInt guard-id)))))))

(parse-shift
 "[1518-11-02 00:00] Guard #433 begins shift")

;; inspired by `partition-by`
(defn- partition-by-shift
  "Groups shifts events (assuming sorted) by the guard shift to which they belong.
  Also adds the proper guard-id to each shift event (:wakes-up, :falls-asleep)."
  [shifts]
  (lazy-seq
   (when-let [shifts-seq (seq shifts)]
     (let [{:keys [guard-id] :as shift-begins} (first shifts-seq)
           one-shift-events (cons shift-begins
                                  ;; take all subsequent events without guard-id
                                  ;; and add guard-id to them
                                  (->> (next shifts-seq)
                                       (take-while #(not (:guard-id %)))
                                       (map #(assoc % :guard-id guard-id))))]
       (cons one-shift-events
             (partition-by-shift
              (lazy-seq (drop (count one-shift-events)
                              shifts-seq))))))))

(defn- sort-shifts-and-fill-guard-ids
  "Sorts shifts chronologically
  and adds :guard-id to all events (:wakes-up, :falls-asleep)."
  [shifts]
  (let [sorted-shifts (sort-by :time shifts)]
    (->> sorted-shifts
         partition-by-shift
         (apply concat))))

(defn- shifts-by-guard-id
  "Sort them, fill guard-id in all events and group them by guard id."
  [shifts]
  (let [ready-shifts (sort-shifts-and-fill-guard-ids shifts)]
    (->> ready-shifts
         (group-by :guard-id))))

(defn- update-sleeping-minutes [acc prev-time curr-time]
  (let [prev-minutes (.getMinute prev-time)
        curr-minutes (.getMinute curr-time)]
    (reduce
     (fn [a m]
       (update a m (fnil inc 0)))
     acc
     (range prev-minutes curr-minutes))))

(defn- guard-sleeping-minutes
  "Given shifts events sorted and belonging to a single guard,
  output a map where key is a minute (0-59) and val is how many times
  the guard has slept during that minute."
  [guard-shifts]
  (first (reduce
          (fn [[acc prev-time] {:keys [action time] :as _shift}]
            (if (and prev-time (= action :wakes-up))
              [(update-sleeping-minutes acc prev-time time) time]
              [acc time])
            )
          [{} nil]
          guard-shifts)))

(guard-sleeping-minutes [{:time (LocalDateTime/parse "1518-11-01T00:00"),
                     :guard-id 10,
                     :action :begins-shift}
                    {:time (LocalDateTime/parse "1518-11-01T00:05"),
                     :action :falls-asleep,
                     :guard-id 10}
                    {:time (LocalDateTime/parse "1518-11-01T00:25"),
                     :action :wakes-up,
                     :guard-id 10},
                    {:time (LocalDateTime/parse "1518-11-01T00:30"),
                     :action :falls-asleep,
                     :guard-id 10},
                    {:time (LocalDateTime/parse "1518-11-01T00:55"),
                     :action :wakes-up,
                     :guard-id 10},
                    {:time (LocalDateTime/parse "1518-11-03T00:05"),
                     :guard-id 10,
                     :action :begins-shift},
                    {:time (LocalDateTime/parse "1518-11-03T00:24"),
                     :action :falls-asleep,
                     :guard-id 10},
                    {:time (LocalDateTime/parse "1518-11-03T00:29"),
                     :action :wakes-up,
                     :guard-id 10}])
;; => {7 1, 20 1, 27 1, 24 2, 39 1, 46 1, 54 1, 15 1, 48 1, 50 1, 21 1, 31 1, 32 1, 40 1, 33 1, 13 1,
;;     22 1, 36 1, 41 1, 43 1, 44 1, 6 1, 28 1, 51 1, 25 1, 34 1, 17 1, 12 1, 23 1, 47 1, 35 1, 19 1,
;;     11 1, 9 1, 5 1, 14 1, 45 1, 53 1, 26 1, 16 1, 38 1, 30 1, 10 1, 18 1, 52 1, 42 1, 37 1, 8 1, 49 1}

(defn sleeping-minutes
  "Counts sleeping minutes for each guard in the shifts.
  Outputs a map where keys are guard ids and values are maps with concrete minute as a key
  and number of times the guard has slept during that minute as value."
  [shifts]
  (->> shifts
       shifts-by-guard-id
       (map (fn [[guard-id guard-shifts]]
              [guard-id (guard-sleeping-minutes guard-shifts)]))
       (into {})))

(defn- guard-sleeping-time
  "Computes total number of minutes that the guard has slept.
  Expects a map where a key is a concrete minute and a value is the number of times the guard has slept
  during that minute"
  [guard-sleeping-minutes]
  (reduce-kv
   (fn [acc _minute cnt]
     (+ acc cnt))
   0
   guard-sleeping-minutes))

(guard-sleeping-time {39 1, 46 2, 54 1, 48 2, 50 1, 40 2, 36 1, 41 2, 43 2, 44 2, 51 1, 47 2, 45 3})
;; => 22

(defn- most-frequent-sleeping-minute
  "Expects the same structure as guard-sleeping-time and returns a single minute
  for which the guard has slept the most times."
  [sleeping-minutes]
  (when-not (empty? sleeping-minutes)
    (first (apply max-key val sleeping-minutes))))

(most-frequent-sleeping-minute {39 1, 46 2, 54 1, 48 2, 50 1, 40 2, 36 1, 41 2, 43 2, 44 2, 51 1, 47 2, 45 3})
;; => 45

(defn most-sleeping-guard [shifts]
  (let [guards-with-sleeping-minutes (sleeping-minutes shifts)

        [guard-id minutes :as _most-sleeping-guard]
        (apply max-key
               (fn [[guard-id mins]] (guard-sleeping-time mins))
               guards-with-sleeping-minutes)]

    [guard-id (most-frequent-sleeping-minute minutes)]))

(most-sleeping-guard test-shifts-data)
;; => [10 24]

(defn puzzle1
  "Finds the ID of the guard sleepting the most minutes
  and multiply that by the minute he's been sleeping the most times."
  []
  (let [[guard-id most-frequent-minute] (io/with-input "04_input.txt" most-sleeping-guard parse-shift)]
    (* guard-id most-frequent-minute)))

(deftest puzzle1-test
  (testing "real data"
    (is (= 35623 (puzzle1)))))

(defn guard-most-sleeping-in-the-same-minute [shifts]
  (let [guards-with-sleeping-minutes (sleeping-minutes shifts)
        [guard-id minutes]
        (apply max-key
               (fn [[guard-id mins]]
                 (if-let [fm (most-frequent-sleeping-minute mins)]
                   ;; get the actual frequency
                   (get mins fm)
                   ;; if guard haven't slept at all
                   -1))
               guards-with-sleeping-minutes)]
    [guard-id (most-frequent-sleeping-minute minutes)]))

(guard-most-sleeping-in-the-same-minute test-shifts-data)
;; => [99 45]

(defn puzzle2
  "Finds the ID of the guard sleeping most in the same minute."
  []
  (let [[guard-id most-frequent-minute] (io/with-input "04_input.txt" guard-most-sleeping-in-the-same-minute parse-shift)]
    (* guard-id most-frequent-minute)))

(deftest puzzle2-test
  (testing "real data"
    (is (= 35623 (puzzle2)))))

;;; tests for auxiliary functions
(deftest test-most-sleeping-guard-minute
  (testing "Returns proper product of most sleeping guard's ID x the minute he's been sleeping the most."
    (is (= [10 24] (most-sleeping-guard test-shifts-data)))))

(deftest parse-shift-test
  (testing "begins shift"
    (is (= {:time (LocalDateTime/parse "1518-11-02T00:00")
            :guard-id 433
            :action :begins-shift}
           (parse-shift
            "[1518-11-02 00:00] Guard #433 begins shift"))))
  ;; notice that for following two cases we don't have guard-id yet
  ;; => we must sort all records first and filled them later
  (testing "wakes up"
    (is (= {:time (LocalDateTime/parse "1518-11-02T00:40")
            :action :wakes-up}
           (parse-shift
            "[1518-11-02 00:40] wakes up"))))
  (testing "falls asleep"
    (is (= {:time (LocalDateTime/parse "1518-11-02T00:10")
            :action :falls-asleep}
           (parse-shift
            "[1518-11-02 00:10] falls asleep"))))
  (testing "invalid action"
    (is (nil? (parse-shift "[1518-11-02 00:20] yawning"))))
  (testing "invalid date format"
    (is (nil? (parse-shift "[1518-11 00:00] falls asleep")))))
