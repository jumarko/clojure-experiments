(ns clojure-experiments.stats.covid19
  "This is my own version of https://dragan.rocks/articles/20/Corona-1-Baby-steps-with-Covid-19-for-programmers.
  Originally forked https://github.com/practicalli/data-science-corvid-19
  and did my custom version here: https://github.com/jumarko/data-science-corvid-19/blob/master/src/jumarko/journal.clj#L1  
  See https://github.com/MitchTalmadge/ASCII-Data for plotting.
  "
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv])
  (:import (com.mitchtalmadge.asciidata.graph ASCIIGraph)))





;; Note: the original data https://open-covid-19.github.io/data/v2/master.csv is no longer available
;; Check output/tables/ dir: https://github.com/open-covid-19/data/tree/master/output/tables
;; The new data are split into multiple files.
;; => better to use data from John Hopkins University directly- time series data
;;    https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series
;;    - which is more similar to the data used in the blog post
;;    - the difference is that data for particular days are in columns

;; (def data-url-template "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/%s-%s-%s.csv")
(def time-series-url-prefix "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_")
(def time-series-url-deaths (str time-series-url-prefix "deaths_global.csv"))
(def time-series-url-confirmed (str time-series-url-prefix "confirmed_global.csv"))
(def time-series-url-recovered (str time-series-url-prefix "recovered_global.csv"))


(defn read-world-data
  "Reads latest deaths, confirmed, and recovered time series csv data and returns them as a vector of 3 elements."
  []
  (let [fetch-csv (fn [url] (->> url slurp csv/read-csv))]
    [(fetch-csv time-series-url-deaths)
     (fetch-csv time-series-url-confirmed)
     (fetch-csv time-series-url-recovered)]))

(def world-data (read-world-data))


;; let's create some selectors
(defn header [world-data]
  ;; all the files have the same header
  (ffirst world-data))
(defn deaths [world-data]
  (rest (first world-data)))
(defn confirmed [world-data]
  (rest (second world-data )))
(defn recovered [world-data]
  (rest (nth world-data 2)))

;; check column names
(header world-data)
;; => ["Province/State" "Country/Region" "Lat" "Long" "1/22/20" "1/23/20" "1/24/20" "1/25/20" "1/26/20" "1/27/20" "1/28/20" "1/29/20" "1/30/20" "1/31/20" "2/1/20" "2/2/20" "2/3/20" "2/4/20" "2/5/20" "2/6/20" "2/7/20" "2/8/20" "2/9/20" "2/10/20" "2/11/20" "2/12/20" "2/13/20" "2/14/20" "2/15/20" "2/16/20" "2/17/20" "2/18/20" "2/19/20" "2/20/20" "2/21/20" "2/22/20" "2/23/20" "2/24/20" "2/25/20" "2/26/20" "2/27/20" "2/28/20" "2/29/20" "3/1/20" "3/2/20" "3/3/20" "3/4/20" "3/5/20" "3/6/20" "3/7/20" "3/8/20" "3/9/20" "3/10/20" "3/11/20" "3/12/20" "3/13/20" "3/14/20" "3/15/20" "3/16/20" "3/17/20" "3/18/20" "3/19/20" "3/20/20" "3/21/20" "3/22/20" "3/23/20" "3/24/20" "3/25/20" "3/26/20" "3/27/20" "3/28/20" "3/29/20" "3/30/20" "3/31/20" "4/1/20" "4/2/20" "4/3/20" "4/4/20" "4/5/20" "4/6/20" "4/7/20" "4/8/20" "4/9/20" "4/10/20" "4/11/20" "4/12/20" "4/13/20" "4/14/20" "4/15/20" "4/16/20" "4/17/20" "4/18/20" "4/19/20" "4/20/20" "4/21/20" "4/22/20" "4/23/20" "4/24/20" "4/25/20" "4/26/20" "4/27/20" "4/28/20" "4/29/20" "4/30/20" "5/1/20" "5/2/20" "5/3/20" "5/4/20" "5/5/20" "5/6/20" "5/7/20" "5/8/20" "5/9/20" "5/10/20" "5/11/20" "5/12/20" "5/13/20" "5/14/20" "5/15/20" "5/16/20" "5/17/20" "5/18/20" "5/19/20" "5/20/20" "5/21/20" "5/22/20" "5/23/20" "5/24/20" "5/25/20" "5/26/20" "5/27/20" "5/28/20" "5/29/20" "5/30/20" "5/31/20" "6/1/20" "6/2/20" "6/3/20" "6/4/20" "6/5/20" "6/6/20" "6/7/20" "6/8/20"]

