(ns clojure-experiments.regex
  "Various experiments with regular expressions.
  See also https://clojure.org/reference/other_functions#regex")


;; re-find returns the first match within the string
(re-find #"abc" "abcdabc")
;; => "abc"

;; re-seq returns all the matches as a sequence
(re-seq #"abc" "abcdabc")
;; => ("abc" "abc")

;; with re-matches, the whole string must match the regex
(re-matches #"abc" "abcdabc")
;; => nil
(re-matches #"abc" "abc")
;; => "abc"


;;; Find the exact usage of a function name in a string by re-seq re-pattern - Clojure: https://stackoverflow.com/questions/76361929/find-the-exact-usage-of-a-function-name-in-a-string-by-re-seq-re-pattern-cloju

(count (re-seq (re-pattern "map") "a hello m world p"))
;; => 0

(count (re-seq (re-pattern "map") "hello world map"))
;; => 1

(re-find #"map" "hello world map")

(defn f
  "searchs the given value(inside !type atom) inside given text(vector-0f-texts)"
  [pattern text]
  ;; See https://clojure.org/reference/reader#_symbols for valid characters in Clojure symbols
  (count (re-seq (re-pattern (str "\\Q" pattern "\\E" "[^a-zA-Z0-9*+!\\-_'?]")) text)))

(def function-text 
  "(defn idx->meta [pair-col]
     (->> pair-col
          (apply hash-map)
          (reduce-kv (fn [acc k v]
                       (let [idx       k
                             str-vals  (filterv string? (vals v))
                             str-paths (->> str-vals
                                            (map #(clojure.string/split % #\"\")))]
                         (->> str-paths
                              (reduce (fn [acc str-path]
                                        (update-in acc str-path (fnil conj []) v))
                                      acc))))
                     {})
          )
     )")


(f "with-open" function-text)
;; => 0
(f "int" function-text)
;; => 0
(f "str" function-text)
