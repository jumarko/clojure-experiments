(ns four-clojure.drop-every-nth-item)

;;; http://www.4clojure.com/problem/41
;;; Write a function which drops every nth item from a sequence

(defn drop-nth-item [coll index]
  (let [nth-items-to-nil (map-indexed (fn [idx element]
                                        (when (not= 0 (mod (inc idx) index))
                                          element))
                                      coll)]
    (remove nil? nth-items-to-nil)))

;; simpler solution using keep-indexed
(defn drop-nth-item [coll index]
  (keep-indexed (fn [idx element]
                  (when (not= 0 (mod (inc idx) index))
                    element))
                coll))

;; most concise solution using partition-all

(defn drop-nth-item [coll index]
  (apply concat
         (partition-all (dec index) index coll)))

(= (drop-nth-item [1 2 3 4 5 6 7 8] 3) [1 2 4 5 7 8])

(= (drop-nth-item [:a :b :c :d :e :f] 2) [:a :c :e])

(= (drop-nth-item [1 2 3 4 5 6] 4) [1 2 3 5 6])

;; my custom check for repetitive elements
(= (drop-nth-item [1 2 1 1 3 4 4 5 5 6] 4) [1 2 1 3 4 4 5 6])
