(ns four-clojure.contain-yourself)

;;; Contain Yourself problem: http://www.4clojure.com/problem/47

(contains? #{4 5 6} 4)

(contains? [1 1 1 1 1] 4)

(contains? {4 :a 2 :b} 4)

(not (contains? [1 2 4] 4))
