(ns clojure-repl-experiments.experiments
  "Single namespace for all my REPL experiments.
  This might be split up later if I find it useful."
  (:require [seesaw.core :as see]))

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


;; Petr Mensik's problem:
;; How to access https://pdfbox.apache.org/docs/2.0.4/javadocs/org/apache/pdfbox/pdmodel/PDPageContentStream.AppendMode.html
;; You have to import inner enum class
(import org.apache.pdfbox.pdmodel.PDPageContentStream$AppendMode)
org.apache.pdfbox.pdmodel.PDPageContentStream$AppendMode



;;; Asked on clojurians slack - brave-and-true channel 3.8.2017
(def filename "/Users/jumar/workspace/clojure/clojure-repl-experiments/resources/suspects.csv")
(slurp filename)
(def vamp-keys [:name :glitter-index])
(defn str->int
  [str]
  (Integer. str))
(def conversions {:name identity
                  :glitter-index str->int})
(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))
(defn parse
  "Convert a CSV into rows of columns"
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\r?\n")))
(parse (slurp filename))
(defn mapify
  "Return a seq of maps like {:name \"Edward Cullen\" :glitter-index 10}"
  [rows]
  (map (fn [unmapped-row]
         (reduce (fn [row-map [vamp-key value]]
                   (assoc row-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys unmapped-row)))
       rows))
(first (parse (slurp filename)))
(first (mapify (parse (slurp filename)))) ;; NullPointer here
(mapify (parse (slurp filename))) ;; and here


;;;
;;; defrecord and protocol experiments from Joy of Clojure
;;;
(defrecord TreeNode [v l r])
(def my-tree (TreeNode. 0 nil nil))
;; you can assoc new fields but the performance is as with regular map
(assoc my-tree :color :red)
;; you can dissoc fields but this will return normal map, not defrecord
(type (dissoc my-tree :v))
;; defrecord never equals to the map with same keys
(= my-tree {:v 0 :l nil :r nil})


;;; Clojure style mixins from Joy of Clojure - p. 211
;;; notice that you have to always provide full implementation of protocol
(defprotocol StringOps
  (rev [s])
  (upp [s]))

(extend-type String
  StringOps
  (rev [s] (clojure.string/reverse s)))

(rev "Works")

;; now try to extend `upp` separately
(extend-type String
  StringOps
  (upp [s] (clojure.string/upper-case s)))
(upp "Works")
;; but `rev` is lost now
#_(rev "Works")
;; but we still can define mixinx separately
(def rev-mixin {:rev clojure.string/reverse})
(def upp-mixin {:upp clojure.string/upper-case})
(def fully-mixed (merge rev-mixin upp-mixin))
(extend String StringOps fully-mixed)
(-> "Works" upp rev)



