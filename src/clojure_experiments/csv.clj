(ns clojure-experiments.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


;;; Using CSV to quickly visualize data can be very handy
;;; and an easy way how to explore, observe trends and find errors


(with-open [writer (io/writer "out-file.csv")]
  (csv/write-csv writer
                 [(map name [:a :c])
                  ["abc" "def"]
                  ["ghi" "jkl"]]))
