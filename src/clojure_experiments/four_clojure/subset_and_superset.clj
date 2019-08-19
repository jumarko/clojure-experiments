(ns four-clojure.subset-and-superset)

;;; http://www.4clojure.com/problem/161

(clojure.set/superset? #{1 2} #{2})

(clojure.set/subset? #{1} #{1 2})

(clojure.set/superset? #{1 2} #{1 2})

(clojure.set/subset? #{1 2} #{1 2})
