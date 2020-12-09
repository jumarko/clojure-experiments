(ns clojure-experiments.advent-of-code.advent-2020.day-04
  "https://adventofcode.com/2020/day/4
  Input: https://adventofcode.com/2020/day/4/input"
  (:require [clojure-experiments.advent-of-code.advent-2020.utils :refer [read-input]]
            [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [clojure.set :as set]))


(def sample-input
  "ecl:gry pid:860033327 eyr:2020 hcl:#fffffd
byr:1937 iyr:2017 cid:147 hgt:183cm

iyr:2013 ecl:amb cid:350 eyr:2023 pid:028048884
hcl:#cfa07d byr:1929

hcl:#ae17e1 iyr:2013
eyr:2024
ecl:brn pid:760753108 byr:1931
hgt:179cm

hcl:#cfa07d eyr:2025 pid:166559648
iyr:2011 ecl:brn hgt:59in")

(def test-input (str/join "\n" (read-input 4 identity)))

(defn- parse-passport [passport-str]
  (->> (str/split passport-str #"[\n ]")
       (map #(str/split % #":"))
       (into {})))

(defn parse-input [input]
  (->> (str/split input #"\n\n" )
       (mapv parse-passport)
       ))


(def sample-passwords (parse-input sample-input))
;; => [{"ecl" "gry",
;;      "pid" "860033327",
;;      "eyr" "2020",
;;      "hcl" "#fffffd",
;;      "byr" "1937",
;;      "iyr" "2017",
;;      "cid" "147",
;;      "hgt" "183cm"}
;; ...

(def required-fields #{"ecl" "pid" "eyr" "hcl" "byr" "iyr" "hgt"})
(defn valid-passport? [passport]
  (set/superset? passport required-fields))

(deftest valid-passports-test
  (testing ""
    (is (= [true false true false]
           (mapv valid-passport? sample-passwords)))))

(defn count-valid-passwords [input]
  (->> (parse-input input)
       (filter valid-passport?)
       count))

(count-valid-passwords sample-input)
;; => 2
(count-valid-passwords test-input)
;; => 182
