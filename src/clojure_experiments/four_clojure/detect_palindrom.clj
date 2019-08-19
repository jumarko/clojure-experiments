(ns four-clojure.detect-palindrom)

;;; Palindrom detector: http://www.4clojure.com/problem/27
;;; Write a function which returns trui if the given sequence is a palindrome.

(defn is-palindrom? [s]
  (= (seq s) (reverse s)))

(false? (is-palindrom? '(1 2 3 4 5)))
(true? (is-palindrom? "racecar"))
(true? (is-palindrom? [:foo :bar :foo]))
(true? (is-palindrom? '(1 1 3 3 1 1)))
(false? (is-palindrom? '(:a :b :c)))
