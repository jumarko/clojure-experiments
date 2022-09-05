(ns user
  (:require
   [flow-storm.api :as fs-api]
   [portal.api :as p]))

;; Start FlowStorm debugger in user.clj instead of from the REPL or clj buffer later
;; This fixes the problem with being unable to switch to FlowStorm window via Cmd+Tab
;; See https://clojurians.slack.com/archives/C03KZ3XT0CF/p1658370905532999?thread_ts=1658305276.475879&cid=C03KZ3XT0CF
(println "Starting FlowStorm debugger on thread: " (.getName (Thread/currentThread)))
(fs-api/local-connect)

(def portal (p/open))
(add-tap #'p/submit)


