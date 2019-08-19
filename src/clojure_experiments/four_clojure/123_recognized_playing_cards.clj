(ns four-clojure.123-recognized-playing-cards
  "http://www.4clojure.com/problem/128.
  Write a function which converts the string 'SJ' into a map {:suit :spade :rank 9}.")

;;; See also http://www.4clojure.com/problem/solutions/128

(def ^:private suite-mapping
  {\S :spade
   \H :heart
   \D :diamond
   \C :club})

(def ^:private rank-mapping
  {\2 0
   \3 1
   \4 2
   \5 3
   \6 4
   \7 5
   \8 6
   \9 7
   \T 8
   \J 9
   \Q 10
   \K 11
   \A 12})

(defn to-card
  "Converts shorthand 'SJ' into full map representation."
  [card-str]
  (when (not= 2 (count card-str))
    (throw (ex-info (str "Invalid length: " (count card-str) " Expected 2 chars")
                    {:card-str card-str
                     :length (count card-str)})))
  (let [[suite-char rank-char] card-str
        suit (suite-mapping suite-char)
        rank (rank-mapping rank-char)]
    (when-not (and suit rank)
      (throw (ex-info (str "Invalid rank or suit: " [suit rank])
                      {:card-str card-str
                       :suit suit
                       :rank rank})))
    {:suit suit
     :rank rank}))

;; should fail
#_(to-card "D10")
#_(to-card "D1")

(= {:suit :diamond :rank 10} (to-card "DQ"))

(= {:suit :heart :rank 3} (to-card "H5"))

(= {:suit :club :rank 12} (to-card "CA"))

(= (range 13) (map (comp :rank to-card str)
                   '[S2 S3 S4 S5 S6 S7
                     S8 S9 ST SJ SQ SK SA]))

;;; For 4clojure it must be in following format

(fn to-card
  [card-str]
  (when (not= 2 (count card-str))
    (throw (ex-info (str "Invalid length: " (count card-str) " Expected 2 chars")
                    {:card-str card-str
                     :length (count card-str)})))
  (let [suite-mapping {\S :spade \H :heart \D :diamond \C :club}
        rank-mapping {\2 0 \3 1 \4 2 \5 3 \6 4 \7 5 \8 6 \9 7 \T 8 \J 9 \Q 10 \K 11 \A 12}
        ;; alternative rank-mapping
        rank-mapping (zipmap [\2 \3 \4 \5 \6 \7 \8 \9 \T \J \Q \D \K \A] (range))
        [suite-char rank-char] card-str
        suit (suite-mapping suite-char)
        rank (rank-mapping rank-char)]
    (when-not (and suit rank)
      (throw (ex-info (str "Invalid rank or suit: " [suit rank])
                      {:card-str card-str
                       :suit suit
                       :rank rank})))
    {:suit suit
     :rank rank}))
