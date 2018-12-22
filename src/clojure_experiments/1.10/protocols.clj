(ns clojure-experiments.1.10.protocols)

(defprotocol component
  (start [c])
  (stop [c]))

(def c (with-meta {:state :init} 
         {`start (fn [c] (assoc c :state :started))
          `stop (fn [c] (assoc c :state :stopped))}))

#_(start c)

(defprotocol component
  :extend-via-metadata true
  (start [c])
  (stop [c]))

#_(start c)
