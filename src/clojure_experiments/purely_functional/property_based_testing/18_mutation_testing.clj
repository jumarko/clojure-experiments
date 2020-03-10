(ns clojure-experiments.purely-functional.property-based-testing.18-mutation-testing
  "https://purelyfunctional.tv/lesson/building-complex-generators-mutation-testing/"
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.test.check :as tc]
            [clojure.string :as string]
            [clojure-experiments.purely-functional.property-based-testing.09-complex-generators :as complex-gen]))

;;;; Mutation Testing is about testing "almost correct data" (like a "eric@lispcastcom" email)
;;;; not completely garbage data (like random strings)
;;;; - 1. Generate correct data
;;;; - 2. Change it randomly
;;;; - 3. Filter out ones that are still correct


;; let's copy this from `clojure-experiments.purely-functional.property-based-testing.09-complex-generators` 
(def email-re #"(([^<>()\[\]\.,;:\s@\"]+(\.[^<>()\[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()\[\]\.,;:\s@\"]+\.)+[^<>()\[\]\.,;:\s@\"]{2,})")

(defn valid-email [email]
  (re-matches email-re email))

(valid-email "eric@lispcast.com")

;; a function which we want to test - throws an exception if the email is invalid

(defn save-email! [email]
  (if (valid-email email)
    (println "Saving email into DB...")
    (throw (ex-info "Invalid email not maching regex" {:email-regex (.pattern email-re)}))))

;; invalid email example
#_(save-email! "eric@lispcastcom")

(defspec bad-emails-fail 100
  (prop/for-all 
   [s (gen/such-that (complement valid-email) gen/string-ascii)]
   (try (save-email! s)
        false
        (catch Exception e
          true)))
  )

;; but look, all those tested emails are garbage!
(comment

  (gen/sample (gen/such-that (complement valid-email) gen/string-ascii))
  ;; => ("" "" "\\" "FU" "!Mz" "j.Qh" "J" "X:BHQ%c" "3N" "89&YB(uC")

  ;;
  )

;;; we'd like to test little typos like "eric@lispcastcom" => Mutation Testing

;;; Let's start with adding a random character
(defn add-char [s char idx]
  (let [idx (mod idx (inc (count s)))] 
    (str (subs s 0 idx)
         char
         (subs s idx))))
(add-char "abc" \J 0)
;; => "Jabc"
(add-char "abc" \J 12)
;; => "Jabc"

(defn drop-char [s idx]
  (let [idx (mod idx (count s))] 
    (str (subs s 0 idx)
         (subs s (inc idx)))))
(drop-char "abc" 0)
;; => "bc"
(drop-char "abc" 1)
;; => "ac"
(drop-char "abc" 2)
;; => "ab"
(drop-char "abc" 3)
;; => "bc"


;;; Now we want to use `add-char` and `bad-char` in generators
(def gen-add-char (gen/tuple (gen/return :add)
                              gen/char-ascii
                              gen/nat))
#_(gen/sample gen-add-char)
;; => ([:add \* 0]
;;     [:add \3 0]
;;     [:add \S 1]
;;     [:add \" 2]
;; ...

(def gen-drop-char (gen/tuple (gen/return :drop)
                              gen/nat))

(def gen-str-mutation (gen/one-of [gen-drop-char gen-add-char]))

(defn mutate [s [op & data]]
  (case op
    :add (apply add-char s data)
    :drop (apply drop-char s data)))

(def gen-bad-email (gen/such-that
                    (complement valid-email)
                    (gen/fmap
                     (fn [[e ms]]
                       (reduce mutate e ms))
                     (gen/tuple complex-gen/gen-email
                                (gen/vector gen-str-mutation)))))

;; Now we can test better "bad" emails 
#_(gen/sample gen-bad-email)
;; => ("#j\"@1N.W|.if6P"
;;     "<\"@~.9V"
;;     "XqO}\"@nZqOq.tu.#}Uu.Aw1s.wvZYQ"
;;     "yl.]D/G+.r_.YP=#Nm@!*.&T`"
;;     "\"p@`Rw.6.?.HGPHI.&$|.CoXK8s="
;;     "\"1{?\"l@!R.sS.a-9Zq"
;;     "\"aOU_Zk+Ha.b!OwBW1j*F.h.ww\\&U1Fwy*.?YSzP.eAl.Q-Ke=HCHeT.o.4%.q##$L2C4e"
;;     



