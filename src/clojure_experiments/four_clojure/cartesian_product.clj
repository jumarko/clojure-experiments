(ns four-clojure.cartesian-product)

;;; http://www.4clojure.com/problem/90
;;; Write a function which calculates a Cartesian product of two sets.

(defn cartesian-product [s1 s2]
  (set (for [x1 s1
             x2 s2]
         [x1 x2])))

(= (cartesian-product #{"ace" "king" "queen"} #{"♠" "♥" "♦" "♣"})
   #{["ace"   "♠"] ["ace"   "♥"] ["ace"   "♦"] ["ace"   "♣"]
     ["king"  "♠"] ["king"  "♥"] ["king"  "♦"] ["king"  "♣"]
     ["queen" "♠"] ["queen" "♥"] ["queen" "♦"] ["queen" "♣"]})

(= (cartesian-product #{1 2 3} #{4 5})
   #{[1 4] [2 4] [3 4] [1 5] [2 5] [3 5]})

(= 300 (count (cartesian-product (into #{} (range 10))
                  (into #{} (range 30)))))
