(ns clojure-repl-experiments.experiments
  "Single namespace for all my REPL experiments.
  This might be split up later if I find it useful."
  (:require [seesaw.core :as s]))

;;; Seesaw tutorial: https://gist.github.com/1441520
;;; check also https://github.com/daveray/seesaw
(comment
  (def f (s/frame :title "Get to know Seesaw"))
  (-> f s/pack! s/show!)
  )



