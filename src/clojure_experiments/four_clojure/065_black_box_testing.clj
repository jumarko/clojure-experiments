(ns four-clojure.066-black-box-testing
  "http://www.4clojure.com/problem/65.
  Write a function which takes a colleciton and returns one of :map, :set, :list, or :vector
  describing the typeo of collection it was given.
  You won't be allowed to inspect their class or use the built-in predicates like list? -
  the point is to poke at them and understand their behavior.")



(defn inspect [coll]
  (let [empty-coll (empty coll)]
    (cond
      (= 1 (get (into empty-coll [[:a 1]])
                :a))
      :map

      (= 1 (count (into empty-coll [1 1])))
      :set

      (= 1 (first (into empty-coll [1 2 3])))
      :vector

      (= 3 (first (into empty-coll [1 2 3])))
      :list)))

(= :map (inspect {:a 1, :b 2}))

(= :list (inspect (range (rand-int 20))))

(= :vector (inspect [1 2 3 4 5 6]))

(= :set (inspect #{10 (rand-int 5)}))

(= [:map :set :vector :list] (map inspect [{} #{} [] ()]))
