(ns clojure-experiments.purely-functional.puzzles.0362-query-params
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]))

;; https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-362-tip-double-recursion/

;; nested recursion
(defn print-double-nested
  "v is a doubly nested vector, like [[1 2 3] [4 5 6]]"
  [v]
  (loop [inner [] outer v]
    (cond
      (not (empty? inner))
      (let [[f & rst] inner]
        (println f)
        (recur rst outer))

      (not (empty? outer))
      (let [[f & rst] outer]
        (recur f rst))
      
      :else
      :done!)))

(print-double-nested [[1 2 3] [4 5 6]])
;; => :done!


;; parse-query-params
;; Notes:
;; - The query string is the string containing the query parameters.
;;   It appears after the ? and before a #. Ex: https://lispcast.com/search?q=clojure#page2
;; - The keys and values are URL Encoded. You can use java.net.URLDecoder/decode to decode them.
;; - The key-value pairs are separated by &. Ex: a=1&b=2
;; - Each key-value pair contains the key, followed by =, followed by the value. Ex: a=1
;; Bonus: Query parameters can contain duplicate keys. Handle them gracefully.
(defn- decode [val]
  (java.net.URLDecoder/decode val "UTF-8"))

(decode "q=%23%5E%21%26*")
;; => "q=#^!&*"

(defn- decode-param-string
  "Converts a string like 'a=b' to a vector ot two strings [a b]"
  [param-string]
  (let [name+val (str/split param-string #"=")]
    (mapv decode name+val)))

(defn query-params
  "Expects url as string and return a map with query string parameters."
  [url]
  (let [query-params-strings (some-> url
                             (str/split #"\?")
                             second
                             (str/split #"&"))
        params (into {} (map decode-param-string query-params-strings))]
    params))


(deftest test-query-params
  (testing "no params"
    (is (= {} (query-params "https://google.sk")))
    (is (= {} (query-params "https://google.sk?"))))
  (testing "simple params"
    (is (= {"a" "b", "z" "y"} (query-params "https://google.sk?a=b&z=y"))))
  (testing "params with special chars"
    (is (= {"q" "#^!&*", "source" "hp"} (query-params "https://google.sk?q=%23%5E%21%26*&source=hp"))))
  (testing "duplicate param"
    (is (= {"a" "c", "z" "y"} (query-params "https://google.sk?a=b&z=y&a=c"))))
)

