(ns clojure-experiments.stats.sk.komunalny-odpad
  (:require clojure.java.io
            [clojure.string :as str]))

(def data (line-seq (clojure.java.io/reader "/Users/jumar/Private/edu/DataScience/datasets/SK/STATdat/Množstvo komunálneho odpadu (v tonách) [zp3001rr].csv"
                                            :encoding "Windows-1250")))

(let [csv-lines (map (fn [line] (str/split line #";"))
                     data)
      [years-line & records-lines] csv-lines
      years (nthnext years-line 2)
      records (->> records-lines rest (ma(rest records-lines)p rest))
      records-map (into {}(rest records-lines)
                        (map (fn [[attribute & values-per-year]] [attribute values-per-year])
                             records))
      ]
  records-map)
