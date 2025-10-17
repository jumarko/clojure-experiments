(ns clojure-experiments.books.clojure-brain-teasers.12-reader-discard
  "Shows how reader's macro `#_` works - discarding expressions.
  See also:
  - https://clojure.org/reference/reader
  - https://www.expressionsofchange.org/dont-say-homoiconic/")

(def my-msgs
  {:emails [[:from "boss"] [:from "mom"]
            #_ #_ [:from "Nigerian Prince"] [:from "LinkedIn"]]
   :discord-msgs {"Clojure Camp" 6
                  #_ #_ "Heart of Clojure" 3
                  "DungeonMasters" 20}
   :voicemails ["Your voicemail box is full."]})
(defn unread [msgs]
  (let [{:keys [emails discord-msgs voicemails]} msgs]
    (+ (count emails)
       (reduce + (vals discord-msgs))
       (count voicemails))))

(unread my-msgs)
;; => 29