;; check sample data
(first (deaths world-data))
;; => ["" "Afghanistan" "33.0" "65.0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "0" "1" "1" "1" "2" "4" "4" "4" "4" "4" "4" "4" "6" "6" "7" "7" "11" "14" "14" "15" "15" "18" "18" "21" "23" "25" "30" "30" "30" "33" "36" "36" "40" "42" "43" "47" "50" "57" "58" "60" "64" "68" "72" "85" "90" "95" "104" "106" "109" "115" "120" "122" "127" "132" "136" "153" "168" "169" "173" "178" "187" "193" "205" "216" "218" "219" "220" "227" "235" "246" "249" "257" "265" "270" "294" "300" "309" "327" "357" "369"]

;; How many data?
(count (deaths world-data))
;; => 266

;; But this isn't unique - so check only unique countries:
(->> (deaths world-data)
     (map second)
     distinct
     count)
;; => 188

;; count columns
(count (first (deaths world-data)))
;; => 143

;; how many observations with confirmed equal 0?
(defn days-observations [row]
  (mapv
   #(Long/parseLong %)
   (subvec row 4)))
(def all-the-days (mapcat days-observations (confirmed world-data)))
;; how many observations in total?
(count all-the-days)
;; => 36974
(count (filter zero? all-the-days))
;; => 10379


;;; now he explores data per country in the blog post
;;; this isn't that interesting for us since we have different data shape
;;; - data is available for all the countries for every days; it's just 0 sometimes


;;; selecting your country
(defn country-data
  "Returns data for all country regions. Can return 0 (for nonexistent country or country without data),
  1 (for most countries), or many rows (when data per region are available - e.g. US, Australia, ...)."
  [country data]
  (filter
   (fn [[_ country-name :as _row]]
     (= country country-name))
   data))

(defn country-observations [country data]
  (->> (country-data country data)
       (mapcat days-observations)
       ;; starting zeros aren't that interesting
       (drop-while zero?)))

(def czech-deaths (country-observations "Czechia" (deaths world-data)))
(def czech-confirmed (country-observations "Czechia" (confirmed world-data)))
(def czech-recovered (country-observations "Czechia" (recovered world-data)))

(def slovak-deaths (country-observations "Slovakia" (deaths world-data)))
(def slovak-confirmed (country-observations "Slovakia" (confirmed world-data)))
(def slovak-recovered (country-observations "Slovakia" (recovered world-data)))

;;; Plotting: https://github.com/MitchTalmadge/ASCII-Data

;; the line is pretty steep so let's use logarithmic scale to examine trends
;; - no need to use anything fancy like Neanderthal at this point:
(defn log ^double [^double x]
  ;; no need to use anything fancy like Neanderthal at this point:
  (Math/log x))

;; can be huge and slow!
(defn print-plot [data]
  (println (.plot (ASCIIGraph/fromSeries (double-array data)))))

(defn print-log-plot [data]
  (println (.plot (ASCIIGraph/fromSeries (double-array (map log data))))))


;; simple line graph is overwhelming - harder to see trends
(comment

  (print-plot czech-deaths)
  ;; this is way to big...
  (print-plot czech-confirmed)
  (print-plot czech-recovered)

  (print-plot slovak-deaths)
  ;;
  )


;; log plots
(comment
  (print-log-plot czech-deaths)
  (print-log-plot czech-confirmed)
  (print-log-plot czech-recovered)
  
  (print-log-plot slovak-deaths)
  (print-log-plot slovak-confirmed)
  (print-log-plot slovak-recovered)
  )


