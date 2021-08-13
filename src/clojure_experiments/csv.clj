(ns clojure-experiments.csv
  "see `clojure-experiments.stats.techml` for more tech.ml.dataset related experiments"
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            ;; requires the `--illegal-access=permit` workaround on JDK 16: https://github.com/clojure-goes-fast/clj-memory-meter/issues/8
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
  ;; UPDATE: doesn't work with JDK16 out of the box
  (mm/measure (vec csv-ds))
  ;; => "27.8 MB" (JDK 11! - 13.8.2021)
  ;; => "13.3 MB" (JDK 16! - 13.8.2021)
  ;; => "23.1 MB" (JDK 11? - a long time ago)
  (mm/measure (vec (csv-data->maps csv-ds)))
  ;; => "36.9 MB" (JDK 11! - 13.8.2021)
  ;; => "22.5 MB" (JDK16! - 13.8.2021)
  ;; => "31.8 MB" (JDK 11? - a long time ago)

  ;; can take a while to load
  (require '[tech.v3.dataset :as ds])

  (def ds (ds/->dataset "https://open-covid-19.github.io/data/v2/latest/master.csv"))
  (mm/measure ds)
  ;; => "6.8 MB" (JDK 11 - 13.8.2021)
  ;; => "6.8 MB" (JDK 16! - 13.8.2021)
  ;; => "5.1 MB"  (JDK 11? - a long time ago)

  ;; "clone" makes it more memory efficient
  ;; (suggested by Chris Nuernberger)
  ;; see also https://github.com/techascent/tech.ml.dataset/blob/master/topics/quick-reference.md#forcing-lazy-evaluation
  (mm/measure (tech.v3.datatype/clone ds))
;; => "3.7 MB"

  ;; dataset is logically a sequence of columens when treated like a sequence:
  (first ds)
;; => #tech.ml.dataset.column<string>[5001]
;;    key
;;    [AD, AE, AF, AF_BAL, AF_BAM, AF_BDG, AF_BDS, AF_BGL, AF_DAY, AF_FRA, AF_FYB, AF_GHA, AF_GHO, AF_HEL, AF_HER, AF_JOW, AF_KAB, AF_KAN, AF_KAP, AF_KDZ, ...]
  ;;


  )

;;; newer version of tech.ml.dataset
;;; https://github.com/techascent/tech.ml.dataset#mini-walkthrough
;;; => see `clojure-experiments.stats.techml` for more
(comment

  ;; can take a while to load
  (require '[tech.v3.dataset :as ds])

  (def csv-data (ds/->dataset "https://github.com/techascent/tech.ml.dataset/raw/master/test/data/stocks.csv"))
  (ds/head csv-data)
;; => https://github.com/techascent/tech.ml.dataset/raw/master/test/data/stocks.csv [5 3]:
;;    | symbol |       date | price |
;;    |--------|------------|------:|
;;    |   MSFT | 2000-01-01 | 39.81 |
;;    |   MSFT | 2000-02-01 | 36.35 |
;;    |   MSFT | 2000-03-01 | 43.22 |
;;    |   MSFT | 2000-04-01 | 28.37 |
;;    |   MSFT | 2000-05-01 | 25.45 |


  (def airports (ds/->dataset "https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat"
                             {:header-row? false :file-type :csv}))

  (ds/head airports)

  ,)
