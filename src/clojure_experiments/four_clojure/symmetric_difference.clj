(ns four-clojure.symmetric-difference)

;;; http://www.4clojure.com/problem/88
;;; Write a function which returns a symmetric difference of two sets.
;;; Symmetric difference is the set of items belonging to one but not both of two sets.

(defn symmetric-difference [s1 s2]
  (set ( remove #(and (s1 %) (s2 %))
                (concat s1 s2))))

(= (symmetric-difference #{1 2 3 4 5 6} #{1 3 5 7})
   #{2 4 6 7})

(= (symmetric-difference #{:a :b :c} #{})
   #{:a :b :c})

(= (symmetric-difference #{} #{4 5 6})
   #{4 5 6})

(= (symmetric-difference #{[1 2] [2 3]} #{[2 3] [3 4]})
   #{[1 2] [3 4]})
