(ns clojure-experiments.stats.techml
  "tech.ml.dataset and tablecloth related experiments.
  See also `clojure-experiments.csv`.
  https://github.com/techascent/tech.ml.dataset#mini-walkthrough
  https://github.com/scicloj/tablecloth
  "
  (:require [tech.v3.dataset :as ds]
            ;; requires the `--illegal-access=permit` workaround on JDK 16: https://github.com/clojure-goes-fast/clj-memory-meter/issues/8
            [clj-memory-meter.core :as mm]
            [tablecloth.api :as tc]
            [tech.v3.datatype.datetime :as tdt]
            [tech.v3.datatype.functional :as tf]))

;;; https://github.com/techascent/tech.ml.dataset#mini-walkthrough
;;; => see `clojure-experiments.stats.techml` for more
(comment

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

  ;; summary stats for every column
  (ds/brief csv-data)
;; => ({:min #object[java.time.LocalDate 0x5dea00f6 "2000-01-01"], :n-missing 0, :col-name "date", :mean #object[java.time.LocalDate 0x72c2d322 "2005-05-12"], :datatype :packed-local-date, :skew -0.13894433927538719, :standard-deviation 9.250270466130954E10, :quartile-3 #object[java.time.LocalDate 0x4450db36 "2007-11-23"], :n-valid 560, :quartile-1 #object[java.time.LocalDate 0x1406f4a3 "2002-11-08"], :median #object[java.time.LocalDate 0x19e6ab65 "2005-08-01"], :max #object[java.time.LocalDate 0x7d5930b8 "2010-03-01"], :first #object[java.time.LocalDate 0x3f92cd4e "2000-01-01"], :last #object[java.time.LocalDate 0x1c1295f3 "2010-03-01"]} {:min 5.97, :n-missing 0, :col-name "price", :mean 100.73428571428572, :datatype :float64, :skew 2.413094643061917, :standard-deviation 132.55477114107094, :quartile-3 100.88, :n-valid 560, :quartile-1 24.169999999999998, :median 57.27, :max 707.0, :first 39.81, :last 223.02} {:n-missing 0, :col-name "symbol", :histogram (["MSFT" 123] ["AMZN" 123] ["IBM" 123] ["AAPL" 123] ["GOOG" 68]), :datatype :string, :mode "MSFT", :n-valid 560, :values ["MSFT" "AMZN" "IBM" "AAPL" "GOOG"], :first "MSFT", :last "AAPL", :n-values 5})

  ;; see also `clojure-experiments.stats.descriptive`
  (ds/descriptive-stats csv-data)
;; => https://github.com/techascent/tech.ml.dataset/raw/master/test/data/stocks.csv: descriptive-stats [3 12]:
;;    | :col-name |          :datatype | :n-valid | :n-missing |       :min |      :mean | :mode |       :max | :standard-deviation |       :skew |     :first |      :last |
;;    |-----------|--------------------|---------:|-----------:|------------|------------|-------|------------|--------------------:|------------:|------------|------------|
;;    |      date | :packed-local-date |      560 |          0 | 2000-01-01 | 2005-05-12 |       | 2010-03-01 |      9.25027047E+10 | -0.13894434 | 2000-01-01 | 2010-03-01 |
;;    |     price |           :float64 |      560 |          0 |      5.970 |      100.7 |       |      707.0 |      1.32554771E+02 |  2.41309464 |      39.81 |      223.0 |
;;    |    symbol |            :string |      560 |          0 |            |            |  MSFT |            |                     |             |       MSFT |       AAPL |



  ,)


;;; tablecloth
(comment

                                        ; https://github.com/scicloj/tablecloth#usage-example
  (-> "https://raw.githubusercontent.com/techascent/tech.ml.dataset/master/test/data/stocks.csv"
      (tc/dataset {:key-fn keyword})
      (tc/group-by (fn [row]
                      {:symbol (:symbol row)
                       :year (tdt/long-temporal-field :years (:date row))}))
      (tc/aggregate #(tf/mean (% :price)))
      (tc/order-by [:symbol :year])
      (tc/head 10))
;; => _unnamed [10 3]:
;;    |     :summary | :year | :symbol |
;;    |-------------:|------:|---------|
;;    |  21.74833333 |  2000 |    AAPL |
;;    |  10.17583333 |  2001 |    AAPL |
;;    |   9.40833333 |  2002 |    AAPL |
;;    |   9.34750000 |  2003 |    AAPL |
;;    |  18.72333333 |  2004 |    AAPL |
;;    |  48.17166667 |  2005 |    AAPL |
;;    |  72.04333333 |  2006 |    AAPL |
;;    | 133.35333333 |  2007 |    AAPL |
;;    | 138.48083333 |  2008 |    AAPL |
;;    | 150.39333333 |  2009 |    AAPL |
  ,)

