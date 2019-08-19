(ns clojure-experiments.purely-functional.property-based-testing.09-complex-generators
  (:require
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.string :as str]
   [clojure.test.check.properties :as prop]))

;;;; Building an email address generator.
;;;; Very often we use strings which are expected to follow  particular format.



;;; There are many regex validators online
;;; this is my attempt to copy the one from the lecture but it doesn't work
(def email-re #"(([^<>()\[\]\.,;:\s@\"]+(\.[^<>()\[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()\[\]\.,;:\s@\"]+\.)+[^<>()\[\]\.,;:\s@\"]{2,})")
(re-matches email-re "jumarko@gmail.com")
;; => ["jumarko@gmail.com" "jumarko" "jumarko" nil nil "gmail.com" "gmail."]

;;; start with such-that -> won't work
#_(gen/sample (gen/such-that #(re-matches email-re %) gen/string-ascii))
;; => Couldn't satisfy...

;;; Start by cleaning up our regex
;;; => give a name to repeating definitions
(def email-char-re #"[^<>()\[\]\.,;:\s@\"]")

(def gen-email-char (gen/such-that #(re-matches email-char-re (str %))
                                   gen/char-ascii))


(gen/sample gen-email-char)
;; => (\m \H \h \2 \0 \G \z \z \/ \|)

;; generate a vector of chars and "join" them
(def gen-email-string (gen/fmap str/join (gen/vector gen-email-char)))
(gen/sample gen-email-string)
;; => ("" "" "" "J" "" "$B|2#" "Kvq" "Su3E" "FDcd`}p" "")



;;; Let's put the pieces together
;; TODO: get rid of repeated not-empty?
(def gen-email
  (gen/fmap 
   (fn [[n1 nn hns tld1 tld2]]
     (str (str/join "." (cons n1 nn))
          "@"
          ;; thns is vector so add at the end
          (str/join "." (conj hns (str tld1 tld2)))))
   (gen/tuple (gen/not-empty gen-email-string)
              (gen/vector (gen/not-empty gen-email-string))
              (gen/not-empty (gen/vector (gen/not-empty gen-email-string)))
              (gen/not-empty gen-email-string)
              (gen/not-empty gen-email-string))))
(gen/sample gen-email)
;; => ("I@H.6`-D" "Et@TZ.g\\.'mx" "$.T@t.P.7WMr&" "S.-@\\d.bY.9CS.EZz=C7f" "0bXt.oJ|@|.u.e/7x8|ja" "=vJ!S.~hF^@S53wH.w.*'P*~.X%2Q.X#SP'RV" "73r@H2Td5.f.L=kA5.1lIP$.ceycUFhqI" "6{}+3~.+CjB.?!B@ytlb\\.=hXoe.W\\.J.GH+T*ugV" "xA}7!Y$.=UY#`S@h%.73x2&=*n" "q%xjNmd.!y|G8bL6N\\.o_}Jp6.9bSlQ!.Y9u=T.G.8y#@NHr?G8.P!+9lp2fyBCk")

;; check that generated emails pass the regex
(comment
  (tc/quick-check
   100
   (prop/for-all [email gen-email]
                 (re-matches email-re email)))
;; => {:result true, :pass? true, :num-tests 100, :time-elapsed-ms 217, :seed 1566209310215} 
)


;;; REFACTORING

;; get rid of not-empty
(def gen-email-string (gen/not-empty (gen/fmap str/join (gen/vector gen-email-char))))

(def gen-email
  (gen/fmap 
   (fn [[n1 nn hns tld1 tld2]]
     (str (str/join "." (cons n1 nn))
          "@"
          ;; thns is vector so add at the end
          (str/join "." (conj hns (str tld1 tld2)))))
   (gen/tuple gen-email-string
              (gen/vector gen-email-string)
              (gen/not-empty (gen/vector gen-email-string))
              gen-email-string
              gen-email-string)))

;; split into email name and domain
(def gen-regular-email-name (gen/fmap
                             (fn [[n1 nn]]
                               (str/join "." (cons n1 nn)))
                             (gen/tuple gen-email-string
                                        (gen/vector gen-email-string))))
(def gen-irregular-email-name (gen/fmap
                               #(str \" % \")
                               (gen/not-empty gen/string-ascii)))
(def gen-email-name (gen/one-of [gen-regular-email-name gen-irregular-email-name]))
(def gen-email-domain (gen/fmap
                       (fn [[hns tld1 tld2]]
                         (str/join "." (conj hns (str tld1 tld2))))
                       (gen/tuple (gen/not-empty (gen/vector gen-email-string))
                                  gen-email-string
                                  gen-email-string)))
(def gen-email
  (gen/fmap 
   (fn [[name domain]]
     (str name
          "@"
          ;; hns is vector so add at the end
          domain))
   (gen/tuple gen-email-name gen-email-domain)))

(comment
  (tc/quick-check
   100
   (prop/for-all [email gen-email]
                 (re-matches email-re email)))
  ;; => {:result true, :pass? true, :num-tests 100, :time-elapsed-ms 217, :seed 1566209310215} 
)


;;; Our generator generates many crazy emails
;;; That might be what we want to really test our system
;;; However, we may want more readable email addresses...

(def gen-email-name2 (gen/elements ["bob" "suzy" "john" "jill"]))
(def gen-email-domain2 (gen/elements ["gmail.com" "hotmail.com" "example.com"]))
(def gen-email2
  (gen/fmap 
   (fn [[name domain]]
     (str name
          "@"
          ;; hns is vector so add at the end
          domain))
   (gen/tuple gen-email-name2 gen-email-domain2)))
(gen/sample gen-email2)

;; but let's make that generate a bit more emails...
(def gen-email2
  (gen/fmap 
   (fn [[name n domain]]
     (str name
          n
          "@"
          ;; hns is vector so add at the end
          domain))
   (gen/tuple gen-email-name2 gen/nat gen-email-domain2)))
(gen/sample gen-email2)
;; => ("suzy0@example.com" "john1@example.com" "suzy1@hotmail.com" "suzy2@gmail.com" "bob4@example.com" "jill1@gmail.com" "john4@hotmail.com" "jill3@hotmail.com" "suzy3@hotmail.com" "suzy3@gmail.com")

(comment
  (tc/quick-check
   100
   (prop/for-all [email gen-email2]
                 (re-matches email-re email)))
  )
