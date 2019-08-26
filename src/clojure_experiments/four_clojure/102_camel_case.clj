(ns clojure-experiments.four-clojure.102-camel-case
  "http://www.4clojure.com/problem/102.
  Write a function that converts hyphen-separated strings to camelCase strings;
  e.g.'keys-like-this' into 'keysLikeThis'."
  (:require
   [clojure.string]
   [clojure.test :refer [deftest is testing]]))

(defn camel-case
  [s]
  (let [[first-word & next-words] (clojure.string/split s #"-")
        capitalized (map clojure.string/capitalize next-words)]
    (apply str first-word capitalized)))

(deftest camel-case-test
  (testing "single world isn't capitalized"
    (is (= "something"
           (camel-case "something"))))
  (testing "multi-word key from problem definition"
    (is (= "multiWordKey"
           (camel-case "multi-word-key"))))
  (testing "already capitalized string doesn't change"
    (is (= "leaveMeAlone"
           (camel-case "leaveMeAlone")))))
