(ns clojure-experiments.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]

            [clj-memory-meter.core :as mm]))

;;; See also
;;; - semantic.csv: https://github.com/jumarko/semantic-csv, http://metasoarous.com/blog/presenting-semantic-csv
;;; - tech.ml.dataset: https://github.com/techascent/tech.ml.dataset
;;;   -> Walkthrough: https://github.com/techascent/tech.ml.dataset/blob/master/docs/walkthrough.md
;;; - For a one-stop data exploration pathway that should work well for you: https://github.com/cnuernber/simpledata/

;;; Using CSV to quickly visualize data can be very handy
;;; and an easy way how to explore, observe trends and find errors
(with-open [writer (io/writer "out-file.csv")]
  (csv/write-csv writer
                 [(map name [:a :c])
                  ["abc" "def"]
                  ["ghi" "jkl"]]))

;; Parsing into maps: https://github.com/clojure/data.csv/#parsing-into-maps
(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

;;; - tech.ml.dataset: https://github.com/techascent/tech.ml.dataset
;;;
;;; chrisn:
;;;; - tech.ml.dataset can load that file I believe.  It is far more efficient with memory. in general.
;;;  - For a one-stop data exploration pathway that should work well for you: https://github.com/cnuernber/simpledata/

(comment

  (require '[clj-memory-meter.core :as mm])

  (def csv-ds (csv/read-csv (slurp "https://open-covid-19.github.io/data/v2/latest/master.csv")))
  ;; don't be fooled by lazy seqs when measuring memory -> use vector
  (mm/measure (vec csv-ds))
  ;; => "23.1 MB"
  (mm/measure (vec (csv-data->maps csv-ds)))
  ;; => "31.8 MB"

  (require '[tech.ml.dataset :as ds])
  (def ds (ds/->dataset "https://open-covid-19.github.io/data/v2/latest/master.csv"))
  (mm/measure ds)
  ;; => "5.1 MB"  ;;

  ;; dataset is logically a sequence of columens when treated like a sequence:
  (first ds)
;; => #tech.ml.dataset.column<string>[5001]
;;    key
;;    [AD, AE, AF, AF_BAL, AF_BAM, AF_BDG, AF_BDS, AF_BGL, AF_DAY, AF_FRA, AF_FYB, AF_GHA, AF_GHO, AF_HEL, AF_HER, AF_JOW, AF_KAB, AF_KAN, AF_KAP, AF_KDZ, ...]
  ;;
  )
