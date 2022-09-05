(ns clojure-experiments.portal
  "https://github.com/djblue/portal"
  (:require [portal.api :as p]))




;; https://github.com/djblue/portal
(comment

  ;; This opens a standalone chrome app window
  ;; You can inspect it (right click -> Inspect) to see the URL, etc.
  ;; - we also open this window in `user` ns
  (def portal (p/open)) ; by saving it in a var we can use it from the REPL https://github.com/djblue/portal#portal-atom
  ;; you can use the Electron version - run /Applications/portal.app first!
  (def portal (p/open {:launcher :electron}))

  (add-tap #'p/submit)

  ;; explore data
  (tap> :hello)
  (tap> [1 2 3])
  (tap> {:x 0 :y 0})

  ;; list of maps can be rendered as tables
  (tap> [{:color :blue} {:color :red}])

  ;; you can event use cider-inspect to inspect @portal !
  (prn @portal)

  ;; you can clear the inspector any time
  (p/clear)

  ;; shutdown
  (remove-tap #'p/submit)
  (p/close)


  ;; you can register your own commands too!
  (p/register! #'reverse)
  ,)

;;; visual-tools-experiments: https://clojureverse.org/t/visual-tools-meeting-2-summary-video/8674
;;; -> https://github.com/scicloj/visual-tools-experiments/tree/main/portal-nrepl



;;; [LA] Meetup: Collaborative Learning - Portal
;;; https://www.youtube.com/watch?v=kID0zo3VoCo
(comment

  (tap> (all-ns))

  ;; timestamps - don't have a nice viewer (yet?)
  (tap> (java.time.Instant/ofEpochMilli (System/currentTimeMillis)))

  ;; if you tap vars, then goto-definition command in portal works
  (tap> #'p/submit)

,)


;;; Visualize graphs with portal: https://github.com/djblue/portal/blob/4abccfc4db7f6bf2d9cd76d7b9ebdca3105f138a/src/examples/data.cljc#L113
(def bar-chart
  ^{:portal.viewer/default :portal.viewer/vega-lite}
  {:data
   {:values
    [{:a "A", :b 28}
     {:a "B", :b 55}
     {:a "C", :b 43}
     {:a "D", :b 91}
     {:a "E", :b 81}
     {:a "F", :b 53}]}
   :mark "bar"
   :encoding
   {:x
    {:field "a"
     :type "nominal"
     :axis {:labelAngle 0}}
    :y {:field "b", :type "quantitative"}}})
(tap> bar-chart)

