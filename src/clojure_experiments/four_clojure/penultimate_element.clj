(ns four-clojure.penultimate-element)

;;; http://www.4clojure.com/problem/20
;;; Write a function which returns the second to last element from a sequence

(defn second-to-last-elem [coll]
  (let [all-but-last (take (dec (count coll)) coll)]
    (last all-but-last)))


(= (second-to-last-elem (list 1 2 3 4 5)) 4)

(= (second-to-last-elem ["a" "b" "c"]) "b")

(= (second-to-last-elem [[1 2] [3 4]]) [1 2])

;; much simpler solution is:
(= ( #(last (drop-last %)) [1 2 3 4 5])
   4)
