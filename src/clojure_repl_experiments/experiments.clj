(ns clojure-repl-experiments.experiments
  "Single namespace for all my REPL experiments.
  This might be split up later if I find it useful."
  (:require [seesaw.core :as see]
            [seesaw.core :as s]))

;;; Seesaw tutorial: https://gist.github.com/1441520
;;; check also https://github.com/daveray/seesaw
(comment
  (def f (see/frame :title "Get to know Seesaw"))
  (-> f see/pack! see/show!)
  )



;;; Simple By Design blog post: https://drewverlee.github.io/posts-output/2017-4-26-simple-by-design
;;;

;; everything is data
{
 :string "hello"
 :character \f
 :integer 42
 :floating-point 3.14
 :boolean true
 :symbol +
 :keyword [:foo ::foo]
 }

{:list '(1 2 3)
 :vector [1 2 3]
 :map {:a 1 :b 2 :c 3}
 :set #{:a :b :c}
 }

;; Macros
(defmacro backwards [form]
  (reverse form))

;; References
(defprotocol Nachos
  (yum [_] "eat some nachos"))
(defrecord Person [name lbs]
  Nachos
  (yum [person]
    (update-in person [:lbs] + 2)))

(def me (atom (->Person "Drew" 182)))
(def me-before @me)
(swap! me yum)
(def me-after @me)
me-after
me-before

;; Generality through reuse
(def xf (comp (remove odd?) (map inc)))
(transduce xf conj [] (range 10))
;; simplified
(into [] xf (range 10))

;; Safety where you need it - clojure.spec
(require '[clojure.spec.alpha :as s])
(require '[clojure.spec.gen.alpha :as gen])
(s/def ::big-even (s/and integer? even? #(> % 1000)))
(s/valid? ::big-even 10)
(s/valid? ::big-even 1002)
;; now we can attach it to API and catch any nasty non-big numbers
(s/explain-data ::big-even 5)
;; and we can generate examples automatically!
(gen/sample (s/gen ::big-even))



;;; multimethod experiments
;;;
(defmulti login (fn [user-role credentials] user-role))
(defmethod login :admin [_ credentials]
  (println "Admin credentials: " credentials))
(defmethod login :user [_ credentials]
  (println "User credentials: " credentials))

(login :admin {:licensee "jumarko" :license-key "xyz"})

(login :user {:licensee "jumarko" :license-key "xyz"})


