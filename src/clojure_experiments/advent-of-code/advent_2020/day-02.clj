(ns clojure-experiments.advent-of-code.advent-2020.day-02
  "https://adventofcode.com/2020/day/2
  Input: https://adventofcode.com/2020/day/2/input"
  (:require [clojure-experiments.advent-of-code.2020.utils :refer [read-input]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]))

(def sample-input
  "1-3 a: abcde
1-3 b: cdefg
2-9 c: ccccccccc")

(defn- parse-input [line]
  (let [[m min max letter password] (re-find #"(\d+)-(\d+) (\w): (\w+)"
                                             line)]
    (when m
      [(Integer/parseInt min) (Integer/parseInt max) (first letter) password])))
(parse-input "1-3 a: abcde")
;; => [1 3 \a "abcde"]

(def test-input (read-input 2 parse-input))


(defn- valid-password? [[min max letter password]]
  (let [freqs (frequencies password)]
    (<= min
        (get freqs letter 0)
        max)))

(s/def ::passwords (s/tuple int? int? char? string?))
(s/fdef check-passwords
  :args (s/or :arity-1 (s/cat :passwords ::passwords)
              :arity-2 (s/cat :valid-fn fn?
                              :paswwords ::passwords)))
(defn check-passwords
  ([passwords] (check-passwords valid-password? passwords))
  ([valid-fn passwords]
   (->> passwords
        (filter valid-fn)
        count)))

(check-passwords test-input)
;; => 393

(defn- valid-password-2? [[pos1 pos2 letter password]]
  (let [x1 (get password (dec pos1))
        x2 (get password (dec pos2))]
    (and (not (= letter x1 x2))
         (or (= x1 letter)
             (= x2 letter)))))

;; or alternative: https://youtu.be/rghUu4z5MdM?t=924
(defn- valid-password-2? [[pos1 pos2 letter password]]
  (let [ok1 (= letter (get password (dec pos1)))
        ok2 (= letter (get password (dec pos2)))]
    (not= ok1 ok2)))

(time (check-passwords valid-password-2? test-input))
;; "Elapsed time: 1.227568 msecs"
;; => 690

(deftest password-policy-test
  (testing "check-passwords"
    (is (= 2
           (check-passwords (mapv parse-input (str/split sample-input #"\n"))))))
  (testing "check-passwords-2"
    (is (= 1
           (check-passwords valid-password-2?
                            (mapv parse-input (str/split sample-input #"\n"))))))
  )


