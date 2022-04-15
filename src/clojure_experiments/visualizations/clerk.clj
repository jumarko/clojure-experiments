(ns clojure-experiments.visualizations.clerk
  "https://github.com/nextjournal/clerk"
  ;; Note: you may need to manually evaluate `hiccup.util` and `hiccup.page` namespaces
  ;; to make this require work.
  (:require [nextjournal.clerk :as clerk]
            [clojure.string :as str]))


;;; Clerk demo: https://github.com/nextjournal/clerk#-using-clerk
;;; see notebooks: https://github.com/nextjournal/clerk/blob/main/notebooks
;;; - these notebooks have been copied to src/clojure_experiments/visualizations/clerk/notebooks/
(comment
  ;; start Clerk's buit-in webserver on the default port 7777,
  ;; opening the browser when done
  (clerk/serve! {:browse? true})

  ;; either call `clerk/show!` explicitly
  ;; see the notebook: https://github.com/nextjournal/clerk/blob/main/notebooks/rule_30.clj
  (clerk/show! "src/clojure_experiments/visualizations/clerk/notebooks/rule_30.clj")

  ;; or let Clerk watch the given `:paths` for changes
  #_(clerk/serve! {:watch-paths ["notebooks" "src"]})
  (clerk/serve! {:watch-paths ["src/clojure_experiments/visualizations/clerk/notebooks/"]})

  ;; start with watcher and show filter function to enable notebook pinning
  (clerk/serve! {:watch-paths ["notebooks" "src"]
                 :show-filter-fn #(str/starts-with? % "notebooks")})

  ,)
