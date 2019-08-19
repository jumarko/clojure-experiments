(ns four-clojure.122-read-binary-number
  "http://www.4clojure.com/problem/122
  Convert a binary number provided as a string to its numerical value.")

(defn binary->num [binary-n]
  (Integer/parseInt binary-n 2))

(defn binary->num-manual [binary-n]
  (reduce (fn [acc v] (+ (Integer/parseInt (str v)) (* 2 acc)))
          0
          binary-n))

(= 0     (binary->num "0"))

(= 7     (binary->num "111"))

(= 8     (binary->num "1000"))

(= 9     (binary->num "1001"))

(= 255   (binary->num "11111111"))

(= 1365  (binary->num "10101010101"))

(= 65535 (binary->num "1111111111111111"))

