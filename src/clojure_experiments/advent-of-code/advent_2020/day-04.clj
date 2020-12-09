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
  (->> (str/split input #"\n\n")
       (mapv parse-passport)))

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

(defn count-valid-passports [input]
  (->> (parse-input input)
       (filter valid-passport?)
       count))

(count-valid-passports sample-input)
;; => 2
(count-valid-passports test-input)
;; => 182


;;; Part 2 - stricter validation
;; byr (Birth Year) - four digits; at least 1920 and at most 2002.
;; iyr (Issue Year) - four digits; at least 2010 and at most 2020.
;; eyr (Expiration Year) - four digits; at least 2020 and at most 2030.
;; hgt (Height) - a number followed by either cm or in:
;;   If cm, the number must be at least 150 and at most 193.
;;   If in, the number must be at least 59 and at most 76.
;; hcl (Hair Color) - a # followed by exactly six characters 0-9 or a-f.
;; ecl (Eye Color) - exactly one of: amb blu brn gry grn hzl oth.
;; pid (Passport ID) - a nine-digit number, including leading zeroes.
;; cid (Country ID) - ignored, missing or not.

(defn- str-to-int [maybe-int]
  (try (Integer/parseInt maybe-int)
       (catch Exception _)))

(defn- str-to-height [hgt]
  (let [[_ height unit] (re-find #"(\d+)(cm|in)" hgt)]
    [(str-to-int height) unit]))
(defn- valid-height? [hgt]
  (let [[height unit] (str-to-height hgt)]
    (or (and (= "cm" unit) (<= 150 height 193))
        (and (= "in" unit) (<= 59 height 76)))))
(valid-height? "183m")
;; => false
(valid-height? "193cm")
;; => true
(valid-height? "194cm")
;; => false

(defn valid-passport? [{:strs [byr iyr eyr hgt hcl ecl pid] :as passport}]
  (and (set/superset? passport required-fields)
       (<= 1920 (str-to-int byr) 2002)
       (<= 2010 (str-to-int iyr) 2020)
       (<= 2020 (str-to-int eyr) 2030)
       (valid-height? hgt)
       (re-matches #"#[0-9a-f]{6,6}" hcl)
       (#{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"} ecl)
       (re-matches #"[0-9]{9,9}" pid)))

(def sample-invalid-passports
 (parse-input
  "eyr:1972 cid:100
hcl:#18171d ecl:amb hgt:170 pid:186cm iyr:2018 byr:1926

iyr:2019
hcl:#602927 eyr:1967 hgt:170cm
ecl:grn pid:012533040 byr:1946

hcl:dab227 iyr:2012
ecl:brn hgt:182cm pid:021572410 eyr:2020 byr:1992 cid:277

hgt:59cm ecl:zzz
eyr:2038 hcl:74454a iyr:2023
pid:3556412378 byr:2007"))

(def sample-valid-passports
  (parse-input
   "pid:087499704 hgt:74in ecl:grn iyr:2012 eyr:2030 byr:1980
hcl:#623a2f

eyr:2029 ecl:blu cid:129 byr:1989
iyr:2014 pid:896056539 hcl:#a97842 hgt:165cm

hcl:#888785
hgt:164cm byr:2001 iyr:2015 cid:88
pid:545766238 ecl:hzl
eyr:2022

iyr:2010 hgt:158cm hcl:#b6652a ecl:blu byr:1944 eyr:2021 pid:093154719"))

(deftest valid-passports-test2
  (testing "invalid-passports"
    (is (not-any? valid-passport? sample-invalid-passports)))
  (testing "valid-passports"
    (is (every? valid-passport? sample-valid-passports))))

(count-valid-passports test-input)
;; => 109
