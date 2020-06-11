(ns clojure-experiments.purely-functional.puzzles.0380-medication-contraindications
  "Read online: https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-380-what-can-you-iterate-through/.
  Solutions: https://gist.github.com/ericnormand/04b22244e6ab9502326a0516eb3bdfa8"
  (:require [clojure.test :refer [are deftest testing]]))

;;; Some medications shouldn't be taken in combination.
;;; Your task is to take the list of medications a patient has and a list of contraindication pairs,
;;; and determine what pairs of medications (if any) they are prescribed that don’t mix well.


(def patient-medications [{:name "Blythenal"
                           :rxNorm "blyth"}
                          {:name "Masbutol"
                           :rxNorm "masbut"}
                          {:name "Welbutril"
                           :rxNorm "welb"}])

(def contraindication-pairs [["nr913ng" "blyth"]
                             ["masbut"  "87f2h139049f"]
                             ["nr913ng" "j1j81f0"]
                             ["blyth" "welb"]
                             ["masbut"  "welb"]])

;; Note: Eric's solution is the same as mine: https://gist.github.com/ericnormand/04b22244e6ab9502326a0516eb3bdfa8#file-eric-normand-clj
(defn contraindications
  "Checks if there are any medications in `meds` that shouldn't be taken in combination.
  Returns such 'contraindication pairs' or nil if there are none (all is fine)."
  [meds pairs]
  (let [norms (set (map :rxNorm meds))]
    (->> pairs
         (filter (fn [[x y]] (and (norms x) (norms y))))
         not-empty)))

