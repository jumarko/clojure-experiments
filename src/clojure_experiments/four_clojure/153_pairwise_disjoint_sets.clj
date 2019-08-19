(ns four-clojure.153-pairwise-disjoint-sets
  "http://www.4clojure.com/problem/153
  given a sets of sets return true if no two sets have any elements in common and false otherwise."
  (:require [clojure.set :as set]))

(defn pairwise-disjoint-sets?
  [set-of-sets]
  (apply distinct? (apply concat set-of-sets))
  ;; perhaps more performant but unfinished and more "clever"
  #_(reduce
   (fn [_ s]
       (if (some false? (map #(set/intersection s %)
                             (disj set-of-sets s)))
         (reduced false)
         true))
   set-of-sets)
  #_(->> set-of-sets
       (map (fn [s]
              (map (partial set/intersection s)
                   (disj set-of-sets s))))
       flatten
       (every? empty?)))

#(apply distinct? (apply concat %))
  

(= (pairwise-disjoint-sets? #{#{\U}
                              #{\s}
                              #{\e \R \E}
                              #{\P \L}
                              #{\.}})
   true)

(= (pairwise-disjoint-sets? #{#{:a :b :c :d :e}
                              #{:a :b :c :d}
                              #{:a :b :c}
                              #{:a :b}
                              #{:a}})
   false)

(= (pairwise-disjoint-sets? #{#{[1 2 3] [4 5]}
                              #{[1 2] [3 4 5]}
                              #{[1] [2] 3 4 5}
                              #{1 2 [3 4] [5]}})
   true)

(= (pairwise-disjoint-sets? #{#{'a 'b}
                              #{'c 'd 'e}
                              #{'f 'g 'h 'i}
                              #{''a ''c ''f}})
   true)

(= (pairwise-disjoint-sets? #{#{'(:x :y :z) '(:x :y) '(:z) '()}
                              #{#{:x :y :z} #{:x :y} #{:z} #{}}
                              #{'[:x :y :z] [:x :y] [:z] [] {}}})
   false)

(= (pairwise-disjoint-sets? #{#{(= "true") false}
                              #{:yes :no}
                              #{(class 1) 0}
                              #{(symbol "true") 'false}
                              #{(keyword "yes") ::no}
                              #{(class '1) (int \0)}})
   false)

(= (pairwise-disjoint-sets? #{#{distinct?}
                              #{#(-> %) #(-> %)}
                              #{#(-> %) #(-> %) #(-> %)}
                              #{#(-> %) #(-> %) #(-> %)}})
   true)

(= (pairwise-disjoint-sets? #{#{(#(-> *)) + (quote mapcat) #_nil}
                              #{'+ '* mapcat (comment mapcat)}
                              #{(do) set contains? nil?}
                              #{#_empty?}})
   false)

