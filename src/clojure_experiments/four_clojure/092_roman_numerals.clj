(ns four-clojure.092-roman-numerals
  "http://www.4clojure.com/problem/92.
  Parse a Roman-numeral string and return the number it represents.
  You can assume that the input will be well-formed, in upper-case,
  and follow the subtractive principle: https://en.wikipedia.org/wiki/Roman_numerals#Subtractive_principle
  You don't need to handle any numbers greater than MMMCMXCIX (3999),
  the largest number representable with ordinary letters."
  (:require [clojure.test :refer [deftest testing is]]))

(defn roman-numerals [str-num]
  (let [romans {\I 1
                \V 5
                \X 10
                \L 50
                \C 100
                \D 500
                \M 1000}
        numbers (->> str-num
                     (clojure.string/upper-case)
                     (map romans))]
    (loop [total 0
           nums numbers]
      (let [[fst snd] nums]
        (cond
          (and fst snd)
          (if (< fst snd)
            ;; this is subtraction pattern
            (recur (+ total (- snd fst)) (nnext nums))
            ;; add only the first one because the second one can form subtraction pattern with the third element
            (recur (+ total fst) (next nums)))

          fst
          (+ total fst)

          :else
          total)))))

;; adereth's solution:
(defn roman-numerals [s]
  (->> s
       reverse
       (replace (zipmap "MDCLXVI" [1000 500 100 50 10 5 1]))
       (partition-by identity)
       (map (partial apply +))
       (reduce #((if (< %1 %2) + -) %1 %2))))

(deftest simple-without-subtraction
  (testing "none"
    (is (= 0 (roman-numerals ""))))
  (testing "one"
    (is (= 1 (roman-numerals "I"))))
  (testing "two"
    (is (= 2 (roman-numerals "II"))))
  (testing "three"
    (is (= 3 (roman-numerals "III"))))
  (testing "five"
    (is (= 5 (roman-numerals "V"))))
  (testing "six"
    (is (= 6 (roman-numerals "VI"))))
  (testing "seven"
    (is (= 7 (roman-numerals "VII"))))
  (testing "eight"
    (is (= 8 (roman-numerals "VIII"))))
  (testing "longer"
    (is (= 1888 (roman-numerals "MDCCCLXXXVIII")))))

(deftest subtraction
  (testing "four"
    (is (= 4 (roman-numerals "IV"))))
  (testing "nine"
    (is (= 9 (roman-numerals "IX"))))
  (testing "longer"
    (is (= 999 (roman-numerals "CMXCIX")))))


(deftest official-tests
  (testing "simple with subtraction"
    (is (= 14 (roman-numerals "XIV"))))
  (testing "longer but no subtraction"
    (is (= 827 (roman-numerals "DCCCXXVII"))))
  (testing "Long and subtraction"
    (is (= 3999 (roman-numerals "MMMCMXCIX"))))
  (testing "Subtraction from L"
    (is (= 48 (roman-numerals "XLVIII")))))

