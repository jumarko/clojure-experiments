;; DEPRECATED! See `clojure-experiments.stats.python.libpython` instead
(ns clojure-experiments.visualizations.libpython
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :as py :refer [py. py.. py.-]]
            [clojure.java.shell :as sh]))

;;; Gigasquid's examples: https://github.com/gigasquid/libpython-clj-examples
;;; - e.g. https://github.com/gigasquid/libpython-clj-examples/blob/master/src/gigasquid/plot.clj

;;; How to use plotly
;;; https://plotly.com/python/getting-started/
(comment
  
;;; ???
  (py/import-module "plotly.express")
  (require-python '[plotly.express :as plotly]))

