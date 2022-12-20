(ns clojure-experiments.stats.excel
  "Playing with Excel sheets in Clojure"
  (:require [clojure.pprint :refer [print-table pprint]]
            [bb-excel.core  :refer [get-sheet get-sheets]]))

(comment

  (def bucket-inventory
    (get-sheet "/Users/jumar/Work/CodeScene/CLOUD/S3/s3-inventory/beanstalk-bucket-inventory-2022-09-08.xlsx"
               "beanstalk-bucket-inventory-2022"
               {:hdr true :row 1}))

  (first bucket-inventory)
  ;; => {:_r 2, "Column1" "elasticbeanstalk-eu-west-1-504003789983", "Column2" "codescene-cloud-web/v2022-08-23T08:40:38_8d1c9d51f622b5e8d4ef2fcf8adec9092e5547d5.zip", "Size (Bytes)" 2.09399653E8, "Size (MB)" 199.69907093048096, "Date" 44796.44493055555}

  )

