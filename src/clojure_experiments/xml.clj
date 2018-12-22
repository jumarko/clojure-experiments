(ns clojure-experiments.xml
  (:require [clojure.data.xml :as xml]))


(-> (slurp "https://www.w3schools.com/xml/note.xml")
    (xml/parse-str)
    meta
    ::xml/location-info)
