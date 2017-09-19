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


;;; how to compose multiple predicate functions into one?
;;; https://stackoverflow.com/questions/45643579/compose-multiple-predicate-functions-into-one
;;; => some-fn
(defn- multiple-of-three? [n] (zero? (mod n 3)))
(defn- multiple-of-five? [n] (zero? (mod n 5)))
(def multiple-of-three-or-five?
  (some-fn multiple-of-three? multiple-of-five?))
(multiple-of-three-or-five? 3)
(multiple-of-three-or-five? 4)
(multiple-of-three-or-five? 5)



;;; Dump system info from system properties
;;; See https://www.roseindia.net/java/beginners/OSInformation.shtml
(def env-system-properties
  ["os.name"
   "os.version"
   "os.arch"
   "java.version"
   "java.class.version"
   "java.vendor"
   "java.vm.name"
   "java.vm.vendor"
   "java.vm.info"
   "java.home"
   "java.io.tmpdir"
   "user.name"
   "user.dir"
   "user.timezone"
   "user.language"
   "file.encoding"
   "file.separator"
   "path.separator"
   "line.separator"
   ])

(defn- environment-info []
  (let [runtime (Runtime/getRuntime)]
    (into (->> env-system-properties
               (mapv (fn [prop-name] [prop-name (System/getProperty prop-name)])))
          [["available processors" (.availableProcessors runtime)]
           ["free memory" (.freeMemory runtime)]
           ["max memory" (.maxMemory runtime)]
           ["total memory" (.totalMemory runtime)]])))


;;; Great explanation of lazy sequences: https://groups.google.com/forum/#!topic/clojure/R288cQPgazw
;;; fibonacci numbers example

(def fibs (cons 0 (cons 1 (lazy-seq (map +' fibs (rest fibs))))))
(take 10 fibs)

;; Exercise:
;; You put the lazy seq at the beginning and it hit an infinite loop.
;; You put the lazy seq after the first two hard-coded values, and it worked.
;; What happens when you put it after just the first hard-coded value? Can you explain why?
;; (def fibs (cons 0 (lazy-seq (cons 1 (map +' fibs (rest fibs))))))
(take 10 fibs)

;; alternative fibo implementation using loop-recur
(defn fibo2 [n]
  (loop [a 1 b 0 acc n]
    (if (zero? acc)
      b
      (recur
       (+' a b)
       a
       (dec acc)))))



;; Asked on Clojurians slack 19.9.2017 - #beginners
(defmulti query-dispatcher first)
(defmethod query-dispatcher "Select" [[_select [lhs op rhs]]]
  (str lhs " " op " " rhs))

(def q  ["Select" [
                   "first_name" "like" "Sitges"]])
(query-dispatcher q)
