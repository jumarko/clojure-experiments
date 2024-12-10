(ns clojure-experiments.logging-and-monitoring.osquery
  "Query your devices like a database.
  Motivated by https://yogthos.net/posts/2022-11-26-nREPL-system-interaction.html
  See https://www.osquery.io/ and https://www.osquery.io/schema.
  "
  (:require [clojure.java.shell :refer [sh]]
            [cheshire.core :as json]))

(defn osquery [query]
  (let [{:keys [exit out err]} (sh "osqueryi" "--json" query)]
    (if (zero? exit)
      (json/decode out true)
      (throw (Exception. err)))))

(comment

  (osquery "select * from routes where destination = '::1'")
;; => ({:hopcount "0", :interface "lo0", :mtu "16384", :type "local", :source "", :gateway "::1", :netmask "128", :flags "2098181", :destination "::1", :metric "0"})


  .)

