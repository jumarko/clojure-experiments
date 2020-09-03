(ns clojure-experiments.purely-functional.puzzles.393-filename-sort
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-393-always-start-with-a-repl/"
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]])
  (:import (java.text Normalizer
                      Normalizer$Form)))

;;; Write a better sorting function that you can pass to `sort-by`
;;; that will:
;;; - be case insensitive ('a' and 'A' are the same)
;;; - sort numbers properly (not like strings, but numerical order) -> '12' is after '2'
;;; - bonus: put accented characters next to each other (e.g. 'e' and 'é')


(defn filename-order [filename]
  (-> filename
      ;; case insensitive
      (str/lower-case)

      ;; proper number ordering -> prefix all numbers with zero
      (str/replace #"(\d+)" "0$1")

      ;; accent insensitive
      ;; https://stackoverflow.com/questions/51731574/removing-accents-and-diacritics-in-kotlin
      ;; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/Normalizer.Form.html
      (Normalizer/normalize Normalizer$Form/NFD)))

(deftest test-filename-order
  (testing "Case insensitivity"
    (is (= ["academy.zip" "Zoo.txt"]
           (sort-by filename-order ["Zoo.txt" "academy.zip"]))))
  (testing "Numbers sorting"
    (is (= ["academy.zip" "Zoo.txt"]
           (sort-by filename-order ["12 Final chapter.txt" "2 Second chapter.txt"]))))
  (testing "Accent insensitivity"
    (is (= ["elena" "Elena" "Élena" "Fero"]
           (sort-by filename-order ["elena", "Elena", "Fero", "Élena"])))))
