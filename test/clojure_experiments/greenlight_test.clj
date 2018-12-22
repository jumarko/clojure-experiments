(ns clojure-experiments.greenlight-test
  "Demonstration of greenlight library for integration testing:
  https://github.com/amperity/greenlight"
  (:require  [clojure.test :refer [is]]
             [greenlight.test :as test :refer [deftest ]]
             [greenlight.step :as step :refer [defstep]]
             [greenlight.runner :as runner]))

;;; Greenlight tests are built from a collection of steps

(defstep math-test
  "A simple test step"
  :title "Simple Math Test"
  :test (fn [_] (is (= 3 (+ 1 2)))))

(deftest simple-test
  "A simple test of addition"
  (math-test))

;; Tests and steps are just data

(math-test)

(simple-test)
;; {:greenlight.test/description "A simple test of addition",
;;     :greenlight.test/line 14,
;;     :greenlight.test/ns user,
;;     :greenlight.test/steps [{:greenlight.step/inputs {},
;;                              :greenlight.step/name math-test,
;;                              :greenlight.step/test #<Fn@7bb15aaa user/math_test[fn]>,
;;                              :greenlight.step/title "Simple Math Test"}],
;;     :greenlight.test/title "simple-test"}


;; Tests can be ran individually

(test/run-test! {} (simple-test))
   ;; {:greenlight.test/context {},
   ;;  :greenlight.test/description "A simple test of addition",
   ;;  :greenlight.test/ended-at #<java.time.Instant@55e7469c 2018-07-01T17:03:29.811Z>,
   ;;  :greenlight.test/line 14,
   ;;  :greenlight.test/ns user,
   ;;  :greenlight.test/outcome :pass,
   ;;  :greenlight.test/started-at #<java.time.Instant@224450d6 2018-07-01T17:03:29.808Z>,
   ;;  :greenlight.test/steps [{:greenlight.step/cleanup [],
   ;;                           :greenlight.step/elapsed 0.002573744,
   ;;                           :greenlight.step/inputs {},
   ;;                           :greenlight.step/message "1 assertions (1 pass)",
   ;;                           :greenlight.step/name math-test,
   ;;                           :greenlight.step/outcome :pass,
   ;;                           :greenlight.step/reports [{:actual (3),
   ;;                                                      :expected 3,
   ;;                                                      :message nil,
   ;;                                                      :type :pass}],
   ;;                           :greenlight.step/test #<Fn@2be25eaa user/math_test[fn]>,
   ;;                           :greenlight.step/title "Simple Math Test"}],
   ;;  :greenlight.test/title "simple-test"}

;; Or as part of a suite with configurable reporters

(runner/run-tests! (constantly {}) [(simple-test)] {})

; Starting test system...
; Running 1 tests...
;
;  * Testing simple-test
;  | user:124
;  | A simple test of addition
;  |
;  +->> Simple Math Test
;  | 1 assertions (1 pass)
;  | [PASS] (0.000 seconds)
;  |
;  |
;  * PASS (0.001 seconds)
;
;
; Ran 1 tests containing 1 steps with 1 assertions:
; * 1 pass
