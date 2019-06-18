(ns clojure-experiments.scripting.joker
  "Quick/Fast clojure scripts using joker.
  See:
  - https://joker-lang.org/
  - https://github.com/candid82/joker
  - Joker Standard Namespaces: https://candid82.github.io/joker/
  ")

(defn call-github-api [url token]
  (for [release (-> (joker.http/send {:url (str "https://api.github.com" url)
                                      :headers {"Authorization" (str "token " token)}})
                    :body
                    (joker.json/read-string))
        :let [assets (get release "assets")]]
    {:name (get release "tag_name")
     :mac (-> (filter #(re-find #"mac" (get % "name")) assets)
              first
              (get "download_count"))}))


(map inc)
