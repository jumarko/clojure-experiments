(ns clojure-experiments.state-machines
  "Inspired by Clojurians discussion and statecharts library: https://lucywang000.github.io/clj-statecharts/docs/get-started/"
  (:require [statecharts.core :as fsm]))

;;;; Getting started: https://lucywang000.github.io/clj-statecharts/docs/get-started/

;;; The Immutable API
;; define the machine
(def machine
  (fsm/machine
   {:id      :lights
    :initial :red
    :context nil
    :states
    {:green  {:on
              {:timer {:target :yellow
                       :actions (fn [& _]
                                  (println "transitioned to :yellow!"))
                       }}}
     :yellow {:on
              {:timer :red}}
     :red    {:on
              {:timer :green}}}

    :on {:power-outage :red}
    }))

;; initialize the machine
;; - Returns the initial state of the machine.
;; - It also executes all the entry actions of the initial states, if any.
(def s1 (fsm/initialize machine)) ; {:_state :red}

;; Returns the next state based the current state & event.
;; It also executes all the entry/exit/transition actions.
(def s2 (fsm/transition machine s1 {:type :timer})) ; {:_state :green}
(def s3 (fsm/transition machine s2 {:type :timer})) ; {:_state :yellow}


;;; The Service API
