(ns clojure-experiments.purely-functional.property-based-testing.mergesort2
  )

;; TODO: this is wrong
;; => Fix it and compare performance with `mergesort.clj`
;; are the problems caused by destructuring??
(defn- merge [v1 v2]
  (loop [acc []
         [f1 & rst1 :as new-v1] v1
         [f2 & rst2 :as new-v2] v2]
    (let [{:keys [done? new-acc new-v1 new-v2]}
          (cond
            (empty? new-v1) {:new-acc (apply conj acc new-v2) :done? true}
            (empty? new-v2) {:new-acc (apply conj acc new-v1) :done? true}
            (<= f1 f2) {:new-acc (conj acc f1) :new-v1 rst1 :new-v2 new-v2}
            :else {:new-acc (conj acc f2) :new-v1 new-v1 :new-v2 rst2})]
      (if done?
        new-acc
        (recur new-acc new-v1 new-v2)))))

(defn- split-point [v]
  (quot (count v) 2))

(defn- mergesort* [v]
  (if (< (count v) 2)
    v
    (let [split (split-point v)]
      (merge (mergesort* (subvec v 0 split))
             (mergesort* (subvec v split))))))
  

(defn mergesort [xs]
  (seq (mergesort* (vec xs))))


(mergesort [])
(mergesort [2 1])
(mergesort (reverse (range 20)))
;; => (0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19)

(mergesort (shuffle (range 20)))



