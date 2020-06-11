(ns clojure-experiments.rebl
  "REBL experiments.
  See Stuart Halloway's Clojure/conj 2018 talk:

  REBL resources:
  - https://github.com/cognitect-labs/REBL-distro
  - https://github.com/cognitect-labs/REBL-distro/wiki/Hotkeys
  - http://rebl.cognitect.com/download.html
  "
  ;; UPDATE: no longer added as dependency
  (:require [cognitect.rebl :as rebl]))

;; Let's add tab to inspect values in REBL ui
(add-tap #(rebl/inspect %))

(defn interesting-data-gen []
  (->> (range 100)
       (map-indexed (fn [idx val] (let [x (* idx val)]
                                    #_(tap> x)
                                    x)))
       (tap> )
       ))

#_(interesting-data-gen)

(comment

  ;; can launch UI from the REPL
  (rebl/ui)

  ;; that doesn't connect to anything inside cider -> you need to inspect manually
  (rebl/inspect (+ 10 20))

  ;; you could use `-main` but that just launches another repl and keeps CIDER asking for input
  (rebl/-main)

  ;; unclear what's the `repl` function for...
  (rebl/repl)
  

  )
