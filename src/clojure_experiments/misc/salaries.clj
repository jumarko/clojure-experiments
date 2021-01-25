(ns clojure-experiments.misc.salaries)

(def salaries
  {:leader 480
   :member 456
   :student 106})

(def total 1078)
(def total-without-student (- total (:student salaries)))
;; 972

(def possible-work-loads
  {:leader #{0.7 0.8 0.9 1.0} ; leaving out 0.6 since that's what we already have
   :member #{0.3 0.4 0.5 0.6 0.7} ; restricting a bit
   :all-members #{1.0 1.1 1.2 1.3 1.4 1.5}})

(defn acceptable-difference?
  ([desired-total current-total]
   (acceptable-difference? desired-total current-total 5))
  ([desired-total current-total max-diff]
   (< (- desired-total max-diff)
      current-total
      (+ desired-total max-diff))))

(defn acceptable-results
  [max-total-salary-diff]
  (for [leader-load (:leader possible-work-loads)
        a-load (:member possible-work-loads)
        b-load (:member possible-work-loads)
        c-load (:member possible-work-loads)
        :let [leader-salary (* leader-load (:leader salaries))
              a-salary (* a-load (:member salaries))
              b-salary (* b-load (:member salaries))
              c-salary (* c-load (:member salaries))
              total-salary (+ leader-salary a-salary b-salary c-salary)]
        :when (and (contains? (:all-members possible-work-loads)
                              (+ a-load b-load c-load))
                   (acceptable-difference? total-without-student total-salary max-total-salary-diff))]
    {:total-salary total-salary
     :leader {:load leader-load :salary leader-salary}
     :member-a {:load a-load :salary a-salary}
     :member-b {:load b-load :salary b-salary}
     :member-c {:load c-load :salary c-salary}}))

;; closest we can get - 974.4K total
(def sorted-results-3 (sort-by :total-salary (acceptable-results 3)))
;; some more alternatives
(def sorted-results-5 (sort-by :total-salary (acceptable-results 5)))
(def sorted-results-10  (sort-by :total-salary (acceptable-results 10)))
;; empty
(def sorted-results-2 (sort-by :total-salary (acceptable-results 2)))

(defn round [d n]
  (format (str "%." n "f") d))
(round 10.123 2)
;; => "10.12"

(defn map->vec [{:keys [load salary]}]
  [load (round salary 2)])

(defn short-summary [results]
  (map
   (fn [{:keys [total-salary leader member-a member-b member-c]}]
     [(round total-salary 2)
      (map->vec leader)
      (map->vec member-a)
      (map->vec member-b)
      (map->vec member-c)])
   results))

(defn unique-results [results]
  (->> results
       short-summary
       (map #(concat (subvec % 0 2)
                     ;; sort just the members to get rid of their equivalent permutations
                     (sort-by first (subvec % 2))))
       set
       (sort-by first)))

(comment

  (set (map #(-> % :leader :load) sorted-results-3))
  ;; => only 0.7 possible for +-3K difference

  (set (map #(-> % :leader :load) sorted-results-5))
  ;; => only 0.7 and 0.8 possible loads for +- 5K difference

  (set (map #(-> % :leader :load) sorted-results-10))
  ;; => 0.7, 0.8, 0.9 and 1.0 possible laods for +- 10K difference


  (unique-results sorted-results-3)
;; => (("974.40" [0.7 "336.00"] [0.3 "136.80"] [0.5 "228.00"] [0.6 "273.60"])
;;     ("974.40" [0.7 "336.00"] [0.3 "136.80"] [0.4 "182.40"] [0.7 "319.20"])
;;     ("974.40" [0.7 "336.00"] [0.4 "182.40"] [0.4 "182.40"] [0.6 "273.60"])
;;     ("974.40" [0.7 "336.00"] [0.4 "182.40"] [0.5 "228.00"] [0.5 "228.00"]))


  (unique-results sorted-results-5)
;; => (("974.40" [0.7 "336.00"] [0.3 "136.80"] [0.5 "228.00"] [0.6 "273.60"])
;;     ("974.40" [0.7 "336.00"] [0.3 "136.80"] [0.4 "182.40"] [0.7 "319.20"])
;;     ("974.40" [0.7 "336.00"] [0.4 "182.40"] [0.4 "182.40"] [0.6 "273.60"])
;;     ("974.40" [0.7 "336.00"] [0.4 "182.40"] [0.5 "228.00"] [0.5 "228.00"])
;;     ("976.80" [0.8 "384.00"] [0.4 "182.40"] [0.4 "182.40"] [0.5 "228.00"])
;;     ("976.80" [0.8 "384.00"] [0.3 "136.80"] [0.5 "228.00"] [0.5 "228.00"])
;;     ("976.80" [0.8 "384.00"] [0.3 "136.80"] [0.3 "136.80"] [0.7 "319.20"])
;;     ("976.80" [0.8 "384.00"] [0.3 "136.80"] [0.4 "182.40"] [0.6 "273.60"]))

  (unique-results sorted-results-10)
  974.40        0.7 336.00        0.3 136.80        0.5 228.00        0.6 273.60
  974.40        0.7 336.00        0.3 136.80        0.4 182.40        0.7 319.20
  974.40        0.7 336.00        0.4 182.40        0.4 182.40        0.6 273.60
  974.40        0.7 336.00        0.4 182.40        0.5 228.00        0.5 228.00
  976.80        0.8 384.00        0.4 182.40        0.4 182.40        0.5 228.00
  976.80        0.8 384.00        0.3 136.80        0.5 228.00        0.5 228.00
  976.80        0.8 384.00        0.3 136.80        0.3 136.80        0.7 319.20
  976.80        0.8 384.00        0.3 136.80        0.4 182.40        0.6 273.60
  979.20        0.9 432.00        0.3 136.80        0.3 136.80        0.6 273.60
  979.20        0.9 432.00        0.3 136.80        0.4 182.40        0.5 228.00
  981.60        1.0 480.00        0.3 136.80        0.3 136.80        0.5 228.00
  981.60        1.0 480.00        0.3 136.80        0.4 182.40        0.4 182.40


  ;;
  )
