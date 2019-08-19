(ns four-clojure.nil-key)

(defn nil-key-fn [key m]
  (and
       (contains? m key)
       (nil? (m key))))

(nil-key-fn :a {:a nil :b 1})
(nil-key-fn :a {:a 2 :b 1})
(nil-key-fn :a {:b 1 :c 3})


;; Another interesting solution using default value
(defn nil-key-fn2 [key m]
  (nil? (m key 0)))

(nil-key-fn2 :a {:a nil :b 1})
(nil-key-fn2 :a {:a 2 :b 1})
(nil-key-fn2 :a {:b 1 :c 3})
